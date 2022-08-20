package com.example.carflix;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AddCar extends AppCompatActivity {

    EditText carNameEdit;
    EditText carNumberEdit;
    Button addCarButton;

    private String memberID;
    private String groupID;
    private String status;

    private String macAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_car);

        memberID = getIntent().getStringExtra("memberID");
        groupID = getIntent().getStringExtra("groupID");
        status = getIntent().getStringExtra("status");

        connectUI();
    }
    private void connectUI(){
        carNameEdit = (EditText) findViewById(R.id.carNameEdit);
        carNumberEdit = (EditText)findViewById(R.id.carNumberEdit);
        addCarButton = (Button)findViewById(R.id.addCarButton);
        addCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //블루투스 페어링이 가능한 차량 탐색
                //등록할 차량의 macAddress를 가져온다.


                //차량의 데이터를 서버에 등록한다.
                String carName = carNameEdit.getText().toString();
                String carNumber = carNumberEdit.getText().toString().replaceAll("\\s", "");

                JSONObject carData = new JSONObject();
                //차량 번호
                //예) 12 가 1234 or 12가 1234 or 12 가1234 or 12가1234
                if(!editTextIsEmpty()&&Pattern.matches("^\\d{2,3}[가-힣]\\d{4}$", carNumber)){
                    int len = carNumber.length();
                    try{

                        carData.put("mb_id", memberID);
                        carData.put("group_id", groupID);
                        carData.put("status", status);
                        carData.put("cr_number_classification", carNumber.substring(0, len-4));
                        carData.put("cr_registeration_number", carNumber.substring(len-4));
                        carData.put("cr_carname", carName);
                        carData.put("cr_mac_address", macAddress);
                    }
                    catch(JSONException e){
                        Log.e("addCar_addCarButton", e.toString());
                    }
                    ServerConnectionThread connectionThread = new ServerConnectionThread("POST", "car/create", carData);
                    connectionThread.start();
                }
            }
        });
    }
    private boolean editTextIsEmpty(){
        //둘중 하나라도 0일경우 true, 아니면 false
        return (carNumberEdit.length()==0||carNameEdit.length()==0);
    }
}
