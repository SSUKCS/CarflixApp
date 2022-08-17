package com.example.carflix;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class ServerData {
    private String category;
    private String purpose;
    JSONObject data;
    //dataArray = data.getJSONArray("data");

    ServerData(String HTTPMethod, String command, JSONObject requestBody){
        initCategory(command);
        ServerConnectionThread thread = new ServerConnectionThread(HTTPMethod, command, requestBody);
        start(thread);
    }
    ServerData(String HTTPMethod, String command, String params, JSONObject requestBody){
        initCategory(command);
        ServerConnectionThread thread = new ServerConnectionThread(HTTPMethod, command, params, requestBody);
        start(thread);
    }
    public String get(){
        String result = null;
        try{
            Log.d("serverData", category+"/"+purpose+" :: "+data.toString());
            switch(purpose){
                case "login_v3":
                    Log.d("serverData_get()", data.getString("message"));
                    result = data.getString("message");
                    if(result.equals("Successfully Login!")){
                        result = result +"/"+data.getString("mb_id");
                    }break;
                case "show_single_name":
                    Log.d("serverData_get()", data.getString("message"));
                    result = data.getString(category+"_userid");break;
                case "show":
                    if(!category.equals("ic")){//json 객체 1개를 return
                        result = data.getJSONArray("data").toString();break;
                    }
                case "group_show":
                case "group_info":
                    String JSONString = data.toString();
                    //{"message":" .... "} : 실패
                    if(Pattern.matches("^\\{\\\"message\\\"\\:\\\".*\\\"\\}$", JSONString)){
                        Log.d("serverData_get()", data.getString("message"));
                        Log.d("serverData_get()", "get Message......");
                        result = data.getString("message");
                    }
                    //{"data":[{" .... "}]} : 성공
                    else if(Pattern.matches("^\\{\\\"data\\\"\\:\\[\\{\\\".*\\\"\\}\\]\\}", JSONString)){
                        Log.d("serverData_get()", "get data......");
                        result = data.getJSONArray("data").toString();
                    }break;
                default: break;
            }
        }
        catch(JSONException e){
            Log.e("serverData_get()", e.toString());
        }
        Log.d("serverData", "SERVERDATA.GET :: "+result);
        return result;
    }
    private void initCategory(String command){
        String[] categoryAndPurpose = command.split("/");
        switch(categoryAndPurpose[0]){
            case "member":category = "mb";break;
            case "car":category="cr";break;
            case "small_group":category="sg";break;
            case "ceo_group":category="cg";break;
            case "rent_group":category="rg";break;
            case "invite_code":category="ic";break;
        }
        purpose = categoryAndPurpose[1];
    }
    private void start(ServerConnectionThread thread){
        thread.start();
        try{
            thread.join();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        try{
            data = new JSONObject(thread.getResult());
        }
        catch(JSONException e){
            Log.e("serverData", e.toString());
        }
    }
}
