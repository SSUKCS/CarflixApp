package com.example.carflix;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GenerateCode extends AppCompatActivity {

    Context context;
    TextView groupTitle;
    TextView groupStatus;

    private ArrayList<InviteCode> inviteCodeList;
    private InviteCodeListAdapter adapter;
    RecyclerView inviteCodeListView;
    TextView listEmpty;

    Button generateCodeButton;
    private String memberID;
    private String groupID;
    private String groupName;
    private String status;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generate_code);
        context = getApplicationContext();

        groupTitle = findViewById(R.id.groupTitle);
        groupStatus = findViewById(R.id.status);

        inviteCodeListView = findViewById(R.id.inviteCodeListView);
        //레이아웃메니저: 리사이클러뷰의 항목배치/스크롤 동작을 설정
        inviteCodeListView.setLayoutManager(new LinearLayoutManager(this));

        memberID = getIntent().getStringExtra("memberID");
        groupID = getIntent().getStringExtra("groupID");
        groupName = getIntent().getStringExtra("groupName");
        status = getIntent().getStringExtra("status");

        groupTitle.setText(groupName);
        switch(status){
            case "small_group":groupStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.sg_color));break;
            case "ceo_group":groupStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.cg_color));break;
            case "rent_group":groupStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.rg_color));break;
        }
        groupStatus.setText(status);

        inviteCodeList = new ArrayList<InviteCode>();
        updateListfromServer();

        adapter = new InviteCodeListAdapter(this, inviteCodeList);
        inviteCodeListView.setAdapter(adapter);

        listEmpty = findViewById(R.id.list_empty);
        if(inviteCodeList.isEmpty())listEmpty.setVisibility(View.VISIBLE);
        else listEmpty.setVisibility(View.GONE);

        generateCodeButton = findViewById(R.id.generateCodeButton);
        generateCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generate();
            }
        });
    }
    private void generate(){
        String result = null;
        //서버로부터 코드를 받아온다.
        String command = "invite_code/create";
        try{
            JSONObject requestBody = new JSONObject();
            requestBody.put("mb_id", memberID);
            requestBody.put("group_id", groupID);
            requestBody.put("status", status);
            Log.d("generateCode", "JSONObject :: "+requestBody);
            //1. 코드를 생성한다
            ServerConnectionThread thread = new ServerConnectionThread("POST", command, requestBody);
            thread.start();
            try{
                thread.join();
            }
            catch(InterruptedException e){
                Log.e("generateCode_generate()_thread.join()", e.toString());
            }
            //생성한 코드를 포함한 지금까지 만들어온 코드들을 서버로부터 전부 받아온다.
            Log.d("generateCode_generate()", thread.getResult());
            updateListfromServer();
            adapter.notifyItemInserted(0);
            inviteCodeListView.scrollToPosition(0);
        }
        catch(JSONException e){
            Log.e("generateCode_generate()", e.toString());
        }

    }
    private void updateListfromServer(){
        inviteCodeList.clear();
        ServerData serverData = new ServerData("GET", "invite_code/show", "mb_id="+memberID, null);
        Log.d("generateCode_generate()", "SERVERDATA :: "+serverData.get());
        try {
            JSONArray jsonArray = new JSONArray(serverData.get());
            int len = jsonArray.length();

            String groupID = getIntent().getStringExtra("groupID");
            String status = getIntent().getStringExtra("status");

            for(int i=0;i<len;i++){
                JSONObject inviteCodeData = jsonArray.getJSONObject(i);
                if(inviteCodeData.getString("group_id").equals(groupID)&&
                        inviteCodeData.getString("status").equals(status)){
                    inviteCodeData.put("group_name", groupName);
                    inviteCodeList.add(new InviteCode(inviteCodeData));
                    Log.d("generateCode", "RESULT["+i+"] :: "+inviteCodeList.get(i).getCode());
                }
            }
            Log.d("generateCode", "SIZE :: "+inviteCodeList.size());
        }
        catch(JSONException e){
            Log.e("generateCode_updateListfromServer()", e.toString());
        }
    }
}

