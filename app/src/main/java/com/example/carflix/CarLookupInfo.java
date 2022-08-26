package com.example.carflix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CarLookupInfo extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap movementRecordMap;

    private ImageView userImage;
    private TextView userInfoTextView;

    private String carID;
    private String carName;

    private static Handler handler;
    private static int i=0, len=0;

    private LogList logList = new LogList();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_lookupinfo);
        connectUI();

        carID = getIntent().getStringExtra("carID");
        carName = getIntent().getStringExtra("carName");

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //주기적으로(1분) 서버에 요청, 위치를 update
        //120개 return
        ServerData serverData = new ServerData("GET", "vehicle_status/show", "cr_id="+carID,null);
        String result = serverData.get();
        if(!result.equals("No vehicle_status Found")){

        }
        handler = new Handler();

        final Runnable runnable = new Runnable(){
            @Override
            public void run(){
                ServerData serverData = new ServerData("GET", "vehicle_status/show", "cr_id="+carID,null);
                String result = serverData.get();
                if(!result.equals("No vehicle_status Found")){
                    try{
                        JSONArray jsonArray = new JSONArray(result);
                        len = jsonArray.length();
                        if(len!=0){
                            String latestStatus  = jsonArray.getJSONObject(0).getString("vs_startup_information");
                            String latestMember = jsonArray.getJSONObject(0).getString("member");
                            String latestRegdate = jsonArray.getJSONObject(0).getString("vs_regdate");
                            String userInfo = "";
                            switch(latestStatus){
                                case "on"://(member)님이 이용중\n(vs_regdate)부터 운행 시작
                                    userInfo = latestMember+"님이 이용중\n"+latestRegdate+"부터 운행 시작";
                                    break;
                                case "off"://마지막 사용자 : (member)님\n(vs_regdate)에 주차
                                    userInfo =latestRegdate+"에 주차\n"+"마지막 사용자 : "+latestMember+"님";
                                    break;
                                case "connection_fault"://(vs_regdate)에 연결 끊김\n마지막 사용자 : (member)님
                                    userInfo = latestRegdate+"에 연결 끊김\n마지막 사용자 : "+latestMember+"님";
                                    break;
                                case "lock":
                                case "unlock":
                                case"trunk_lock":
                                case"trunk_unlock":
                                default:break;
                            }
                            for(i=0;i<len;i++){
                                JSONObject vehicleStatus = jsonArray.getJSONObject(i);
                                String member = vehicleStatus.getString("member");
                                String status = vehicleStatus.getString("vs_startup_information");
                                String date = vehicleStatus.getString("vs_longitude");
                                double latitude = Double.parseDouble(vehicleStatus.getString("vs_latitude"));
                                double longitude = Double.parseDouble(vehicleStatus.getString("vs_longitude"));;
                                logList.add(new LatLng(latitude, longitude), member, status, date);
                            }
                            logList.showByLog();
                        }
                    }
                    catch(JSONException e){
                        Log.e("CarLookupInfo", "thread_JSONException :: "+e);
                    }
                }
            }
        };
        Thread thread = new Thread(new Runnable(){
            public void run(){
                while (true) {
                    handler.post(runnable) ;
                    try {
                        Thread.sleep(60000);
                    } catch (Exception e) {
                        e.printStackTrace() ;
                    }
                }
            }
        });
        thread.start();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }
    // NULL이 아닌 GoogleMap 객체를 파라미터로 제공해 줄 수 있을 때 호출
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        movementRecordMap = googleMap;

        LatLng SEOUL = new LatLng(37.556, 126.97);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(SEOUL);
        markerOptions.title(carName);
        markerOptions.snippet("현재 위치 : ");

        movementRecordMap.addMarker(markerOptions);

        movementRecordMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL, 10));
    }

    private void connectUI(){
        userImage = findViewById(R.id.userImage);
        userInfoTextView = findViewById(R.id.userInfoText);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}
