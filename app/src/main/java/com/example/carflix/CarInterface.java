package com.example.carflix;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;



public class CarInterface extends AppCompatActivity {
    int position;
    private Context context;

    ImageView carImg;
    TextView carName;
    TextView isAvailable;

    Button doorOpen;
    Button doorClose;
    Button trunkOpen;
    Button trunkClose;
    Button startCar;

    String memberID;
    CarData carData;

    LoadingDialog dialog;

    private CarController carController;

    private CarTracingService carTracingService;
    private final CarTracingService.StateUpdateCallBack carTracingStateUpdateCallBack = this::carTracingStateUpdateCallBack;
    private final ServiceConnection carTracingStateBindConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i("CarInterface_carTracingStateBindConnection", "onServiceConnected: connected");
            CarTracingService.CarServiceBinder carServiceBinder = (CarTracingService.CarServiceBinder) iBinder;
            carTracingService = carServiceBinder.getService();
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
                            dialog.setText("기기 탐색중...");
                            isAvailable.setTextColor(Color.parseColor("#5DC19B"));
                            break;
                        case ArduinoBluetooth.FOUND_DEVICE:
                            dialog.setText("기기에 신호를 보내는중...");
                            isAvailable.setTextColor(Color.parseColor("#5DC19B"));
                            break;
                        case ArduinoBluetooth.SUCCESSFUL_CONNECTION:
                            dialog.setText("기기에 신호를 보내는중...");
                            dialog.setTextColor(Color.parseColor("#4488FF"));
                            break;
                        case ArduinoBluetooth.FAILED_CONNECTION:
                            dialog.setText("연결 실패");
                            dialog.setTextColor(Color.parseColor("#F23920"));
                            carController.endConnection();
                            carController = null;
                            dialog.dismiss();
                            break;
                        case CarController.SUCCESSFUL_CONTROL:
                            dialog.setText("제어 성공");
                            dialog.setTextColor(Color.parseColor("#4488FF"));
                            carController.endConnection();
                            carController = null;
                            dialog.dismiss();
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
        setContentView(R.layout.car_interface);
        dialog = new LoadingDialog(this);
        dialog.registerBackPressed(new LoadingDialog.DialogBackPressed() {
            @Override
            public void onBackPressed() {
                if(carController != null){
                    carController.endConnection();
                    carController = null;
                    Toast.makeText(getApplicationContext(), "취소됨.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //W/Activity: Can request only one set of permissions at a time
        getPermission();
        connectUI();

        context = getApplicationContext();
        memberID = getIntent().getStringExtra("memberID");
        position = getIntent().getIntExtra("position", -1);
        carData = (CarData)getIntent().getSerializableExtra("carData");
        switch(carData.getStatus())
        {
            case"운전 가능":isAvailable.setTextColor(Color.parseColor("#4488FF"));break;
            case"운전 불가능":isAvailable.setTextColor(Color.parseColor("#FF5544"));break;
            case"운전중":isAvailable.setTextColor(Color.parseColor("#9911BB"));break;
        }

        carImg.setImageResource(carData.getcarImg());
        carName.setText(carData.getCarName());
        isAvailable.setText(carData.getStatus());


        //차량 문을 연다
        doorOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                carController = new CarController(
                        getApplicationContext(), BluetoothAdapter.getDefaultAdapter(),
                        new CarControllerCallback(),
                        CarController.DOOR_OPEN,
                        memberID
                );
                carController.start();
            }
        });

        //차량 문을 닫는다
        doorClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                carController = new CarController(
                        getApplicationContext(), BluetoothAdapter.getDefaultAdapter(),
                        new CarControllerCallback(),
                        CarController.DOOR_CLOSE,
                        memberID
                );
                carController.start();
            }
        });

        //차량 트렁크 문을 연다
        trunkOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                carController = new CarController(
                        getApplicationContext(), BluetoothAdapter.getDefaultAdapter(),
                        new CarControllerCallback(),
                        CarController.TRUNK_OPEN,
                        memberID
                );
                carController.start();
            }
        });
        //차량 트렁크 문을 닫는다
        trunkClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                carController = new CarController(
                        getApplicationContext(), BluetoothAdapter.getDefaultAdapter(),
                        new CarControllerCallback(),
                        CarController.TRUNK_CLOSE,
                        memberID
                );
                carController.start();
            }
        });

        //차량에 시동을 건다/시동이 걸려있으면 끈다
        startCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                /*
                biometricPrompt.authenticate(promptInfo);*/

                carData.setAvailable(false);

                startServiceIntent = new Intent(getApplicationContext(), CarTracingService.class);
                startServiceIntent.putExtra("mb_id", memberID);
                startServiceIntent.putExtra("car_name",carData.getCarName());
                startServiceIntent.putExtra("mac_address", carData.getMac_address());
                startService(startServiceIntent);

                Intent tracingBindService = new Intent(getApplicationContext(), CarTracingService.class);
                bindService(tracingBindService, carTracingStateBindConnection, BIND_AUTO_CREATE);
                //버튼을 보이지 않게 한다.
                view.setVisibility(View.INVISIBLE);
            }
        });
    }
    private void carTracingStateUpdateCallBack(String state){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch(state){
                    case ArduinoBluetooth.SEARCHING:
                        dialog.setText("기기 탐색중...");
                        isAvailable.setTextColor(Color.parseColor("#5DC19B"));
                        break;
                    case ArduinoBluetooth.FOUND_DEVICE:
                        dialog.setText("기기 연결중....");
                        isAvailable.setTextColor(Color.parseColor("#5DC19B"));
                        break;
                    case ArduinoBluetooth.SUCCESSFUL_CONNECTION:
                        dialog.setText("연결 성공");
                        dialog.setTextColor(Color.parseColor("#4488FF"));
                        dialog.dismiss();
                        break;
                    case ArduinoBluetooth.FAILED_CONNECTION:
                        dialog.setText("연결 실패");
                        dialog.setTextColor(Color.parseColor("#F23920"));
                        dialog.dismiss();
                        break;
                    case CarTracingService.SUCCESSFUL_CAR_ON:
                        isAvailable.setText("운전중");
                        isAvailable.setTextColor(Color.parseColor("#9911BB"));
                        unbindService(carTracingStateBindConnection);
                        dialog.dismiss();
                        break;
                    case CarTracingService.FAILED_CAR_ON:
                        isAvailable.setText("운전 불가능");
                        isAvailable.setTextColor(Color.parseColor("#FF5544"));
                        unbindService(carTracingStateBindConnection);
                        dialog.dismiss();
                        break;
                }
            }
        });

    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(dialog.isShowing())
            dialog.dismiss();
    }

    @Override
    public void onBackPressed(){
        dialog.dismiss();
        finish();
    }

    private void connectUI(){
        carImg = (ImageView)findViewById(R.id.carImg);
        carName = (TextView)findViewById(R.id.carName);
        isAvailable = (TextView)findViewById(R.id.isAvailable);

        doorOpen = (Button)findViewById(R.id.doorOpen);
        doorClose = (Button)findViewById(R.id.doorClose);
        trunkOpen = (Button)findViewById(R.id.trunkOpen);
        trunkClose = (Button)findViewById(R.id.trunkClose);
        startCar = (Button)findViewById(R.id.startCar);
    }

    private void getPermission(){
        if(!(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
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
}
