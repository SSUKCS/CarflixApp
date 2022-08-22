package com.example.carflix;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.Timer;
import java.util.TimerTask;

public class CarTracingService extends Service {
    //블루투스 연결 관련 변수들
    private static final String TAG = "CarTracingService";

    private String carName;
    private String mbId;
    private long sendingTerm = 5000;

    private ArduinoBluetooth tracingThread;

    private String mState = ArduinoBluetooth.SEARCHING;

    public static final String GOT_AVAIL = "got_avail";
    private static final String TIME_OVER = "time_over";
    private static final String GOT_REQ = "got_req";
    private static final String GOT_OFF = "got_off";

    public static final String SUCCESSFUL_CAR_ON = "successful_car_on";
    public static final String FAILED_CAR_ON = "failed_car_on";
    public static final String FINISHED = "finished";

    //gps서비스 관련 변수들

    private boolean stopService = false;
    private FusedLocationProviderClient fusedLocationClient;
    private final Location[] location = {new Location(LocationManager.GPS_PROVIDER)};
    private LocationCallback locationCallback;

    private void setState(String state){
        mState = state;
    }
    private void passStateToActivity(){
        if(stateUpdateCallBack != null) {
            stateUpdateCallBack.onStateUpdate(mState);
        }
    }

    public String getState(){
        return mState;
    }

    public interface StateUpdateCallBack{
        void onStateUpdate(String state);
    }
    private StateUpdateCallBack stateUpdateCallBack;

    public void registerCallback(StateUpdateCallBack stateUpdateCallBack){
        this.stateUpdateCallBack = stateUpdateCallBack;
    }

    private class TracingBluetooth extends ArduinoBluetooth{
        public TracingBluetooth(Context context, BluetoothAdapter bluetoothAdapter){
            super(context, bluetoothAdapter);
        }

        @Override
        public void onStateUpdate(String state) {
            setState(state);
            passStateToActivity();
        }

        TimerTask timerTask;
        Timer expiringTimer;
        String eachStatus = "none";
        private void makeTimer(){
            timerTask = new TimerTask() {
                @Override
                public void run() {

                    eachStatus = TIME_OVER;
                    cancel();
                    notifyToThread();
                }
            };
            expiringTimer = new Timer();
            expiringTimer.schedule(timerTask, sendingTerm, sendingTerm);
        }
        private void sendToServerRepeatly(){
            makeTimer();
            boolean off;
            while(true) {
                arduinoInterpreter.listenNext();
                waitUntilNotify();
                requestLocationUpdates();
                /*location 이 잘 update되는지 확인하기 위한 코드
                if (location[0] != null) {
                    //location update
                    Log.d(TAG, "location update " + location[0]);
                    Log.d(TAG, "location Latitude " + location[0].getLatitude());
                    Log.d(TAG, "location Longitude " + location[0].getLongitude());
                    Log.d(TAG, "Speed :: " + location[0].getSpeed() * 3.6);
                    Log.e(TAG, "locationCallback :: "+locationCallback);

                }*/
                off = false;
                String command = "vehicle_status/";
                JSONObject requestBody = new JSONObject();
                try{
                    //서버에게 시동상태를 전송하는 코드
                    /*requestbody 형태
                        {
                        "vs_startup_information" : "on"/"off"/"connection_fault"(아두이노로부터 정보가 오지 않음),
                        "member" : ,
                        "vs_latitude" :,
                        "vs_longitude" :,
                        "cr_id"CrID:
                        }*/
                    switch (eachStatus) {
                        case TIME_OVER://아두이노로부터 응답을 받지 않았다는 데이터("connection_fault")를 전송
                            command +="connection_status";
                            requestBody.put("vs_startup_information", "connection_fault");
                            break;
                        case GOT_REQ://아두이노로부터 응답을 받았다는 데이터("on")를 전송
                            timerTask.cancel();
                            command +="boot_status";
                            requestBody.put("vs_startup_information", "on");
                            break;
                        case GOT_OFF://아두이노로부터 시동꺼짐을 받았다는 데이터("off")를 전송
                            timerTask.cancel();
                            command +="boot_off";
                            requestBody.put("vs_startup_information", "off");
                            off = true;
                            break;
                    }
                    //서버에게 시동상태를 전송
                    requestBody.put("member", mbId);
                    requestBody.put("vs_latitude", location[0].getLatitude());
                    requestBody.put("vs_longitude", location[0].getLongitude());
                    requestBody.put("cr_id", availData.getCrId());
                    ServerConnectionThread serverConnectionThread = new ServerConnectionThread("POST", command, requestBody);
                    serverConnectionThread.start();
                    if(off)
                        break;
                }
                catch(JSONException e){
                    Log.e(TAG, e.toString());
                }
                eachStatus = "none";
                makeTimer();
            }
        }
        ArduinoInterpreter arduinoInterpreter;
        @Override
        public void onConnected(ArduinoInterpreter arduinoInterpreter, String macAddress) {
            this.arduinoInterpreter = arduinoInterpreter;
            arduinoInterpreter.startListening();
            ArduinoData arduinoData = new ArduinoData.Builder()
                    .setReqon(mbId)
                    .build();
            arduinoInterpreter.sendToArduino(arduinoData);
            Log.i(TAG, "run: 시동 요청.");
            arduinoInterpreter.listenNext();
            waitUntilNotify();
            if(curStatus.equals(GOT_AVAIL)){ //시동요청이 올바른가에 해당
                String param = "cr_id="+availData.getCrId()+"&mb_id="+availData.getMbId();
                //서버에 시동요청이 올바른가 보냄
                ServerData serverData = new ServerData("GET", "vehicle_status/boot_on", param, null);
                JSONObject serverJsonData=null;
                String isRequestAvailable="";
                try{
                    serverJsonData = new JSONObject(serverData.get());
                    isRequestAvailable = serverJsonData.getString("message");
                }
                catch(JSONException e){
                    Log.e(TAG, e.toString());
                }
                if(isRequestAvailable.equals("success boot on")) {
                    //>>>만약 서버로부터 올바르다고 받았다면
                    arduinoData = new ArduinoData.Builder()
                            .setStart()
                            .build();
                    arduinoInterpreter.sendToArduino(arduinoData); //시동 허가 전송
                    arduinoInterpreter.listenNext();
                    waitUntilNotify(); //시동상태 전송요청을 받아야 시동이 걸린것임. 기다림.
                    if (curStatus.equals(SUCCESSFUL_CAR_ON)) { //시동상태 전송 요청을 받았다면
                        //***************서버에게 데이터 전송 (헤더 61번에 해당함)
                        JSONObject requestBody = new JSONObject();
                        String command = "vehicle_status/boot_status";
                        try {
                            requestBody.put("member", mbId);
                            requestBody.put("vs_latitude", location[0].getLatitude());
                            requestBody.put("vs_longitude", location[0].getLongitude());
                            requestBody.put("cr_id", availData.getCrId());
                            requestBody.put("vs_startup_information", "on");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        ServerConnectionThread serverConnectionThread = new ServerConnectionThread("POST", command, requestBody);
                        serverConnectionThread.start();
                        //시동 걸린 상태로 진입
                        makePendingIntent();
                        onStateUpdate(SUCCESSFUL_CAR_ON);
                        sendToServerRepeatly(); //반복적으로 5초마다 전송하는 메서드
                        //시동꺼짐 정보를 서버로 전송하고 시동상태 해제
                        arduinoData = new ArduinoData.Builder()
                                .setSendOffOk()
                                .build();
                        arduinoInterpreter.sendToArduino(arduinoData); //아두이노에게 완료 전달
                        arduinoInterpreter.listenNext();
                        //종료.
                        onStateUpdate(FINISHED);
                    }
                }
                else {
                    //>>>만약 올바르지 않다고 받았다면
                    onStateUpdate(FAILED_CAR_ON);
                }
            }
            stopSelf();
        }

        @Override
        public void onConnectionFailed() {
            setState(FAILED_CONNECTION); //기기 연결 실패
            passStateToActivity();
            stopSelf();
        }

        @Override
        public void onBluetoothNotOn() {
            Toast.makeText(getApplicationContext(), "블루투스가 켜져있지 않습니다.",
                    Toast.LENGTH_SHORT).show();
        }
        private ArduinoData.AvailData availData;
        private String curStatus = "x";
        @Override
        public void onReceive(ArduinoData arduinoData, ArduinoInterpreter arduinoInterpreter) {
            super.onReceive(arduinoData, arduinoInterpreter);
            if(arduinoData.getHeaderCode() == ArduinoData.S_REQON_AVAIL){ //
                availData = arduinoData.getReqonAvail();
                curStatus = GOT_AVAIL;
                notifyToThread();
            }
            else if(arduinoData.getHeaderCode() == ArduinoData.S_REQSEND_STATE){
                if(curStatus.equals(GOT_AVAIL)){
                    curStatus = SUCCESSFUL_CAR_ON;
                }
                else{
                    eachStatus = GOT_REQ;
                }
                notifyToThread();
            }
            else if(arduinoData.getHeaderCode() == ArduinoData.S_REQSEND_OFF){
                eachStatus = GOT_OFF;
                notifyToThread();
            }
        }
    }

    @Override
    public void onCreate() {
        if (tracingThread == null) {
            //tracing thread
            tracingThread = new TracingBluetooth(
                    getApplicationContext(), BluetoothAdapter.getDefaultAdapter()
            );
            //locationCallback 설정
            //IllegalStateException :: 불법적이거나 부적절한 시간에 메소드가 호출
            //LocationClient.connect() is asynchronous.
            // You can't immediately start using the client methods until connection is complete.

            //20220823 line 383 -> here
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    location[0] = locationResult.getLastLocation();
                }
            };
        }
    }

    public class CarServiceBinder extends Binder {
        CarTracingService getService(){
            return CarTracingService.this;
        }
    }

    private final IBinder mBinder = new CarServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        carName = intent.getStringExtra("car_name");
        mbId = intent.getStringExtra("mb_id");
        String bluetoothMacAddress = intent.getStringExtra("mac_address");
        if(bluetoothMacAddress != null)
            tracingThread.setTargetMacAddress(bluetoothMacAddress);
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.e(TAG, "Location Update Callback Removed");
        }
        tracingThread.start();
        return START_NOT_STICKY;
    }

    /**
     * 시동걸기 성공시 포그라운드서비스로 전환
     */
    private void makePendingIntent()
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1");
        //로고로 이미지 변경, 혹시 오류 발생시 아래 이미지로 재설정
        //R.mipmap.ic_launcher
        builder.setSmallIcon(R.drawable.image_carflix_logo);
        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText("현재 " + carName + "를 이용하고 있습니다.");
        style.setBigContentTitle(null);
        style.setSummaryText("차량 운행중");
        builder.setContentText(null);
        builder.setContentTitle(null);
        builder.setOngoing(true);
        builder.setStyle(style);
        builder.setWhen(0);
        builder.setShowWhen(false);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                    new NotificationChannel("1", "Carflix 포그라운드",
                            NotificationManager.IMPORTANCE_NONE)
            );
        }
        Notification notification = builder.build();
        startForeground(1, notification);
    }

    //locationCallback을 통해 location을 update
    private void requestLocationUpdates() {
        Log.e(TAG, "locationUpdateStart :: ");
        LocationRequest request = LocationRequest.create();
        request.setInterval(200)
                .setFastestInterval(100)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        final int[] permission = {ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)};
        if (permission[0] == PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "permission granted, requestLocationUpdates :: ");
            fusedLocationClient.requestLocationUpdates(request, locationCallback, null);
        }
        if(stopService)fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    /**
     * 서비스 종료 (포그라운드 서비스 종료)
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        //블루투스
        if(tracingThread != null){
            tracingThread.endConnection();
        }
        //gps
        stopForeground(STOP_FOREGROUND_REMOVE);
        Log.e(TAG, "onDestroy :: ");
        stopService = true;
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.e(TAG, "Location Update Callback Removed");
        }
    }
}