package com.example.carflix;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class carInterface extends AppCompatActivity {
    Button door;
    Button trunk;
    Button startCar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_interface);

        door = (Button)findViewById(R.id.door);
        trunk = (Button)findViewById(R.id.trunk);
        startCar = (Button)findViewById(R.id.startCar);
    }
}
