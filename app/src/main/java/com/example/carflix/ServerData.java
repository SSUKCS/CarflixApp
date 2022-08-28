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
        //13.56.94.107/admin/api/vehicle_status/boot_on.php?cr_id=3&mb_id=4
        String result = null;
        try{
            Log.d("serverData", category+"/"+purpose+" :: "+data.toString());
            switch(purpose){
                case "boot_on":
                    result = data.toString();
                    break;
                case "create"://mb, sg, cg, rg, ic : {message:}, cr:{message:, cr_id:}, cc:{message, mb_id:, group_id:, status:}
                    if(category.equals("cr")){
                        Log.d("serverData_"+category+"id", data.getString(category+"_id"));
                        result = data.getString(category+"_id");
                    }
                    else if(category.equals("cc")){
                        //그룹을 생성한 사람 id/그룹id/그룹status를 return
                        result = data.toString();
                        Log.d("serverData_"+category+"_"+purpose, result);
                    }
                    break;
                case "group_show":
                case "group_info":
                    //{"message":" .... "} : 실패
                    if(Pattern.matches("^\\{\\\"message\\\"\\:\\\".*\\\"\\}$", data.toString())){
                        Log.d("serverData_message", data.getString("message"));
                        result = data.getString("message");
                    }
                    //{"data":[{" .... "}]} : 성공
                    else if(Pattern.matches("^\\{\\\"data\\\"\\:\\[\\{\\\".*\\\"\\}\\]\\}", data.toString())){
                        Log.d("serverData_data", "get data......");
                        result = data.getJSONArray("data").toString();
                    }break;
                case "login_v3":
                    Log.d("serverData_message", data.getString("message"));
                    result = data.toString();
                    break;
                case "registration_delete_request":
                    Log.d("serverData_message", data.getString("message"));
                    if(data.getString("message").equals("car delete request success")){
                        result = data.toString();
                    }
                    else{
                        result = data.getString("message");
                    }break;
                case "show":
                    if(category.equals("ic")||category.equals("vs")){
                        //{"message":" .... "} : 실패
                        if(Pattern.matches("^\\{\\\"message\\\"\\:\\\".*\\\"\\}$", data.toString())){
                            Log.d("serverData_message", data.getString("message"));
                            result = data.getString("message");
                        }
                        //{"data":[{" .... "}]} : 성공
                        else if(Pattern.matches("^\\{\\\"data\\\"\\:\\[\\{\\\".*\\\"\\}\\]\\}", data.toString())){
                            Log.d("serverData_data", "get data......");
                            result = data.getJSONArray("data").toString();
                        }
                    }
                    else
                    {//json 객체 1개를 return
                        result = data.toString();
                    }break;
                case "show_single_name":
                    Log.d("serverData_"+category+"userid", data.getString(category+"_userid"));
                    result = data.getString(category+"_userid");break;

                default: result = data.toString();break;
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
            case "vehicle_status":category="vs";break;
            case "code_car":category="cc";break;
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
    }
}
