package com.example.carflix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CarLookupInfo extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap movementRecordMap;
    private Marker latestDriverLocation;

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

        latestDriverLocation = null;
        carID = getIntent().getStringExtra("carID");
        carName = getIntent().getStringExtra("carName");

        getSupportActionBar().setTitle(carName);

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //주기적으로(1분) 서버에 요청, 위치를 update
        //최대 120개(2시간) return
        ServerData serverData = new ServerData("GET", "vehicle_status/show", "cr_id="+carID,null);
        String result = serverData.get();
        if(!result.equals("No vehicle_status Found")){
            handler = new Handler();

            final Runnable runnable = new Runnable(){
                String userInfo="", member, status, date, hhmmtt, hourAndMinute;
                double latitude=0.0, longitude =0.0;
                JSONArray jsonArray;
                @Override
                public void run(){
                    try{
                        jsonArray = new JSONArray(result);
                        len = jsonArray.length();
                        if(len!=0){
                            movementRecordMap.clear();
                            //가장 최근 위치를 지도에 표시
                            JSONObject latestVehicleStatus = jsonArray.getJSONObject(0);

                            for(i=0;i<len;i++){
                                JSONObject vehicleStatus = jsonArray.getJSONObject(i);
                                member = vehicleStatus.getString("member");

                                if(!member.equals(vehicleStatus.getString("member")))
                                    //가장 최근에 사용한 member의 정보만 표시
                                    break;

                                status = vehicleStatus.getString("vs_startup_information");
                                date = vehicleStatus.getString("vs_regdate");
                                if(!vehicleStatus.getString("vs_latitude").equals("") &&
                                !vehicleStatus.getString("vs_longitude").equals("")){
                                    latitude = Double.parseDouble(vehicleStatus.getString("vs_latitude"));
                                    longitude = Double.parseDouble(vehicleStatus.getString("vs_longitude"));;
                                    logList.add(new LatLng(latitude, longitude), member, status, date);
                                }


                            }
                            //현재위치 표시connection_fault
                            //userName(String), status(String), date(String)
                            member = logList.getUserInfo(logList.getSize()-1)[0];
                            status = logList.getUserInfo(logList.getSize()-1)[1];
                            //"vs_regdate" : "yyyy-MM-dd tt:mm:ss"
                            date = logList.getUserInfo(logList.getSize()-1)[2];
                            hhmmtt = date.split(" ")[1];
                            hourAndMinute = hhmmtt.substring(0, hhmmtt.lastIndexOf(":"));
                            switch(status){
                                case "on"://(member)님이 이용중\n(hour:minute)부터 운행 시작
                                    userInfo = member+"님이 운전중\n"+hourAndMinute+"부터 운행 시작";
                                    break;
                                case "off"://마지막 사용자 : (member)님\n(hour:minute)에 주차
                                    userInfo =hourAndMinute+"에 주차\n"+"마지막 사용자 : "+member+"님";
                                    break;
                                case ""://(hour:minute)에 연결 끊김\n마지막 사용자 : (member)님
                                    userInfo = hourAndMinute+"에 연결 끊김\n마지막 사용자 : "+member+"님";
                                    break;
                                case "lock":
                                case "unlock":
                                case"trunk_lock":
                                case"trunk_unlock":
                                    userInfo = member+"님이 사용중\n"+hourAndMinute+"부터 운행 시작";
                                    break;
                                default:
                                    userInfo = "차량을 이용한 기록이\n존재하지 않습니다.";
                                    break;
                            }
                            userInfoTextView.setText(userInfo);
                            Log.d("CarLookupInfo", userInfo);
                            movementRecordMap.moveCamera(CameraUpdateFactory.newLatLngZoom(logList.getLocation(0), 100));
                            if(latestDriverLocation==null){
                                MarkerOptions markOptions = new MarkerOptions();
                                markOptions.position(new LatLng(latitude, longitude)).title(member).snippet(hourAndMinute);
                                latestDriverLocation = movementRecordMap.addMarker(markOptions);
                                movementRecordMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 10));
                            }
                            else{
                                latestDriverLocation.setPosition(new LatLng(latitude, longitude));
                                latestDriverLocation.setSnippet(hourAndMinute);
                            }
                            //위치 기록 표시
                            for(i=0;i<logList.getSize();i++){
                                MarkerOptions markerOptions = new MarkerOptions();
                                member = logList.getUserInfo(i)[0];
                                date = logList.getUserInfo(i)[2];
                                hhmmtt = date.split(" ")[1];
                                hourAndMinute = hhmmtt.substring(0, hhmmtt.lastIndexOf(":"));
                                markerOptions.position(logList.getLocation(i)).title(member).snippet(hourAndMinute);

                                Drawable markerImage = getDrawable(R.drawable.ic_track_mark);
                                Bitmap markerBitmap  = ((BitmapDrawable)markerImage).getBitmap();
                                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(markerBitmap));
                                movementRecordMap.addMarker(markerOptions);
                            }
                        }
                    }
                    catch(JSONException e){
                        Log.e("CarLookupInfo", "thread_JSONException :: "+e);
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
        LatLng SEOUL = new LatLng(37.532, 127.024);
        if(!logList.isEmpty())
            movementRecordMap.moveCamera(CameraUpdateFactory.newLatLngZoom(logList.getLocation(0), 10));
        else
            movementRecordMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL,10));
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