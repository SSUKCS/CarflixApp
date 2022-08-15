package com.example.carflix;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.annotation.NonNull;

public class serverConnectionThread extends Thread{
    private static final String TAG = "ConnectionThread";
    String httpmethod;
    private String urlStr;
    private JSONObject requestBody=null;

    private String outputString;

    public boolean monitorExist = false;
    private Handler handler=null;
    private TextView monitor=null;

    public void setMonitor(Handler handler, TextView monitor){
        this.handler = handler;
        this.monitor = monitor;
        if(handler != null && monitor != null)monitorExist=true;
    }
    public serverConnectionThread(String HTTPMethod, String command, JSONObject requestBody){
        this.httpmethod = HTTPMethod;//GET|POST|HEAD|OPTIONS|PUT|DELETE|TRACE
        this.urlStr = "http://13.56.94.107/admin/api/"+command+".php";
        this.requestBody = requestBody;
    }
    public serverConnectionThread(String HTTPMethod, String command, String param, JSONObject requestBody){
        this.httpmethod = HTTPMethod;//GET|POST|HEAD|OPTIONS|PUT|DELETE|TRACE
        this.urlStr = "http://13.56.94.107/admin/api/"+command+".php?"+param;
        this.requestBody = requestBody;
    }
    public void run(){
        try{
            String result = request(urlStr);
            if(result != null){
                outputString = result.substring(result.indexOf("{"));
                Log.d(TAG, "OUTPUT :: "+ outputString);
            }
            else{
                Log.d(TAG, "OUTPUT :: "+ result);
            }
            if(monitorExist){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        monitor.setText(outputString);
                    }
                });
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    @NonNull
    private String request(String urlStr){
        StringBuilder output = new StringBuilder();
        try{
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if(connection != null){
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestMethod(httpmethod);

                connection.setDoInput(true);
                connection.setDoOutput(true);

                //outstream을 통해 requestBody(jsonObject) 전달
                if(requestBody!=null){
                    //request body 전달시 json 형식으로 전달
                    connection.setRequestProperty("Content-Type", "applicaiont/json");

                    OutputStream outputStream = connection.getOutputStream();
                    Log.d(TAG, "REQUESTBODY :: "+ requestBody.toString());
                    outputStream.write(requestBody.toString().getBytes("euc-kr"));
                    outputStream.flush();
                }

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "URLSTR :: "+urlStr);
                Log.d(TAG, "HTTP RESPONSECODE :: "+ responseCode);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = null;
                while(true) {
                    line = reader.readLine();
                    if(line==null)break;
                    output.append(line+"\n");
                }
                reader.close();
                connection.disconnect();
            }
        }
        catch(Exception e) {
            Log.e(TAG, "Exception in processing response :: ", e);
        }
        return output.toString();
    }
    public String getResult(){
        return outputString;
    }
}
