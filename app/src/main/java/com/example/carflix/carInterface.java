package com.example.carflix;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import androidx.appcompat.app.AppCompatActivity;

public class carInterface extends AppCompatActivity {
    private Context context;

    ImageView carImg;
    TextView carName;
    TextView isAvailable;

    Button doorOpen;
    Button doorClose;
    Button trunk;
    Button startCar;

    carData carData;

    boolean door_isOpen;
    boolean trunk_isOpen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_interface);

        context = getApplicationContext();

        carData = (carData)getIntent().getSerializableExtra("carData");

        carImg = (ImageView)findViewById(R.id.carImg);
        carName = (TextView)findViewById(R.id.carName);
        isAvailable = (TextView)findViewById(R.id.isAvailable);

        doorOpen = (Button)findViewById(R.id.doorOpen);
        doorClose = (Button)findViewById(R.id.doorClose);
        trunk = (Button)findViewById(R.id.trunk);
        startCar = (Button)findViewById(R.id.startCar);

        carImg.setImageResource(carData.getcarImg());
        carName.setText(carData.getCarName());
        if(carData.isAvailable()) {
            isAvailable.setText("운전 가능");
            isAvailable.setTextColor(Color.parseColor("#4488FF"));
        }
        else {
            isAvailable.setText("운전 불가능");
            isAvailable.setTextColor(Color.parseColor("#FF5544"));
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

        //차량에 시동을 건다.
        startCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
