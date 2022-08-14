package com.example.carflix;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class serverData {
    private String category;
    private String purpose;
    JSONObject data;
    //dataArray = data.getJSONArray("data");

    serverData(String HTTPMethod, String command, JSONObject requestBody){
        initCategory(command);
        serverConnectionThread thread = new serverConnectionThread(HTTPMethod, command, requestBody);
        start(thread);
    }
    serverData(String HTTPMethod, String command, String params, JSONObject requestBody){
        initCategory(command);
        serverConnectionThread thread = new serverConnectionThread(HTTPMethod, command, params, requestBody);
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
                case "group_info":
                    String JSONString = data.toString();
                    //{"message":" .... "}
                    if(Pattern.matches("^\\{\\\"message\\\"\\:\\\".*\\\"\\}$", JSONString)){
                        Log.d("serverData_get()", data.getString("message"));
                        Log.d("serverData_get()", "get Message......");
                        result = data.getString("message");
                    }
                    //{"data":[{" .... "}]}
                    else if(Pattern.matches("^\\{\\\"data\\\"\\:\\[\\{\\\".*\\\"\\}\\]\\}", JSONString)){
                        Log.d("serverData_get()", "get data......");
                        result = data.getJSONArray("data").toString();
                    }
                    ;break;
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
    private void start(serverConnectionThread thread){
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
