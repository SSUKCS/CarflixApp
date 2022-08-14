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
public class groupData implements Serializable {
    protected String groupID;
    protected String groupName;
    protected String groupDescription;
    protected String status;
    protected ArrayList<carData> carDataList;
    public groupData(String groupName, String groupDescription, String status){
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.status = status;

        carDataList = new ArrayList<>();
        //챠량 데이터 입력단(임의로 생성)
        int carimg_default = R.drawable.carimage_default;
        for(int i=1;i<11;i++){
            carDataList.add(new carData(carimg_default, groupName+"의 차량"+i));
        }
    }
    public groupData(String groupName, String groupDescription, String status, ArrayList<carData> carDataList){
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.status = status;
        this.carDataList = carDataList;
    }
    public groupData(JSONObject groupData){
        String groupStatus=null;
        try{
            switch(groupData.getString("status")){
                case "small_group":groupStatus = "sg";break;
                case "ceo_group":groupStatus =  "cg";break;
                case "rent_group":groupStatus = "rg";break;
            }
            Log.d("groupData", groupStatus);
            groupID = groupData.getString(groupStatus+"_id");
            groupName = groupData.getString(groupStatus+"_title");
            groupDescription = groupData.getString(groupStatus+"_description");
            status = groupData.getString("status");
        }
        catch(JSONException e){
            Log.e("groupData", e.toString());
        }
    }
    protected groupData(){
    }
    public String getGroupName(){
        return groupName;
    }
    public void setGroupname(String carName){
        this.groupName = carName;
    }
    public String getGroupDescription(){
        return groupDescription;
    }
    public void setGroupDescription(String groupDescription){this.groupDescription = groupDescription; }
    public String getStatus(){ return status; }
    public void setStatus(String status){ this.status = status;}
    public ArrayList<carData> getCarDataList(){return carDataList;}
    public void setCarDataList(ArrayList<carData> carDataList){this.carDataList = carDataList;}
}
