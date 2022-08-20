package com.example.carflix;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class MainActivity2 extends AppCompatActivity {/*
    private final static int REQUEST_ENABLE_BT = 1;
    private final static String TAG = "abcdefg";
    private final static UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    private TextView textStatus;
    private Button btnConnect;

    private Intent startServiceIntent;
    private Intent bindServiceIntent;


    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get permission
        String[] permission_list = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
        };
        ActivityCompat.requestPermissions(MainActivity2.this, permission_list, 1);

        // Enable bluetooth
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                Toast.makeText(getApplicationContext(), "블루투스 권한을 허용해주세요.", Toast.LENGTH_LONG).show();
                finish();
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // variables
        textStatus = findViewById(R.id.text_status);
        btnConnect = findViewById(R.id.btn_connect);
    }
    private void renewTextStatus(String state){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch(state){
                    case ArduinoBluetooth.SEARCHING:
                        textStatus.setText("기기 탐색중");
                        break;
                    case ArduinoBluetooth.FOUND_DEVICE:
                        textStatus.setText("기기 연결중");
                        break;
                    case ArduinoBluetooth.SUCCESSFUL_CONNECTION:
                        textStatus.setText("연결 성공");
                        break;
                    case ArduinoBluetooth.FAILED_CONNECTION:
                        textStatus.setText("연결 실패");
                        break;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(startServiceIntent != null) {
            bindServiceIntent = new Intent(this, CarTracingService.class);
            bindService(bindServiceIntent, bindConnection, BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bindServiceIntent != null) {
            unbindService(bindConnection);
            bindServiceIntent = null;
        }
    }

    private CarTracingService carTracingService;
    private final CarTracingService.StateUpdateCallBack stateUpdateCallBack = this::renewTextStatus;
    private final ServiceConnection bindConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(TAG, "onServiceConnected: connected");
            CarTracingService.CarServiceBinder carServiceBinder = (CarTracingService.CarServiceBinder) iBinder;
            carTracingService = carServiceBinder.getService();
            carTracingService.registerCallback(stateUpdateCallBack);
            renewTextStatus(carTracingService.getState());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "onServiceDisconnected: disconnected");
            carTracingService = null;
        }
    };

    public void onClickConnect(View view){

        if(startServiceIntent == null) {
            textStatus.setText("블루투스 시작중");

            startServiceIntent = new Intent(this, CarTracingService.class);
            startServiceIntent.putExtra("car_name", "회사차");
            startServiceIntent.putExtra("mb_id", "12345");
            //startServiceIntent.putExtra("mac_address", "no");
            startService(startServiceIntent);

            bindServiceIntent = new Intent(this, CarTracingService.class);
            bindService(bindServiceIntent, bindConnection, BIND_AUTO_CREATE);
        }


        if(timerTask == null) {
            timerTask = new TimerTask() {
                int count;

                @Override
                public void run() {
                    Log.i(TAG, "run: test" + ++count);
                }
            };
            timer = new Timer();
            Log.i(TAG, "onClickConnect: start");
            timer.schedule(timerTask, 3000, 3000);
        }
        else{
            timerTask.cancel();
            Log.i(TAG, "onClickConnect: canceled");
            timerTask = null;
        }
    }
    TimerTask timerTask;
    Timer timer;
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }*/
}
