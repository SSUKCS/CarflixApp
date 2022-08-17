package com.example.carflix;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class InviteCode {
    private String groupID;
    private String groupName;
    private String status;
    private String code;
    private String regDate;

    public InviteCode(JSONObject inviteCode){
        try{
            groupID = inviteCode.getString("group_id");
            groupName = inviteCode.getString("group_name");
            status = inviteCode.getString("status");
            code = inviteCode.getString("ic_number");
            regDate = inviteCode.getString("ic_regdate");
        }
        catch(JSONException e){
            Log.e("InviteCode", e.toString());
        }
    }
    public String getGroupName(){return groupName;}
    public String getStatus() {return status;}
    public String getCode() {return code;}
    public String getRegDate() {return regDate;}
}
