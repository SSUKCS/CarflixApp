package com.example.carflix;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
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

import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;


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

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

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

    private CarControlService carControlService;
    private CarControlService.StateUpdateCallBack carControlStateUpdateCallBack = this::carControlStateUpdateCallBack;
    private final ServiceConnection carControlServiceBindConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i("CarInterface_carControlServiceBindConnection", "onServiceConnected: connected");
            CarControlService.CarServiceBinder carServiceBinder = (CarControlService.CarServiceBinder) iBinder;
            carControlService = carServiceBinder.getService();
            carControlService.registerCallback(carControlStateUpdateCallBack);
            carTracingStateUpdateCallBack(carControlService.getState());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i("CarInterface_CarInterface_carControlServiceBindConnection", "onServiceDisconnected: disconnected");
            carControlService = null;
        }
    };

    private Intent startServiceIntent;
    private Intent bindServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_interface);
        dialog = new LoadingDialog(this);
        getPermission();

        if (BluetoothAdapter.getDefaultAdapter()!=null&&BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                Toast.makeText(context, "블루투스 권한을 허용해주세요.", Toast.LENGTH_LONG).show();
                finish();
            }
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT, savedInstanceState); is deprecated
            ActivityResultLauncher<Intent> startActivityForResult =
                    registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                        if(result.getResultCode()!=RESULT_OK){
                            Toast.makeText(context, "블루투스 권한을 허용해주세요.", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
            startActivityForResult.launch(enableBtIntent);
        }
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
        /*DEVICE_CREDENTIAL 및 BIOMETRIC_STRONG | DEVICE_CREDENTIAL 인증자 유형 조합은
        Android 10(API 수준 29) 이하에서 지원되지 않는다*/
        /*if(Build.VERSION.SDK_INT>29)
        {
            executor = ContextCompat.getMainExecutor(this);
            biometricPrompt = new BiometricPrompt(this,
                    executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode,
                                                  @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Toast.makeText(getApplicationContext(),
                                    R.string.auth_error_message, Toast.LENGTH_SHORT)
                            .show();
                }

                @Override
                public void onAuthenticationSucceeded(
                        @NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Toast.makeText(getApplicationContext(),
                            R.string.auth_success_message, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(getApplicationContext(), R.string.auth_fail_message,
                                    Toast.LENGTH_SHORT)
                            .show();
                }
            });
            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("지문 인증")
                    .setSubtitle("기기에 등록된 지문을 이용하여 지문을 인증해주세요.")
                    .setNegativeButtonText("취소")
                    .setAllowedAuthenticators(DEVICE_CREDENTIAL|BIOMETRIC_STRONG)
                    .build();
        }
        else if(Build.VERSION.SDK_INT>Build.VERSION_CODES.M){

        }*/

        //차량 문을 연다
        doorOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                bindServiceIntent = new Intent(context, CarControlService.class);
                bindServiceIntent.putExtra("mb_id", memberID);
                bindServiceIntent.putExtra("mac_address", carData.getMac_address());
                bindServiceIntent.putExtra("mode", CarControlService.DOOR_OPEN);
                bindService(bindServiceIntent, carControlServiceBindConnection, BIND_AUTO_CREATE);
            }
        });

        //차량 문을 닫는다
        doorClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                bindServiceIntent = new Intent(context, CarControlService.class);
                bindServiceIntent.putExtra("mb_id", memberID);
                bindServiceIntent.putExtra("mac_address", carData.getMac_address());
                bindServiceIntent.putExtra("mode", CarControlService.DOOR_CLOSE);
                bindService(bindServiceIntent, carControlServiceBindConnection, BIND_AUTO_CREATE);
            }
        });

        //차량 트렁크 문을 연다
        trunkOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                bindServiceIntent = new Intent(context, CarControlService.class);
                bindServiceIntent.putExtra("mb_id", memberID);
                bindServiceIntent.putExtra("mac_address", carData.getMac_address());
                bindServiceIntent.putExtra("mode", CarControlService.TRUNK_OPEN);
                bindService(bindServiceIntent, carControlServiceBindConnection, BIND_AUTO_CREATE);
            }
        });
        //차량 트렁크 문을 닫는다
        trunkClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                bindServiceIntent = new Intent(context, CarControlService.class);
                bindServiceIntent.putExtra("mb_id", memberID);
                bindServiceIntent.putExtra("mac_address", carData.getMac_address());
                bindServiceIntent.putExtra("mode", CarControlService.TRUNK_CLOSE);
                bindService(bindServiceIntent, carControlServiceBindConnection, BIND_AUTO_CREATE);
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
                        break;
                    case ArduinoBluetooth.FAILED_CONNECTION:
                        dialog.setText("연결 실패");
                        dialog.setTextColor(Color.parseColor("#F23920"));
                        break;
                    case CarControlService.SUCCESSFUL_CONTROL:
                        isAvailable.setText("운전중");
                        isAvailable.setTextColor(Color.parseColor("#9911BB"));
                        unbindService(carControlServiceBindConnection);
                        dialog.dismiss();
                        break;
                    case CarControlService.FAILED_CONTROL:
                        isAvailable.setText("운전 불가능");
                        isAvailable.setTextColor(Color.parseColor("#FF5544"));
                        unbindService(carControlServiceBindConnection);
                        dialog.dismiss();
                        break;
                }
            }
        });

    }
    private void carControlStateUpdateCallBack(String state){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch(state){
                    case ArduinoBluetooth.SEARCHING:
                        dialog.setText("기기 탐색중....");
                        dialog.setTextColor(Color.parseColor("#5DC19B"));
                        break;
                    case ArduinoBluetooth.FOUND_DEVICE:
                        dialog.setText("기기 연결중...");
                        dialog.setTextColor(Color.parseColor("#5DC19B"));
                        break;
                    case ArduinoBluetooth.SUCCESSFUL_CONNECTION:
                        dialog.setText("연결 성공");
                        dialog.setTextColor(Color.parseColor("#4488FF"));
                        break;
                    case ArduinoBluetooth.FAILED_CONNECTION:
                        dialog.setText("연결 실패");
                        dialog.setTextColor(Color.parseColor("#F23920"));
                        break;
                    case CarControlService.SUCCESSFUL_CONTROL:
                        unbindService(carControlServiceBindConnection);
                        dialog.dismiss();
                        break;
                    case CarControlService.FAILED_CONTROL:
                        unbindService(carControlServiceBindConnection);
                        dialog.dismiss();
                        break;
                }
            }
        });
    }
    @Override
    public void onBackPressed(){

        finish();
    }
    public void getPermission(){
        //Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        if(!(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN)==PackageManager.PERMISSION_GRANTED))
        {
            String[] permission_list = {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            };
            ActivityCompat.requestPermissions(this, permission_list, 1);
        }
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
}
