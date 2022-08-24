package com.example.carflix;

import android.Manifest;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.regex.Pattern;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class AddCar extends AppCompatActivity {

    EditText carNameEdit;
    EditText carNumberEdit;
    TextView carIdServiceState;
    Button addCarButton;

    LoadingDialog dialog;

    private String memberID;
    private String groupID;
    private String status;

    private String macAddress;

    private CarIdService carIdService;
    private CarIdService.CarIdServiceCallback CarIdServiceCallback = this::CarIdStateUpdateCallback;
    private final ServiceConnection carIdServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i("CarInterface_carControlServiceBindConnection", "onServiceConnected: connected");
            CarIdService.CarServiceBinder carServiceBinder = (CarIdService.CarServiceBinder) iBinder;
            carIdService = carServiceBinder.getService();
            carIdService.registerCallback(CarIdServiceCallback);
            CarIdStateUpdateCallback(carIdService.getState());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i("CarInterface_CarInterface_carControlServiceBindConnection", "onServiceDisconnected: disconnected");
            carIdService = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_car);
        dialog = new LoadingDialog(this);
        getPermission();

        memberID = getIntent().getStringExtra("memberID");
        groupID = getIntent().getStringExtra("groupID");
        status = getIntent().getStringExtra("status");

        connectUI();
    }
    private void CarIdStateUpdateCallback(String state){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch(state){
                    case ArduinoBluetooth.SEARCHING:
                        dialog.show();
                        dialog.setText("기기 탐색중....");
                        dialog.setTextColor(Color.parseColor("#5DC19B"));
                        break;
                    case ArduinoBluetooth.FOUND_DEVICE:
                        dialog.show();
                        dialog.setText("기기 연결중...");
                        dialog.setTextColor(Color.parseColor("#5DC19B"));
                        break;
                    case ArduinoBluetooth.SUCCESSFUL_CONNECTION:
                        dialog.show();
                        dialog.setText("연결 성공.");
                        dialog.setTextColor(Color.parseColor("#9911BB"));
                        break;
                    case ArduinoBluetooth.FAILED_CONNECTION:
                        dialog.setText("차량과 연결이 실패하였습니다.");
                        dialog.setTextColor(Color.parseColor("#F23920"));
                        dialog.dismiss();
                        break;
                    case CarIdService.ASSIGN_OK:
                        dialog.show();
                        dialog.setText("아이디 할당 성공!");
                        unbindService(carIdServiceConnection);
                        dialog.dismiss();
                        break;
                }
            }
        });
    }
    private void connectUI(){
        getSupportActionBar().setTitle("차량 추가");

        carNameEdit = (EditText) findViewById(R.id.carNameEdit);
        carNumberEdit = (EditText)findViewById(R.id.carNumberEdit);
        carIdServiceState = (TextView)findViewById(R.id.carIdServiceState);
        addCarButton = (Button)findViewById(R.id.addCarButton);
        addCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //차량의 데이터를 서버에 등록한다.
                String carName = carNameEdit.getText().toString();
                String carNumber = carNumberEdit.getText().toString().replaceAll("\\s", "");
                //차량 번호
                //예) 12 가 1234 or 12가 1234 or 12 가1234 or 12가1234
                boolean debug_mode_l = true;
                if(debug_mode_l ||
                        (!editTextIsEmpty()&&Pattern.matches("^\\d{2,3}\\s*[가-힣]\\s*\\d{4}$", carNumber))){
                    int len = carNumber.length();
                    //블루투스 페어링이 가능한 차량 탐색
                    //등록할 차량의 macAddress를 가져온다.
                    Intent bindServiceIntent = new Intent(getApplicationContext(), CarIdService.class);
                    bindServiceIntent.putExtra("mb_id", memberID);
                    bindServiceIntent.putExtra("group_id", groupID);
                    bindServiceIntent.putExtra("status", status);
                    bindServiceIntent.putExtra("numberClassification", carNumber.substring(0, len-4));
                    bindServiceIntent.putExtra("registerationNum", carNumber.substring(len-4));
                    bindServiceIntent.putExtra("carName", carName);
                    bindServiceIntent.putExtra("mode", CarIdService.ASSIGN_MODE);
                    bindService(bindServiceIntent, carIdServiceConnection, BIND_AUTO_CREATE);
                }
                else{
                    Log.d("AddCar", "editTextIsEmpty :: "+editTextIsEmpty());
                    Log.d("AddCar", "Pattern matches :: "+Pattern.matches("^\\d{2,3}[가-힣]\\d{4}$", carNumber));
                }
            }
        });
    }
    private boolean editTextIsEmpty(){
        //둘중 하나라도 0일경우 true, 아니면 false
        return (carNumberEdit.length()==0||carNameEdit.length()==0);
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void getPermission(){
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN)==PackageManager.PERMISSION_GRANTED){
        }
        // 권한이 없을 경우 권한을 요구함
        else {
            String[] permission_list = {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
            };
            ActivityCompat.requestPermissions(this, permission_list, 1);
        }
    }
}
