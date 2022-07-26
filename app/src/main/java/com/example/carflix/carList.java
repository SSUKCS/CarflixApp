package com.example.carflix;

import android.content.Context;
import android.os.Bundle;

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
        carList = findViewById(R.id.recyclerView);
        //레이아웃메니저: 리사이클러뷰의 항목배치/스크롤 동작을 설정
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        carList.setLayoutManager(layoutManager);

        carDataList = new ArrayList<>();
        adapter = new carListAdapter(context, carDataList);
        carList.setAdapter(adapter);
        //챠량 데이터 입력단(임의로 생성)
        for(int i=1;i<4;i++){
            carDataList.add(new carData(carImg_default, "차량"+i));
            adapter.notifyItemInserted(carDataList.size()-1);
        }
    }

}
