package com.example.carflix;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class JoinGroup extends AppCompatActivity {
    String memberID;
    EditText inviteCodeEditText;
    Button joinButton;
    //lwbgaylciu
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_group);
        memberID = getIntent().getStringExtra("memberID");
        connectUI();
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inviteCode = inviteCodeEditText.getText().toString();
                try{
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("ic_number", inviteCode);
                    requestBody.put("member", memberID);
                    ServerData serverData = new ServerData("POST", "code_car/create", requestBody);
                    String result = serverData.get();
                    Log.d("JoinGroup", result);
                    JSONObject resultJSON = new JSONObject(result);

                    String status = resultJSON.getString("status");
                    String group_id= resultJSON.getString("group_id");
                    String param = "_id=";
                    switch(status){
                        case"small_group": param = "sg"+param+group_id;break;
                        case"ceo_group":param = "cg"+param+group_id;break;
                        case"rent_group":param = "rg"+param+group_id;break;
                    }
                    serverData = new ServerData("GET", status+"/show", param, null);
                    Log.d("JoinGroup", serverData.get());
                }
                catch (JSONException e){
                    Log.e("JoinGroup", e.toString());
                }

            }
        });
    }
    private void connectUI(){
        inviteCodeEditText = findViewById(R.id.inviteCodeEditText);
        joinButton = findViewById(R.id.joinGroupButton);
    }
}
