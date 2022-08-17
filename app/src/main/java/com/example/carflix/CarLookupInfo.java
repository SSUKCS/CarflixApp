package com.example.carflix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class CarLookupInfo extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap movementRecordMap;

    private String driverName;
    private String carName;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 서비스에서 보내온 인텐트 내부의 데이터 획득
            String message = intent.getStringExtra("message");
            Log.d("receiver", "Got message: " + message);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_lookupinfo);

        carName = getIntent().getStringExtra("carName");

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        // action 이름이 "custom-event-name"으로 정의된 인텐트를 수신
        // observer의 이름은 mMessageReceiver이다.
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("locationService_location&speed"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mMessageReceiver);
    }
    // NULL이 아닌 GoogleMap 객체를 파라미터로 제공해 줄 수 있을 때 호출
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        movementRecordMap = googleMap;

        LatLng SEOUL = new LatLng(37.556, 126.97);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(SEOUL);
        markerOptions.title(carName);
        markerOptions.snippet("한국 수도");

        movementRecordMap.addMarker(markerOptions);

        movementRecordMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL, 10));
    }
}
