package com.example.carflix;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Executor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
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
    Button trunk;
    Button startCar;

    CarData carData;

    boolean carData_isAvailable_initialState;
    boolean door_isOpen=false;
    boolean trunk_isOpen=false;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_interface);

        context = getApplicationContext();
        position = getIntent().getIntExtra("position", -1);
        carData = (CarData)getIntent().getSerializableExtra("carData");

        connectUI();

        carImg.setImageResource(carData.getcarImg());
        carName.setText(carData.getCarName());
        carData_isAvailable_initialState = carData.isAvailable();
        isAvailable.setText(carData.getStatus());
        switch(carData.getStatus())
        {
            case"운전 가능":isAvailable.setTextColor(Color.parseColor("#4488FF"));break;
            case"운전 불가능":isAvailable.setTextColor(Color.parseColor("#FF5544"));break;
            case"운전중":isAvailable.setTextColor(Color.parseColor("#9911BB"));break;
        }



        /*DEVICE_CREDENTIAL 및 BIOMETRIC_STRONG | DEVICE_CREDENTIAL 인증자 유형 조합은
        Android 10(API 수준 29) 이하에서 지원되지 않는다*/
        if(Build.VERSION.SDK_INT>29)
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

        }

        //차량 문을 연다
        doorOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //차량 문을 닫는다
        doorClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //차량 트렁크 문을 연다(option)
        trunk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //차량에 시동을 건다/시동이 걸려있으면 끈다
        startCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPermission();
                biometricPrompt.authenticate(promptInfo);
                if(carData.isAvailable())
                {
                    carData.setAvailable(false);
                    isAvailable.setText("운전중");
                    isAvailable.setTextColor(Color.parseColor("#9911BB"));

                    Intent intent = new Intent(getApplicationContext(), LocationService.class);
                    startService(intent);
                }
                else if(isAvailable.getText().equals("운전 불가능")){
                    Toast.makeText(context,"운전할 수 없습니다.",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    carData.setAvailable(true);
                    isAvailable.setText("운전 가능");
                    isAvailable.setTextColor(Color.parseColor("#4488FF"));

                    Intent intent = new Intent(getApplicationContext(), LocationService.class);
                    Log.e("carInterface", "STOP CONTEXT "+getApplicationContext());
                    stopService(intent);
                }
            }
        });
    }
    @Override
    public void onBackPressed(){
        if(carData_isAvailable_initialState != carData.isAvailable())
        {
            Intent intent = new Intent();
            intent.putExtra("position", Integer.toString(position));
            intent.putExtra("carData_isAvailableChanged", carData.isAvailable());
            intent.putExtra("carStatusChanged", isAvailable.getText());
            setResult(9001, intent);
        }
        finish();
    }
    public void getPermission(){
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN)==PackageManager.PERMISSION_GRANTED){
        }
        // 권한이 없을 경우 권한을 요구함
        else {
            final String requiredPermission[] = {android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.BLUETOOTH_SCAN};
            ActivityCompat.requestPermissions(this, requiredPermission, 1
            );
        }
    }
    private void connectUI(){
        carImg = (ImageView)findViewById(R.id.carImg);
        carName = (TextView)findViewById(R.id.carName);
        isAvailable = (TextView)findViewById(R.id.isAvailable);

        doorOpen = (Button)findViewById(R.id.doorOpen);
        doorClose = (Button)findViewById(R.id.doorClose);
        trunk = (Button)findViewById(R.id.trunk);
        startCar = (Button)findViewById(R.id.startCar);
    }
}
