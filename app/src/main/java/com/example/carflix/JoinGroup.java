package com.example.carflix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class JoinGroup extends AppCompatActivity {
    String memberID;
    EditText inviteCodeEditText;
    Button joinButton;
    //n0aooo6ytk
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_group);
        getSupportActionBar().setTitle("그룹 가입");
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
                    JSONObject resultJSON = new JSONObject(serverData.get());
                    Log.d("JoinGroup", "resultJSON :: "+resultJSON);
                    // 성공: {"message":"group invited","mb_id":"29","group_id":"34","status":"ceo_group"}
                    // 실패: {"message":"group not invited"}
                    String message = resultJSON.getString("message");
                    if(message.equals("group invited")){
                        String group_id= resultJSON.getString("group_id");
                        String status = resultJSON.getString("status");
                        String groupStatus = "";
                        switch(status){
                            case"small_group": groupStatus = "sg_id";break;
                            case"ceo_group":groupStatus = "cg_id";break;
                            case"rent_group":groupStatus = "rg_id";break;
                        }
                        serverData = new ServerData("GET", status+"/show", groupStatus+"="+group_id, null);
                        //성공 : Connected successfully
                        if(!new JSONObject(serverData.get()).get("mb_id").equals("")){
                            resultJSON.remove("message");
                            resultJSON.remove("status");
                            resultJSON.put("ic_number", inviteCode);
                            String saveData =resultJSON.toString();
                            Log.d("JoinGroup", "saveData :: "+saveData);
                            saveGroupData(status, saveData);
                        }
                        else{
                            //모종의 이유(예: 그룹이 삭제)로 그룹데이터가 사용 불가능
                            Toast.makeText(getApplicationContext(), "존재하지 않는 그룹입니다.", Toast.LENGTH_LONG);
                            Log.d("JoinGroup", "그룹이 존재하지 않습니다.");
                        }
                    }
                    else{
                        Log.d("JoinGroup", message);
                    }

                }
                catch (JSONException e){
                    Log.e("JoinGroup", e.toString());
                }
                finish();
            }
        });
    }
    private void saveGroupData(String status, String saveData){
        /*
         * savedInviteGroupData  -   small_group:[{"mb_id":"....","group_id":"....","status":"...."}, ....],
         *                           ceo_group:[{"mb_id":"....","group_id":"....","status":"...."}, ....],
         *                           rent_group:[{"mb_id":"....","group_id":"....","status":"...."}, ....]
         */
        SharedPreferences savedInviteGroupData = getSharedPreferences(getString(R.string.invite_Group_Data), MODE_PRIVATE);
        String savedGroupJSONArrayString = "";
        switch(status){
            case"small_group":
                savedGroupJSONArrayString = savedInviteGroupData.getString(getString(R.string.smallGroupKey), getString(R.string.groupDataKey_noValue));
                break;
            case"ceo_group":
                savedGroupJSONArrayString = savedInviteGroupData.getString(getString(R.string.ceoGroupKey), getString(R.string.groupDataKey_noValue));
                break;
            case"rent_group":
                savedGroupJSONArrayString = savedInviteGroupData.getString(getString(R.string.rentGroupKey), getString(R.string.groupDataKey_noValue));
                break;
        }

        Log.d("JoinGroup_saveGroupData", "BEFORE :: "+savedGroupJSONArrayString);
        //저장되어 있는 값이 존재 안함
        if(!savedGroupJSONArrayString.contains(saveData)){
            try{
                JSONArray savedGroupJSONArray;
                if(savedGroupJSONArrayString.equals(getString(R.string.groupDataKey_noValue)))
                    savedGroupJSONArray = new JSONArray();
                else//저장되어 있는 값이 존재하지 않음
                    savedGroupJSONArray = new JSONArray(savedGroupJSONArrayString);

                JSONObject saveJSONObject = new JSONObject(saveData);
                savedGroupJSONArray.put(saveJSONObject);
                Log.d("JoinGroup_saveGroupData", "AFTER :: "+savedGroupJSONArray);
                //SharedPreferences에 저장
                SharedPreferences.Editor editor = savedInviteGroupData.edit();
                switch(status){
                    case"small_group":
                        editor.putString(getString(R.string.smallGroupKey), savedGroupJSONArray.toString());
                        break;
                    case"ceo_group":
                        editor.putString(getString(R.string.ceoGroupKey), savedGroupJSONArray.toString());
                        break;
                    case"rent_group":
                        editor.putString(getString(R.string.rentGroupKey), savedGroupJSONArray.toString());
                        break;
                }
                editor.apply();
            }
            catch(JSONException e){
                Log.e("JoinGroup_saveGroupData", e.toString());
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "이미 가입되어있는 그룹입니다.", Toast.LENGTH_LONG).show();
            Log.d("JoinGroup_saveGroupData", "이미 가입되어있는 그룹입니다.");
        }
    }
    private void connectUI(){
        inviteCodeEditText = findViewById(R.id.inviteCodeEditText);
        joinButton = findViewById(R.id.joinGroupButton);
    }
}
