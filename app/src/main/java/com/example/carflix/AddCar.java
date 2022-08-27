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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_car);
        dialog = new LoadingDialog(this);
        dialog.registerBackPressed(new LoadingDialog.DialogBackPressed() {
            @Override
            public void onBackPressed() {
                if(carIdManager != null){
                    carIdManager.endConnection();
                    carIdManager = null;
                    Toast.makeText(getApplicationContext(), "취소됨.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        getPermission();

        memberID = getIntent().getStringExtra("memberID");
        groupID = getIntent().getStringExtra("groupID");
        status = getIntent().getStringExtra("status");

        connectUI();
    }
    
    private class CarIdManagerCallback implements CarIdManager.CarIdManagerCallback {
        @Override
        public void onStateUpdate(String state) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch(state){
                        case ArduinoBluetooth.FOUND_DEVICE:
                            Log.d("ArduinoBluetooth", ArduinoBluetooth.FOUND_DEVICE);
                            dialog.setText("기기 연결중...");
                            dialog.setTextColor(Color.parseColor("#5DC19B"));
                            break;
                        case ArduinoBluetooth.SUCCESSFUL_CONNECTION:
                            Log.d("ArduinoBluetooth", ArduinoBluetooth.SUCCESSFUL_CONNECTION);
                            dialog.setText("아이디 할당중...");
                            dialog.setTextColor(Color.parseColor("#9911BB"));
                            break;
                        case ArduinoBluetooth.FAILED_CONNECTION:
                            Log.d("ArduinoBluetooth", ArduinoBluetooth.FAILED_CONNECTION);
                            carIdManager.endConnection();
                            carIdManager = null;
                            dialog.setText("차량과 연결이 실패하였습니다.");
                            dialog.setTextColor(Color.parseColor("#F23920"));
                            dialog.dismiss();
                            break;
                        case CarIdManager.ASSIGN_OK:
                            Log.d("ArduinoBluetooth", CarIdManager.ASSIGN_OK);
                            addCarButton.setEnabled(false);
                            carIdManager.endConnection();
                            carIdManager = null;
                            dialog.setText("아이디 할당 성공!");
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), "할당 성공", Toast.LENGTH_SHORT).show();
                            finish();
                            break;
                    }
                }
            });
        }

        @Override
        public void onConnectFailed() { }

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
    private CarIdManager carIdManager;
    
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
                //boolean debug_mode_l = true;
                if((!editTextIsEmpty()&&Pattern.matches("^\\d{2,3}\\s*[가-힣]\\s*\\d{4}$", carNumber))){
                    int len = carNumber.length();
                    //블루투스 페어링이 가능한 차량 탐색
                    //등록할 차량의 macAddress를 가져온다.
                    CarIdManager.Userdata userdata = new CarIdManager.Userdata(
                            memberID, groupID, status, carNumber.substring(0, len-4), carNumber.substring(len-4), carName
                    );
                    
                    carIdManager = new CarIdManager(getApplicationContext(), 
                            BluetoothAdapter.getDefaultAdapter(), new CarIdManagerCallback(), 
                            userdata, CarIdManager.ASSIGN_MODE);
                    carIdManager.start();
                    dialog.show();
                    dialog.setText("기기 탐색중....");
                    dialog.setTextColor(Color.parseColor("#5DC19B"));
                    
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
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Log.d("AddCar", "dialog.isShowing()"+dialog.isShowing());
        if(carIdManager != null){
            //carIdManager.endConnection();
        }
        if(dialog.isShowing())dialog.cancel();
    }
}
