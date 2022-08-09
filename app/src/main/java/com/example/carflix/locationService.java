package com.example.carflix;

import android.Manifest;
import android.app.Activity;
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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.concurrent.TimeUnit;

public class locationService extends Service {
    private static final String TAG = "locationService";
    private Context context;

    private NotificationCompat.Builder notification;
    private NotificationManager NotificationManager;

    private Handler handler;
    private Runnable runnable;
    private boolean stopService = false;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private carData carData;
    IBinder LBinder = new locationBinder();
    class locationBinder extends Binder {
        // 외부로 데이터를 전달하려면 바인더 사용
        // Binder 객체는 IBinder 인터페이스 상속구현 객체
        //public class Binder extends Object implements IBinder
        locationService getService(){
            return locationService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "Background Service onCreate :: ");
        super.onCreate();
        context = this;
        handler = new Handler();
        runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    requestLocationUpdates();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    handler.postDelayed(this, TimeUnit.SECONDS.toMillis(2));
                }
            }
        };
        if (!stopService) {
            handler.postDelayed(runnable, TimeUnit.SECONDS.toMillis(2));
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //액티비티에서 bindService() 를 실행하면 호출
        // 리턴한 IBinder 객체는 서비스와 클라이언트 사이의 인터페이스 정의
        // 서비스 객체를 리턴
        return LBinder;
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.e(TAG, "onTaskRemoved :: ");
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand :: ");
        carData = (carData)intent.getSerializableExtra("carData");
        startForeground();
        // GPS를 2초마다 가져오는 스레드를 서비스에서 실행해준다. 서비스가 실행되면 이 스레드도 같이 실행된다.
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.e(TAG, "Location Update Callback Removed");
        }
        return START_STICKY;
    }
    private void startForeground()
    {
        Log.e(TAG, "startForeground :: ");
        // 포그라운드 서비스 상태에서 알림을 누르면 carInterface를 다시 열게 된다.
        Intent returnIntent = new Intent(getApplicationContext(), carInterface.class);
        returnIntent.putExtra("carData", carData);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                returnIntent, PendingIntent.FLAG_UPDATE_CURRENT);
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
                    .setSmallIcon(R.drawable.userimage_default)
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
        Log.e(TAG, "onDestroy :: ");
        stopService = true;
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.e(TAG, "Location Update Callback Removed");
        }
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
            Log.e(TAG, "permission granted :: ");
            final Location[] location = {new Location(LocationManager.GPS_PROVIDER)};
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    location[0] = locationResult.getLastLocation();

                    if (location[0] != null) {
                        Log.d(TAG, "location update " + location[0]);
                        Log.d(TAG, "location Latitude " + location[0].getLatitude());
                        Log.d(TAG, "location Longitude " + location[0].getLongitude());
                        Log.d(TAG, "Speed :: " + location[0].getSpeed() * 3.6);

                        if (NotificationManager != null && fusedLocationClient != null && !stopService) {
                            notification.setContentText("Your current location is " +  location[0].getLatitude() + "," + location[0].getLongitude());
                            NotificationManager.notify(101, notification.build());
                        }
                    }
                }
            };
            fusedLocationClient.requestLocationUpdates(request, locationCallback, null);
        }
    }
}
