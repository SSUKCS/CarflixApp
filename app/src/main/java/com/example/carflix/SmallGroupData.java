package com.example.carflix;

import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

//small_group data class
//{
//    "sg_id":,
//    "sg_title":,
//    "sg_description":,
//    "status":
//}
public class SmallGroupData implements Serializable {
    protected String creatorID;
    protected String groupID;
    protected String groupName;
    protected String groupDescription;
    protected String status;

    public SmallGroupData(JSONObject groupData){
        String groupStatus=null;
        try{
            switch(groupData.getString("status")){
                case "small_group":groupStatus = "sg";break;
                case "ceo_group":groupStatus =  "cg";break;
                case "rent_group":groupStatus = "rg";break;
            }
            Log.d("groupData", groupStatus);
            creatorID = groupData.getString("mb_id");
            groupID = groupData.getString(groupStatus+"_id");
            groupName = groupData.getString(groupStatus+"_title");
            groupDescription = groupData.getString(groupStatus+"_description");
            status = groupData.getString("status");
        }
        catch(JSONException e){
            Log.e("groupData", e.toString());
        }
    }
    protected SmallGroupData(){
    }
    protected JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("creatorID", creatorID);
            jsonObject.put("groupID", groupID);
            jsonObject.put("groupName", groupName);
            jsonObject.put("groupDescription", groupDescription);
            jsonObject.put("status", status);
        }
        catch(JSONException e){
            Log.d(getClass().getSimpleName(), e.toString());
        }
        return jsonObject;
    }
    public String toJSONString(){
        return this.toJSONObject().toString();
    }
    public String getCreatorID(){return creatorID;}
    public String getGroupID() {return groupID;}
    public String getStatus(){ return status; }

    public String getGroupName(){
        return groupName;
    }
    public void setGroupName(String carName){
        this.groupName = carName;
    }

    public String getGroupDescription(){
        return groupDescription;
    }
    public void setGroupDescription(String groupDescription){this.groupDescription = groupDescription; }

}
