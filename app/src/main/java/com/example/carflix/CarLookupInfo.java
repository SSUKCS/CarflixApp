package com.example.carflix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
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

import java.io.ByteArrayInputStream;

public class CarLookupInfo extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap movementRecordMap;
    private Marker latestDriverLocation;

    private ImageView userImage;
    private TextView userInfoTextView;

    private String carID;
    private String carName;

    private static Handler handler;

    private static int i=0, len=0;
    private static String userInfo, latestUserID, userID, userName, status, date, hhmmtt, hourAndMinute;
    private static ServerData serverData;

    private LogList logList = new LogList();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_lookupinfo);

        latestDriverLocation = null;
        //carID = getIntent().getStringExtra("carID");
        carID = "3";
        carName = getIntent().getStringExtra("carName");

        getSupportActionBar().setTitle(carName);

        serverData = new ServerData("GET", "vehicle_status/show", "cr_id="+carID,null);
        String result = serverData.get();

        if(!result.equals("No vehicle_status Found")){
            connectUI();

            SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            handler = new Handler();

            final Runnable runnable = new Runnable(){
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
                            latestUserID = latestVehicleStatus.getString("member");
                            //1. 서버에서 받은 데이터를 logList로 저장
                            for(i=0;i<len;i++){
                                JSONObject vehicleStatus = jsonArray.getJSONObject(i);
                                userID = vehicleStatus.getString("member");
                                if(!userID.equals(latestUserID)||i==len-1){
                                    //가장 최근에 사용한 member의 정보만 표시
                                    serverData = new ServerData("GET", "member/show", "mb_id="+latestUserID, null);
                                    JSONObject userData = new JSONObject(serverData.get());
                                    if(!userData.getString("mb_id").equals("")){
                                        //mb_nickname(mb_userid)
                                        userName = userData.getString("mb_nickname")+"("+userData.getString("mb_userid")+")";
                                        try{
                                            String userImageBase64 = userData.getString("mb_image");
                                            Log.d("settingProfile", "mb_image :: "+userImageBase64);
                                            //,(22글자) 제거
                                            userImageBase64 = userImageBase64.substring("data:image\\/;base64".length());
                                            if(!userImageBase64.equals("")){

                                                byte[] image = Base64.decode(userImageBase64, Base64.DEFAULT);

                                                //byte[] 데이터  stream 데이터로 변환 후 bitmapFactory로 이미지 생성
                                                ByteArrayInputStream inStream = new ByteArrayInputStream(image);
                                                Bitmap bitmap = BitmapFactory.decodeStream(inStream) ;
                                                userImage.setImageBitmap(bitmap);
                                            }
                                            else{
                                                userImage.setImageResource(R.drawable.userimage1_default);
                                            }
                                        }
                                        catch(IllegalArgumentException e){//ava.lang.IllegalArgumentException: bad base-64
                                            userImage.setImageResource(R.drawable.userimage1_default);
                                            Log.e("ProfileMenu_settingProfile_getImage", e.toString());
                                        }
                                    }
                                    else{
                                        userName = "이미 탈퇴한 회원입니다.";
                                    }
                                    break;
                                }
                                status = vehicleStatus.getString("vs_startup_information");
                                date = vehicleStatus.getString("vs_regdate");
                                if(!vehicleStatus.getString("vs_latitude").equals("") &&
                                        !vehicleStatus.getString("vs_longitude").equals("")){
                                    latitude = Double.parseDouble(vehicleStatus.getString("vs_latitude"));
                                    longitude = Double.parseDouble(vehicleStatus.getString("vs_longitude"));;
                                    logList.add(new LatLng(latitude, longitude), userID, status, date);
                                }


                            }

                            //2-1.현재 위치(가장 최근 위치)를 표시
                            //userName(String), status(String), date(String)
                            logList.getClass();
                            userID = logList.getUserInfo(logList.getSize()-1)[0];
                            status = logList.getUserInfo(logList.getSize()-1)[1];
                            //"vs_regdate" : "yyyy-MM-dd tt:mm:ss"
                            date = logList.getUserInfo(logList.getSize()-1)[2];
                            hhmmtt = date.split(" ")[1];
                            hourAndMinute = hhmmtt.substring(0, hhmmtt.lastIndexOf(":"));
                            if(!userName.equals("이미 탈퇴한 회원입니다.")){
                                switch(status){
                                    case "on"://(userName)님이 이용중\n(hour:minute)부터 운행 시작
                                        userInfo = userName+"님이 운전중\n"+hourAndMinute+"부터 운행 시작";
                                        break;
                                    case "off"://마지막 사용자 : (userName)님\n(hour:minute)에 주차
                                        userInfo =hourAndMinute+"에 주차\n"+"마지막 사용자 : "+userName+"님";
                                        break;
                                    case ""://(hour:minute)에 연결 끊김\n마지막 사용자 : (userName)님
                                        userInfo = hourAndMinute+"에 연결 끊김\n마지막 사용자 : "+userName+"님";
                                        break;
                                    case "lock":
                                    case "unlock":
                                    case"trunk_lock":
                                    case"trunk_unlock":
                                        userInfo = userName+"님이 사용중\n"+hourAndMinute+"부터 운행 시작";
                                        break;
                                    default:
                                        userInfo = "차량 이용 기록을 불러오는데\n실패했습니다.";
                                        break;
                                }
                            }
                            else{
                                String lastLocation = logList.getLocationToString(logList.getSize()-1);
                                userInfo = "탈퇴한 회원입니다.\n마지막 위치 : "+lastLocation;
                            }
                            userInfoTextView.setText(userInfo);
                            Log.d("CarLookupInfo", userInfo);
                            //2-2.현재 위치(가장 최근 위치)를 지도 상에 표시
                            movementRecordMap.moveCamera(CameraUpdateFactory.newLatLngZoom(logList.getLocation(logList.getSize()-1), 20));
                            if(latestDriverLocation==null){
                                MarkerOptions markOptions = new MarkerOptions();
                                markOptions.position(new LatLng(latitude, longitude)).title(userName).snippet(hourAndMinute);
                                latestDriverLocation = movementRecordMap.addMarker(markOptions);
                                movementRecordMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 50));
                            }
                            else{
                                latestDriverLocation.setPosition(new LatLng(latitude, longitude));
                                latestDriverLocation.setSnippet(hourAndMinute);
                            }
                            //3. 위치 기록 표시
                            for(i=0;i<logList.getSize();i++){
                                MarkerOptions markerOptions = new MarkerOptions();
                                userID = logList.getUserInfo(i)[0];
                                date = logList.getUserInfo(i)[2];
                                hhmmtt = date.split(" ")[1];
                                hourAndMinute = hhmmtt.substring(0, hhmmtt.lastIndexOf(":"));
                                markerOptions.position(logList.getLocation(i)).title(userName).snippet(hourAndMinute);

                                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getBitmap(R.drawable.ic_track_mark)));
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
                    //주기적으로(1분) 서버에 요청, 위치를 update
                    //최대 120개(2시간) return
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
        else{
            findViewById(R.id.mapLayout).setVisibility(View.GONE);
            findViewById(R.id.userInfo).setVisibility(View.GONE);
            findViewById(R.id.noData).setVisibility(View.VISIBLE);
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
    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
    private void connectUI(){
        userImage = findViewById(R.id.userImage);
        userInfoTextView = findViewById(R.id.userInfoText);
    }
    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = ContextCompat.getDrawable(this, drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}