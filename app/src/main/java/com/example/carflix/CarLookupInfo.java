package com.example.carflix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CarLookupInfo extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap movementRecordMap;

    private ImageView userImage;
    private TextView userInfoText;

    private String carID;
    private String carName;

    private boolean stopRunning = false;
    Thread thread;
    Handler handler;
    private static int i=0, len=0;

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
        handler.post(new Runnable() {
            @Override
            public void run() {

            }
        });
        thread = new Thread(){
            public void run(){
                while (!stopRunning) {
                    ServerData serverData = new ServerData("GET", "vehicle_status/show", "cr_id="+carID,null);
                    String result = serverData.get();
                    if(!result.equals("No vehicle_status Found")){
                        Message message = handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        try{
                            JSONArray jsonArray = new JSONArray(result);
                            len = jsonArray.length();
                            if(len!=0){
                                for(i=0;i<len;i++){
                                    JSONObject vehicleStatus = jsonArray.getJSONObject(i);
                                    bundle.putString("userName"+i, vehicleStatus.getString("member"));
                                    bundle.putString("status"+i, vehicleStatus.getString("vs_startup_information"));
                                    bundle.putString("date"+i, vehicleStatus.getString("vs_regdate"));
                                    bundle.putString("latitude"+i, vehicleStatus.getString("vs_latitude"));
                                    bundle.putString("longitude"+i, vehicleStatus.getString("vs_longitude"));
                                }
                            }
                        }
                        catch(JSONException e){
                            Log.e("CarLookupInfo", "thread_JSONException :: "+e.toString());
                        }
                        message.setData(bundle);
                        handler.sendMessage(message);
                    }
                    try {
                        Thread.sleep(60000);//1000*60
                    } catch (InterruptedException e) {
                        Log.e("CarLookupInfo", "thread_InterruptedException :: "+e.toString());
                    }
                }
            }
        };
        thread.start();
    }
    @Override
    protected void onResume() {
        super.onResume();
        stopRunning = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRunning = true;

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
        userInfoText = findViewById(R.id.userInfoText);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopRunning = true;
    }
}
