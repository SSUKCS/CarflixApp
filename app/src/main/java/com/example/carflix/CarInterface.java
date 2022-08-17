package com.example.carflix;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    Button trunk;
    Button startCar;

    CarData carData;

    boolean carData_isAvailable_initialState;
    boolean door_isOpen;
    boolean trunk_isOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_interface);

        context = getApplicationContext();
        position = getIntent().getIntExtra("position", -1);
        carData = (CarData)getIntent().getSerializableExtra("carData");

        carImg = (ImageView)findViewById(R.id.carImg);
        carName = (TextView)findViewById(R.id.carName);
        isAvailable = (TextView)findViewById(R.id.isAvailable);

        doorOpen = (Button)findViewById(R.id.doorOpen);
        doorClose = (Button)findViewById(R.id.doorClose);
        trunk = (Button)findViewById(R.id.trunk);
        startCar = (Button)findViewById(R.id.startCar);

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


        door_isOpen = false;
        trunk_isOpen = false;

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
}
