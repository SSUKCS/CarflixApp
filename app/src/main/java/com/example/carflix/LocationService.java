package com.example.carflix;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class LocationService extends Service {
    private static final String TAG = "locationService";

    private NotificationCompat.Builder notification;
    private NotificationManager NotificationManager;

    private Handler handler;
    private Runnable runnable;
    private boolean stopService = false;
    private FusedLocationProviderClient fusedLocationClient;
    private final Location[] location = {new Location(LocationManager.GPS_PROVIDER)};
    private LocationCallback locationCallback;


    String userID;
    String carID;
    String vs_startupInformation="on";

    @Override
    public void onCreate() {
        Log.e(TAG, "Background Service onCreate :: ");
        super.onCreate();

        handler = new Handler();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                location[0] = locationResult.getLastLocation();

                if (location[0] != null) {
                    //location update
                    Log.d(TAG, "location update " + location[0]);
                    Log.d(TAG, "location Latitude " + location[0].getLatitude());
                    Log.d(TAG, "location Longitude " + location[0].getLongitude());
                    Log.d(TAG, "Speed :: " + location[0].getSpeed() * 3.6);
                    Log.e(TAG, "locationCallback :: "+locationCallback);
                    try{
                        JSONObject requestBody = new JSONObject();
                        requestBody.put("vs_startup_information", vs_startupInformation);
                        requestBody.put("member", userID);
                        requestBody.put("vs_latitude", location[0].getLatitude());
                        requestBody.put("vs_longitude", location[0].getLongitude());
                        requestBody.put("cr_id", carID);

                        ServerConnectionThread serverConnectionThread = new ServerConnectionThread("POST", "vehicle_status/boot_status", requestBody);
                        serverConnectionThread.start();
                    } catch(JSONException e){
                        Log.e(TAG, e.toString());
                    }
                    if (NotificationManager != null && fusedLocationClient != null && !stopService) {
                        //pendingIntent를 통해 notification에 정보 전달
                        notification.setContentText("Your current location is " +  location[0].getLatitude() + "," + location[0].getLongitude());
                        NotificationManager.notify(101, notification.build());
                    }
                }
            }
        };
        runnable = new Runnable() {
            // GPS를 5초마다 가져오는 스레드를 서비스에서 실행해준다. 서비스가 실행되면 이 스레드도 같이 실행된다.
            @Override
            public void run() {
                try {
                    Log.e("runnable :: ", "is alive");
                    requestLocationUpdates();

                    handler.postDelayed(this, TimeUnit.SECONDS.toMillis(5));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        if (!stopService) {
            handler.postDelayed(runnable, TimeUnit.SECONDS.toMillis(5));
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.e(TAG, "onTaskRemoved :: ");
        stopSelf();
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand :: ");
        startForeground();
        userID = intent.getStringExtra("user_id");
        carID = intent.getStringExtra("car_id");
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.e(TAG, "Location Update Callback Removed");
        }
        return START_NOT_STICKY;
    }
    private void startForeground()
    {
        //pending인텐트 설정 및 notification 과 broadcast 리시버를 통해 연결
        Log.e(TAG, "startForeground :: ");
        // 포그라운드 서비스 상태에서 알림을 누르면 carInterface를 다시 열게 된다.
        Intent returnIntent = new Intent(this, CarInterface.class);
        //pendingIntent 설정
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                returnIntent, PendingIntent.FLAG_IMMUTABLE);
        String CHANNEL_ID = "carflix-locationService";
        String CHANNEL_NAME = "carflix-locationService";

        NotificationManager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Notification과 채널 연걸
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setVibrationPattern(new long[]{0});
            channel.enableVibration(true);
            NotificationManager.createNotificationChannel(channel);

            // Notification 세팅
            notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.image_carflix_logo)
                    .setContentTitle("Carflix")
                    .setContentIntent(pendingIntent)
                    .setChannelId(CHANNEL_ID)
                    .setContentText("운전중......");
            // id 값은 0보다 큰 양수가 들어가야 한다.
            NotificationManager.notify(101, notification.build());
            // foreground에서 시작
            startForeground(101, notification.build());
        }
        else {
            notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(STOP_FOREGROUND_REMOVE);
        Log.e(TAG, "onDestroy :: ");
        stopService = true;
        if (handler != null) {
            handler.removeCallbacks(runnable);
            Log.e(TAG, "removeCallbacks :: ");
        }
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.e(TAG, "Location Update Callback Removed");
        }
        stopSelf();
    }
    private void requestLocationUpdates() {
        Log.e(TAG, "locationUpdateStart :: ");
        LocationRequest request = LocationRequest.create();
        request.setInterval(200)
               .setFastestInterval(100)
               .setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        final int[] permission = {ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)};
        if (permission[0] == PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "permission granted, requestLocationUpdates :: ");
            fusedLocationClient.requestLocationUpdates(request, locationCallback, null);
        }
        if(stopService)fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}
