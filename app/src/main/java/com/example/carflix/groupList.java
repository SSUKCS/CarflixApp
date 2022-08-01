package com.example.carflix;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class groupList extends AppCompatActivity {

    private Context context;

    private ArrayList<groupData> groupDataList;
    private groupListAdapter adapter;
    private RecyclerView groupList;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_list);
        context = getApplicationContext();
        groupList = findViewById(R.id.groupListView);
        //레이아웃메니저: 리사이클러뷰의 항목배치/스크롤 동작을 설정
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        groupList.setLayoutManager(layoutManager);
        groupDataList = new ArrayList<>();
        adapter = new groupListAdapter(context, groupDataList);
        groupList.setAdapter(adapter);
       
        
        //회사 데이터 입력단(임의로 생성)
        groupDataList.add(new groupData("우리 가족", "소규모 가족 그룹", "소규모"));
        adapter.notifyItemInserted(groupDataList.size()-1);
        groupDataList.add(new groupData("A회사", "대규모 회사 그룹", "대규모"));
        adapter.notifyItemInserted(groupDataList.size()-1);
       
        adapter.setItemClickListener(new groupListAdapter.itemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent(getApplicationContext(), carList.class);
                startActivity(intent);
            }
        });
    }
}
