package com.example.carflix;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
public class carList extends AppCompatActivity {

    private Context context;

    private ArrayList<carData> carDataList;
    private carListAdapter adapter;
    private RecyclerView carList;


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
       
        adapter.setItemClickListener(new carListAdapter.itemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                //carInterface로 이동
                Intent intent = new Intent(getApplicationContext(), carInterface.class);
                carData carData = carDataList.get(position);
                intent.putExtra("carData", carData);
                startActivity(intent);
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
        }
        return super.onOptionsItemSelected(item);
    }
}
