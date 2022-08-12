package com.example.carflix;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class serverData {
    String category;
    JSONArray dataArray;
    serverData(String HTTPMethod, String command, JSONObject requestBody){
        switch(command.split("/")[0]){
            case "member":category = "mb";break;
            case "car":category="cr";break;
            case "small_group":category="sg";break;
            case "ceo_group":category="cg";break;
            case "rent_group":category="rg";break;
            case "invite_code":category="ic";break;

        }
        serverConnectionThread thread = new serverConnectionThread(HTTPMethod, command, requestBody);
        thread.start();
        try{
            thread.join();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        try{
            dataArray = new JSONObject(thread.getResult()).getJSONArray("data");
        }
        catch(JSONException e){
            Log.e("serverData", e.toString());
        }
    }
    public boolean contains(String Key, String value){
        int len = dataArray.length();
        Log.d("serverData", "len :: "+ len);
        try{
            for(int i = 0;i<len;i++){
                //thread.getResult().contains("\"mb_userid\":\""+useridEdit.getText()+"\"")
                if(dataArray.getJSONObject(i).get(Key).toString().equals(value)){
                    return true;
                }
            }
        }
        catch(JSONException e){
            return false;
        }
        return false;
    }
    public int getLastID(){
        int lastIndex = dataArray.length()-1;
        try{
            int result = dataArray.getJSONObject(lastIndex).getInt(category+"_id");
            return result;
        }
        catch(JSONException e){
            Log.e("serverData", e.toString());
            return -1;
        }
    }
}
