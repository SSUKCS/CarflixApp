package com.example.carflix;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Executor;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

import java.util.Timer;
import java.util.TimerTask;

import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;

public class CarInterface extends AppCompatActivity {
    public static final String TAG = "CarInterface";
    ImageView carImg;
    TextView carName;
    TextView textViewCarStatus;

    Button btnDoorOpen;
    Button btnDoorClose;
    Button btnTrunkOpen;
    Button btnTrunkClose;
    Button btnStartCar;

    String memberID;
    CarData carData;

    LoadingDialog controlDialog;
    LoadingDialog turnOnDialog;

    private CarController carController;

    private CarTracingService carTracingService;
    private boolean isCarTracingOn;
    private final CarTracingService.StateUpdateCallBack carTracingStateUpdateCallBack = this::carTracingStateUpdateCallBack;

    public String getCarStatusFromServer(){
        //서버로부터 자동차 상태를 받아오는 코드
        return CarData.AVAILABLE;
    }

    TimerTask timerTask;
    Timer updateFromServerTimer;
    long updateTerm = 3000;
    private void startRepeatUpdate(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "run: 차량 상태 업데이트");
                // String carStatus = getCarStatusFromServer();
                // setCarState(carStatus);
            }
        };
        updateFromServerTimer = new Timer();
        updateFromServerTimer.schedule(timerTask, 1000, updateTerm);
    }

    private void cancelRepeatUpdate(){
        timerTask.cancel();
    }

    private void setCarState(String carStatus){
        textViewCarStatus.setText(carStatus);
        switch(carStatus){
            case CarData.AVAILABLE:
                textViewCarStatus.setTextColor(Color.parseColor("#4488FF"));
                btnStartCar.setVisibility(View.VISIBLE);
                break;
            case CarData.OCCUPIED:
                textViewCarStatus.setTextColor(Color.parseColor("#FF5544"));
                btnStartCar.setVisibility(View.INVISIBLE);
                break;
            case CarData.DRIVING:
                textViewCarStatus.setTextColor(Color.parseColor("#9911BB"));
                btnStartCar.setVisibility(View.INVISIBLE);
                break;
            default:
                textViewCarStatus.setTextColor(Color.parseColor("#000"));
                break;
        }
    }

    private final ServiceConnection carTracingStateBindConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i("CarInterface_carTracingStateBindConnection", "onServiceConnected: connected");
            CarTracingService.CarServiceBinder carServiceBinder = (CarTracingService.CarServiceBinder) iBinder;
            carTracingService = carServiceBinder.getService();
            if(carTracingService.getState().equals(CarTracingService.SUCCESSFUL_CAR_ON)){
                setCarState(CarData.DRIVING);
            }
            carTracingService.registerCallback(carTracingStateUpdateCallBack);
            carTracingStateUpdateCallBack(carTracingService.getState());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i("CarInterface_CarInterface_carTracingStateBindConnection", "onServiceDisconnected: disconnected");
            carTracingService = null;
        }
    };


    private class CarControllerCallback implements CarController.CarControlCallback {
        @Override
        public void onStateUpdate(String state) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (state) {
                        case ArduinoBluetooth.SEARCHING:
                            controlDialog.setText("기기 탐색중...");
                            break;
                        case ArduinoBluetooth.FOUND_DEVICE:
                            controlDialog.setText("기기에 신호를 보내는중...");
                            break;
                        case ArduinoBluetooth.SUCCESSFUL_CONNECTION:
                            controlDialog.setText("기기에 신호를 보내는중...");
                            controlDialog.setTextColor(Color.parseColor("#4488FF"));
                            break;
                        case ArduinoBluetooth.FAILED_CONNECTION:
                            controlDialog.setText("연결 실패");
                            controlDialog.setTextColor(Color.parseColor("#F23920"));
                            carController.endConnection();
                            carController = null;
                            controlDialog.dismiss();
                            break;
                        case CarController.SUCCESSFUL_CONTROL:
                            controlDialog.setText("제어 성공");
                            controlDialog.setTextColor(Color.parseColor("#4488FF"));
                            carController.endConnection();
                            carController = null;
                            controlDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "성공", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });

        }

        @Override
        public void onConnectFailed() {
        }

        @Override
        public void onBluetoothNotOn() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "블루투스가 꺼져있습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private Intent startServiceIntent;
    private Intent bindServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tracingServiceIntent = new Intent(getApplicationContext(), CarTracingService.class);
        isCarTracingOn = false;

        setContentView(R.layout.car_interface);
        controlDialog = new LoadingDialog(this);
        turnOnDialog = new LoadingDialog(this);


        controlDialog.registerBackPressed(new LoadingDialog.DialogBackPressed() {
            @Override
            public void onBackPressed() {
                if (carController != null) {
                    carController.endConnection();
                    carController = null;
                    Toast.makeText(getApplicationContext(), "취소됨.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        turnOnDialog.registerBackPressed(new LoadingDialog.DialogBackPressed(){
            @Override
            public void onBackPressed() {
                if(carTracingService != null &&
                        !carTracingService.getState().equals(CarTracingService.SUCCESSFUL_CAR_ON)){
                    carTracingService.end();
                    unbindService(carTracingStateBindConnection);
                    isCarTracingOn = false;
                    Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //W/Activity: Can request only one set of permissions at a time
        getPermission();
        connectUI();

        memberID = getIntent().getStringExtra("memberID");
        carData = (CarData)getIntent().getSerializableExtra("carData");
        switch(carData.getCarStatus())
        {
            case CarData.AVAILABLE:
                textViewCarStatus.setTextColor(Color.parseColor("#4488FF"));
                break;
            case CarData.OCCUPIED:
                textViewCarStatus.setTextColor(Color.parseColor("#FF5544"));
                break;
            case CarData.DRIVING:
                textViewCarStatus.setTextColor(Color.parseColor("#9911BB"));
                break;
        }

        carImg.setImageResource(carData.getcarImg());
        carName.setText(carData.getCarName());

        //차량 문을 연다
        btnDoorOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlDialog.show();
                carController = new CarController(
                        getApplicationContext(), BluetoothAdapter.getDefaultAdapter(),
                        new CarControllerCallback(),
                        CarController.DOOR_OPEN,
                        memberID, carData.getCarID()
                );
                carController.start();
            }
        });

        //차량 문을 닫는다
        btnDoorClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlDialog.show();
                carController = new CarController(
                        getApplicationContext(), BluetoothAdapter.getDefaultAdapter(),
                        new CarControllerCallback(),
                        CarController.DOOR_CLOSE,
                        memberID, carData.getCarID()
                );
                carController.start();
            }
        });

        //차량 트렁크 문을 연다
        btnTrunkOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlDialog.show();
                carController = new CarController(
                        getApplicationContext(), BluetoothAdapter.getDefaultAdapter(),
                        new CarControllerCallback(),
                        CarController.TRUNK_OPEN,
                        memberID, carData.getCarID()
                );
                carController.start();
            }
        });
        //차량 트렁크 문을 닫는다
        btnTrunkClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlDialog.show();
                carController = new CarController(
                        getApplicationContext(), BluetoothAdapter.getDefaultAdapter(),
                        new CarControllerCallback(),
                        CarController.TRUNK_CLOSE,
                        memberID, carData.getCarID()
                );
                carController.start();
            }
        });

        //차량에 시동을 건다/시동이 걸려있으면 끈다
        btnStartCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turnOnDialog.show();
                /*
                biometricPrompt.authenticate(promptInfo);*/

                startServiceIntent = new Intent(getApplicationContext(), CarTracingService.class);
                startServiceIntent.putExtra("mb_id", memberID);
                startServiceIntent.putExtra("car_data", carData);
                startServiceIntent.putExtra("mac_address", carData.getMac_address());
                startService(startServiceIntent);

                bindService(tracingServiceIntent, carTracingStateBindConnection, BIND_AUTO_CREATE);
                isCarTracingOn = true;
                //버튼을 보이지 않게 한다.
                view.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onPause() {
        if(carTracingService != null) {
            unbindService(carTracingStateBindConnection);
            carTracingService = null;
        }
        cancelRepeatUpdate();
        super.onPause();
    }

    private Intent tracingServiceIntent;

    @Override
    protected void onResume() {
        setCarState(getCarStatusFromServer());
        super.onResume();
        if(isCarTracingOn){
            bindService(tracingServiceIntent, carTracingStateBindConnection, BIND_AUTO_CREATE);
        }
        else {
            startRepeatUpdate();
        }
    }

    private void carTracingStateUpdateCallBack(String state){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch(state){
                    case ArduinoBluetooth.SEARCHING:
                        turnOnDialog.setText("기기 탐색중...");
                        break;
                    case ArduinoBluetooth.FOUND_DEVICE:
                        turnOnDialog.setText("기기 연결중....");
                        break;
                    case ArduinoBluetooth.SUCCESSFUL_CONNECTION:
                        turnOnDialog.setText("연결 성공");
                        turnOnDialog.setTextColor(Color.parseColor("#4488FF"));
                        controlDialog.dismiss();
                        break;
                    case ArduinoBluetooth.FAILED_CONNECTION:
                        turnOnDialog.setText("연결 실패");
                        turnOnDialog.setTextColor(Color.parseColor("#F23920"));
                        controlDialog.dismiss();
                        break;
                    case CarTracingService.SUCCESSFUL_CAR_ON:
                        setCarState(CarData.DRIVING);
                        Toast.makeText(getApplicationContext(), "시동 성공", Toast.LENGTH_SHORT).show();
                        turnOnDialog.dismiss();
                        startRepeatUpdate();
                        break;
                    case CarTracingService.FAILED_CAR_ON:
                        Toast.makeText(getApplicationContext(), "시동 실패", Toast.LENGTH_SHORT).show();
                        turnOnDialog.dismiss();
                        break;
                    case CarTracingService.FINISHED:
                        setCarState(CarData.AVAILABLE);
                        unbindService(carTracingStateBindConnection);
                        startRepeatUpdate();
                        carTracingService = null;
                        isCarTracingOn = false;
                        break;
                }
            }
        });

    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(controlDialog.isShowing())
            controlDialog.dismiss();
    }

    @Override
    public void onBackPressed(){
        controlDialog.dismiss();
        turnOnDialog.dismiss();
        finish();
    }

    private void connectUI(){
        carImg = (ImageView)findViewById(R.id.carImg);
        carName = (TextView)findViewById(R.id.carName);
        textViewCarStatus = (TextView)findViewById(R.id.isAvailable);

        btnDoorOpen = (Button)findViewById(R.id.doorOpen);
        btnDoorClose = (Button)findViewById(R.id.doorClose);
        btnTrunkOpen = (Button)findViewById(R.id.trunkOpen);
        btnTrunkClose = (Button)findViewById(R.id.trunkClose);
        btnStartCar = (Button)findViewById(R.id.startCar);
    }

    private void getPermission(){
        if(!(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN)==PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)==PackageManager.PERMISSION_GRANTED))
        {
            // 권한이 없을 경우 권한을 요구함
            String[] permission_list = {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            };
            ActivityCompat.requestPermissions(this, permission_list, 1);
        }
        else{
            finish();
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}