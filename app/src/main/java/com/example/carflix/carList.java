package com.example.carflix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
public class carList extends AppCompatActivity {

    private Context context;

    private ArrayList<carData> carDataList;
    private carListAdapter adapter;
    private RecyclerView carList;

    private final static int DEFAULT = -1;
    private int nowDriving = -1;

    int carImg_default = R.drawable.carimage_default;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_list);
        context = getApplicationContext();
        carList = findViewById(R.id.carListView);
        //레이아웃메니저: 리사이클러뷰의 항목배치/스크롤 동작을 설정
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        carList.setLayoutManager(layoutManager);
        carDataList = (ArrayList<carData>) getIntent().getSerializableExtra("carDataList");
        adapter = new carListAdapter(context, carDataList);
        carList.setAdapter(adapter);
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                data -> {
                    Log.d("carList", "data : " + data);
                    switch (data.getResultCode())
                    {
                        //carInterface->carList
                        case 9001:
                        Intent intent = data.getData();

                        boolean isAvailableChanged = intent.getBooleanExtra("carData_isAvailableChanged", true);
                        int position = Integer.parseInt(intent.getStringExtra("position"));
                        if(isAvailableChanged)nowDriving = DEFAULT;
                        else nowDriving = position;
                        String carStatus = intent.getStringExtra("carStatusChanged");

                        Log.d("carList", "isAvailableChanged : " + isAvailableChanged);
                        Log.d("carList", "position : " + position);
                        Log.d("carList", "carStatusChanged : " + carStatus);

                        carDataList.get(position).setAvailable(isAvailableChanged);
                        carDataList.get(position).setStatus(carStatus);
                        adapter.notifyItemChanged(position);break;

                        //carLookupInfo->carList
                        case 9002: break;

                        //addCar -> break;
                        case 9003: break;
                        default:break;
                    }
                });
        adapter.setItemClickListener(new carListAdapter.itemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                //carInterface로 이동
                if(nowDriving == DEFAULT || nowDriving==position){
                    Intent intent = new Intent(getApplicationContext(), carInterface.class);
                    carData carData = carDataList.get(position);
                    intent.putExtra("carData", carData);
                    intent.putExtra("position", position);
                    launcher.launch(intent);
                }
                else{
                    Toast.makeText(context, "차량 운전중입니다.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onLookupInfoClick(View v, int position) {
                //lookupInfo
                Intent intent = new Intent(getApplicationContext(), carLookupInfo.class);
                startActivity(intent);
            }
        });
        
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_car_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int curId = item.getItemId();

        switch(curId){
            case R.id.addCar:
                Toast.makeText(this, "차량 추가", Toast.LENGTH_LONG).show();
                break;
            case R.id.generateCode:
                Toast.makeText(this, "코드 생성", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), generateCode.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed(){
        /*
        if(carData_isAvailable_initialState != carData.isAvailable())
        {
            Intent intent = new Intent();
            intent.putExtra("position", Integer.toString(position));
            intent.putExtra("carData_isAvailableChanged", carData.isAvailable());
            intent.putExtra("carStatusChanged", isAvailable.getText());
            setResult(RESULT_OK, intent);
        }
        finish();
        */
    }
}
