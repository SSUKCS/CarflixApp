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

public class ServerConnectionThread extends Thread{
    private static final String TAG = "ConnectionThread";
    String httpmethod;
    private String urlStr;
    private JSONObject requestBody=null;

    private String outputString;

    public boolean monitorExist = false;

    public ServerConnectionThread(String HTTPMethod, String command, JSONObject requestBody){
        this.httpmethod = HTTPMethod;//GET|POST|HEAD|OPTIONS|PUT|DELETE|TRACE
        this.urlStr = "http://13.56.94.107/admin/api/"+command+".php";
        this.requestBody = requestBody;
    }
    public ServerConnectionThread(String HTTPMethod, String command, String param, JSONObject requestBody){
        this.httpmethod = HTTPMethod;//GET|POST|HEAD|OPTIONS|PUT|DELETE|TRACE
        this.urlStr = "http://13.56.94.107/admin/api/"+command+".php?"+param;
        this.requestBody = requestBody;
    }
    public void run(){
        try{
            String result = request(urlStr);
            Log.d("ServerConnectionThread", "RESULT :: "+result);
            if(result != ""){
                outputString = result.substring(result.indexOf("{"));
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
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept-Language", "ko-KR,ko;q=0.8,en-US;q=0.6,en;q=0.4");
                    connection.setRequestProperty("Content-Transfer-Encoding", "application/json");

                    OutputStream outputStream = connection.getOutputStream();
                    Log.d(TAG, "REQUESTBODY :: "+ requestBody.toString());
                    outputStream.write(requestBody.toString().getBytes("euc-kr"));
                    outputStream.flush();
                }

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "URLSTR :: "+urlStr);
                Log.d(TAG, "HTTP RESPONSECODE :: "+ responseCode);
                if(httpmethod == "GET"){
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = null;
                    while(true) {
                        line = reader.readLine();
                        if(line==null)break;
                        output.append(line+"\n");
                    }
                    reader.close();
                }
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
