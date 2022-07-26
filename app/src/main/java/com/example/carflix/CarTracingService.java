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
import android.os.IBinder;
import android.os.Looper;
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

    private String mbId;
    private CarData carData;
    private long expireTerm = 5000;

    private ArduinoBluetooth tracingThread;

    private String mState = ArduinoBluetooth.SEARCHING;

    private static final int TIME_OVER = 1;
    private static final int GOT_REQ = 2;
    private static final int GOT_OFF = 3;

    public static final String SUCCESSFUL_CAR_ON = "successful_car_on";
    public static final String FAILED_CAR_ON = "failed_car_on";
    public static final String FINISHED = "finished";

    //gps서비스 관련 변수들

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

        private void makeTimer(){
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    sendToServer(TIME_OVER);
                }
            };
            expiringTimer = new Timer();
            expiringTimer.schedule(timerTask, expireTerm, expireTerm);
        }
        private void sendToServer(int eachStatus) {
            String command = "vehicle_status/";
            JSONObject requestBody = new JSONObject();
            try {
                switch (eachStatus) {
                    case TIME_OVER://아두이노로부터 응답을 받지 않았다는 데이터("connection_fault")를 전송
                        Log.i(TAG, "sendToServer: 시간초과");
                        command += "connection_status";
                        requestBody.put("vs_startup_information", "connection_fault");
                        break;
                    case GOT_REQ://아두이노로부터 응답을 받았다는 데이터("on")를 전송
                        Log.i(TAG, "sendToServer: on 받았음.");
                        command += "boot_status";
                        requestBody.put("vs_startup_information", "on");
                        break;
                    case GOT_OFF://아두이노로부터 시동꺼짐을 받았다는 데이터("off")를 전송
                        Log.i(TAG, "sendToServer: off 받았음.");
                        command += "boot_off";
                        requestBody.put("vs_startup_information", "off");
                        break;
                }

                requestBody.put("member", mbId);
                requestBody.put("vs_latitude", location[0].getLatitude());
                requestBody.put("vs_longitude", location[0].getLongitude());
                requestBody.put("cr_id", carData.getCarID());
                ServerConnectionThread serverConnectionThread = new ServerConnectionThread("POST", command, requestBody);
                serverConnectionThread.start();

                Log.i(TAG, "sendToServer: "+location[0].getLatitude()+", "+location[0].getLongitude());

            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }
        }

        ArduinoInterpreter arduinoInterpreter;
        @Override
        public void onConnected(ArduinoInterpreter arduinoInterpreter, String macAddress) {
            this.arduinoInterpreter = arduinoInterpreter;
            String param = "cr_id="+carData.getCarID()+"&mb_id="+mbId;
            //서버에 시동요청이 올바른가 보냄
            ServerData serverData = new ServerData("GET", "vehicle_status/boot_on", param, null);
            JSONObject serverJsonData=null;
            String isRequestAvailable="";
            try{
                serverJsonData = new JSONObject(serverData.get());
                isRequestAvailable = serverJsonData.getString("message");
                Log.i(TAG, "onConnected: " + isRequestAvailable);
            }
            catch(JSONException e){
                Log.e(TAG, e.toString());
            }
            if(isRequestAvailable.equals("success boot on")) {
                //>>>만약 서버로부터 올바르다고 받았다면
                arduinoInterpreter.startListening();
                ArduinoData arduinoData = new ArduinoData.Builder()
                        .setStart()
                        .build();
                arduinoInterpreter.sendToArduino(arduinoData); //시동 허가 전송
                arduinoInterpreter.listenNext();
            }
            else{
                onStateUpdate(FAILED_CAR_ON);
                end();
                return;
            }
            Log.i(TAG, "run: 시동 성공.");
        }

        @Override
        public void onConnectionFailed() {
            setState(FAILED_CONNECTION); //기기 연결 실패
            passStateToActivity();
            end();
        }

        @Override
        public void onBluetoothNotOn() {
            Toast.makeText(getApplicationContext(), "블루투스가 켜져있지 않습니다.",
                    Toast.LENGTH_SHORT).show();
        }
        boolean firstReceive = true;
        boolean nowRun = false;
        @Override
        public void onReceive(ArduinoData arduinoData, ArduinoInterpreter arduinoInterpreter) {
            super.onReceive(arduinoData, arduinoInterpreter);
            if(arduinoData.getHeaderCode() == ArduinoData.S_REQSEND_STATE){
                nowRun = true;
                if (firstReceive){
                    firstReceive = false;
                    sendToServer(GOT_REQ);
                    //시동 걸린 상태로 진입
                    makePendingIntent();
                    onStateUpdate(SUCCESSFUL_CAR_ON);
                    makeTimer();
                    arduinoInterpreter.listenNext();
                }
                else {
                    timerTask.cancel();
                    sendToServer(GOT_REQ);
                    makeTimer();
                    arduinoInterpreter.listenNext();
                }
            }
            else if(arduinoData.getHeaderCode() == ArduinoData.S_REQSEND_OFF){
                if(nowRun) {
                    timerTask.cancel();
                    sendToServer(GOT_OFF);
                    arduinoData = new ArduinoData.Builder()
                            .setSendOffOk()
                            .build();
                    arduinoInterpreter.sendToArduino(arduinoData); //아두이노에게 완료 전달
                    arduinoInterpreter.listenNext();
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                    //종료.
                    onStateUpdate(FINISHED);
                    end();
                }
                else{ //시동걸기 전에 받은 off 신호는 종료시킨다
                    arduinoData = new ArduinoData.Builder()
                            .setSendOffOk()
                            .build();
                    arduinoInterpreter.sendToArduino(arduinoData); //아두이노에게 완료 전달
                    arduinoInterpreter.listenNext();
                }
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
        mState = ArduinoBluetooth.SEARCHING;

        mbId = intent.getStringExtra("mb_id");
        carData = (CarData) intent.getSerializableExtra("car_data");
        String bluetoothMacAddress = intent.getStringExtra("mac_address");
        if(bluetoothMacAddress != null)
            tracingThread.setTargetMacAddress(bluetoothMacAddress);
        if(tracingThread.getState()==Thread.State.NEW)
            tracingThread.start();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                location[0] = locationResult.getLastLocation();
            }
        };
        requestLocationUpdates();
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
        style.bigText(null);
        
        style.setBigContentTitle("회원님께서 " + carData.getCarName() + "을 운전하고 있습니다.");
        style.setSummaryText("차량 운전중");
        builder.setContentText(null);
        builder.setContentTitle(null);
        builder.setOngoing(true);
        builder.setStyle(style);
        builder.setWhen(0);
        builder.setShowWhen(false);

        Intent notificationIntent = new Intent(getBaseContext(), CarInterface.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.putExtra("memberID", mbId);

        notificationIntent.putExtra("carData", carData);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                notificationIntent, PendingIntent.FLAG_MUTABLE);

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
        request.setInterval(2000)//sendingTerm*2
                .setFastestInterval(1000)//sendingTerm
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        final int[] permission = {
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        };
        if (permission[0] == PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "permission granted, requestLocationUpdates :: ");
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
        }
    }

    private boolean isEnd = false;
    public void end(){
        //블루투스
        if(!isEnd) {
            isEnd = true;
            if (tracingThread != null) {
                tracingThread.endConnection();
            }
            //gps
            stopForeground(STOP_FOREGROUND_REMOVE);
            Log.e(TAG, "onDestroy :: ");
            if (fusedLocationClient != null) {
                fusedLocationClient.removeLocationUpdates(locationCallback);
                Log.e(TAG, "Location Update Callback Removed");
            }
            stopSelf();
        }
    }

    /**
     * 서비스 종료 (포그라운드 서비스 종료)
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        tracingThread = null;
    }
}