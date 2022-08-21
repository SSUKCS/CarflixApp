package com.example.carflix;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.core.app.NotificationCompat;

public class CarIdService extends Service {
    private static final String TAG = "CarIdService";

    private String mbId;
    private String groupId;
    private String groupStatus;
    private String numberClassification;
    private String registerationNum;
    private String carName;

    private int mode;
    public static final int ASSIGN_MODE = 1;
    public static final int DELETE_MODE = 2;

    private ArduinoBluetooth arduinoBluetooth;

    private String mState = ArduinoBluetooth.SEARCHING;
    public static final String ID_EXIST_ERROR = "id_exist_error";
    public static final String ID_NOT_EXIST_ERROR = "id_not_exist_error";
    public static final String ID_EXIST = "id_exist";
    public static final String ID_CLEAR = "id_clear";
    public static final String ASSIGN_OK = "assign_ok";
    public static final String DELETE_OK = "delete_ok";
    public static final String DELETE_FAILED = "delete_failed";
    public static final String UNKNOWN_ERROR = "unknown_error";
    public static final String ACTIVITY_DISTROYED = "activity_destroyed";

    private void setState(String state){
        mState = state;
    }
    private void passStateToActivity(){
        if(carIdServiceCallback != null) {
            carIdServiceCallback.onStateUpdate(mState);
        }
    }

    public String getState(){
        return mState;
    }

    public interface CarIdServiceCallback {
        void onStateUpdate(String state);
    }
    private CarIdServiceCallback carIdServiceCallback;

    public void registerCallback(CarIdServiceCallback stateUpdateCallBack){
        this.carIdServiceCallback = stateUpdateCallBack;
    }
    public void unregisterCallback(){
        this.carIdServiceCallback = null;
    }

    private class IdBluetooth extends ArduinoBluetooth{
        public IdBluetooth(Context context, BluetoothAdapter bluetoothAdapter){
            super(context, bluetoothAdapter);
        }

        @Override
        public void onStateUpdate(String state) {
            setState(state);
            passStateToActivity();
        }
        private String curStatus = "x";
        private String crId;
        @Override
        public void onConnected(ArduinoInterpreter arduinoInterpreter, String macAddress) {
            arduinoInterpreter.startListening();
            ArduinoData arduinoData = new ArduinoData.Builder()
                    .setReqbc()
                    .build();

            arduinoInterpreter.sendToArduino(arduinoData);
            Log.i(TAG, "run: CarId가 있는지 보냄.");
            waitUntilNotify();
            if(curStatus.equals(ID_CLEAR)){
                if(mode == ASSIGN_MODE){
                    onStateUpdate(ID_CLEAR);
                    //
                    /*
                       서버로 macAddress와 그외여러 값들을 보내고
                       서버로부터 crId를 받는다.
                     */
                    try{
                        JSONObject carInfo = new JSONObject();
                        carInfo.put("mb_id", mbId);
                        carInfo.put("group_id", groupId);
                        carInfo.put("status", groupStatus);
                        carInfo.put("cr_number_classification", numberClassification);
                        carInfo.put("cr_registeration_number", registerationNum);
                        carInfo.put("cr_carName", carName);
                        carInfo.put("cr_mac_address", macAddress);

                        ServerData serverData = new ServerData("POST", "car/create", carInfo);
                        crId = serverData.get();
                    }
                    catch(JSONException e){
                        Log.e(TAG, e.toString());
                    }
                    //
                    arduinoData = new ArduinoData.Builder()
                            .setAssignId(crId)
                            .build();
                    arduinoInterpreter.sendToArduino(arduinoData);
                    waitUntilNotify();
                    if(curStatus.equals(ASSIGN_OK)){
                        //아두이노에서 할당 성공을 알려줌
                        //차량에 crId를 성공한 경우이다.
                        //이때에는 최종적으로 서버에 해당 crId를 등록해야한다.
                        onStateUpdate(ASSIGN_OK);

                    }
                    else {
                        //차량에 crId를 등록실패한 경우이다.
                        //해당 crId는 무효처리되며 서버에서도 지워야한다.
                        try{
                            ServerConnectionThread serverConnectionThread = new ServerConnectionThread("DELETE", "car/create", new JSONObject().put("cr_id", crId));
                            serverConnectionThread.start();
                        }
                        catch(JSONException e){
                            Log.e(TAG, e.toString());
                        }
                        onStateUpdate(UNKNOWN_ERROR);
                    }
                }
                else
                    onStateUpdate(ID_EXIST_ERROR);
            }
            else if(curStatus.equals(ID_EXIST)){
                if(mode == DELETE_MODE){
                    onStateUpdate(ID_EXIST);
                    //
                    /*
                       서버로 헤더번호 71번의 차량등록 해제 요청을 한 뒤
                       서버로부터 crId를 받는다.
                     */
                    JSONObject requestBody = new JSONObject();
                    try{
                        requestBody.put("mb_id", mbId);
                        requestBody.put("cr_mac_address", macAddress);
                    }
                    catch(JSONException e) {
                        Log.e(TAG, e.toString());
                    }
                    ServerData serverData = new ServerData("DELETE", "registration_delete_request", null);
                    if(!serverData.get().equals("car delete request fail")) {
                        try{
                            crId = new JSONObject(serverData.get()).getString("cr_id");
                        }
                        catch(JSONException e){
                            Log.e(TAG, e.toString());
                        }
                    }
                    //
                    arduinoData = new ArduinoData.Builder()
                            .setDeleteId(crId)
                            .build();
                    arduinoInterpreter.sendToArduino(arduinoData);
                    waitUntilNotify();
                    if(curStatus.equals(DELETE_OK)){
                        //아두이노가 서버로부터 받은 crId와 자신의 crID가 일치한다면
                        //crId를 지운것이다.
                        //이때 서버에서 해당 crId의 차량정보가 삭제된다.
                        onStateUpdate(DELETE_OK);
                        try{
                            ServerConnectionThread serverConnectionThread = new ServerConnectionThread("DELETE", "car/create", new JSONObject().put("cr_id", crId));
                            serverConnectionThread.start();
                        }
                        catch(JSONException e){
                            Log.e(TAG, e.toString());
                        }

                    }
                    else if(curStatus.equals(DELETE_FAILED)){
                        //아두이노가 지우기에 실패한 경우이다.
                        //서버에서 해당 crId의 정보가 삭제되지 않는다.(작업이 무효처리된다.)
                        onStateUpdate(DELETE_FAILED);
                    }
                    else //지우기 실패한 경우이며 위와 동일한 작업을 한다.
                        onStateUpdate(UNKNOWN_ERROR);
                }
                else
                    onStateUpdate(ID_NOT_EXIST_ERROR);
            }
        }

        @Override
        public void onConnectionFailed() {
            setState(FAILED_CONNECTION); //기기 연결 실패
            passStateToActivity();
        }

        @Override
        public void onBluetoothNotOn() {
            Toast.makeText(getApplicationContext(), "블루투스가 켜져있지 않습니다.",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onReceive(ArduinoData arduinoData, ArduinoInterpreter arduinoInterpreter) {
            super.onReceive(arduinoData, arduinoInterpreter);
            if(arduinoData.getHeaderCode() == ArduinoData.S_SUCBC){
                if(arduinoData.getSucbc() == ArduinoData.EXIST_CR_ID)
                    curStatus = ID_EXIST;
                else
                    curStatus = ID_CLEAR;
                notifyToThread();
            }
            else if(arduinoData.getHeaderCode() == ArduinoData.S_ASSIGN_ID_OK) {
                curStatus = ASSIGN_OK;
                notifyToThread();
            }
            else if(arduinoData.getHeaderCode() == ArduinoData.S_DELETE_OK){
                curStatus = DELETE_OK;
                notifyToThread();
            }
            else if(arduinoData.getHeaderCode() == ArduinoData.S_DELETE_FAILED){
                curStatus = DELETE_FAILED;
                notifyToThread();
            }
        }
    }

    @Override
    public void onCreate() {
        if (arduinoBluetooth == null) {
            arduinoBluetooth = new IdBluetooth(
                    getApplicationContext(), BluetoothAdapter.getDefaultAdapter()
            );
        }
    }

    public class CarServiceBinder extends Binder {
        CarIdService getService(){
            return CarIdService.this;
        }
    }

    private final IBinder mBinder = new CarServiceBinder();

    @Override
    public IBinder onBind(Intent intent){

        mbId = intent.getStringExtra("mb_id");
        groupId = intent.getStringExtra("group_id");
        groupStatus = intent.getStringExtra("status");
        numberClassification = intent.getStringExtra("numberClassification");
        registerationNum = intent.getStringExtra("registerationNum");
        carName = intent.getStringExtra("carName");
        mode = intent.getIntExtra("mode", 0);
        if(mode == 0){
            Log.e(TAG, "onStartCommand: mode isn't be set.", new Exception("CarIdServiceException: not exist mode."));
        }
        arduinoBluetooth.start();
        return mBinder;
    }


    /**
     * 서비스 종료
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        setState(ACTIVITY_DISTROYED);
        if(carIdServiceCallback != null) {
            carIdServiceCallback.onStateUpdate(ACTIVITY_DISTROYED);
        }
        if(arduinoBluetooth != null){
            arduinoBluetooth.endConnection();
        }
    }
}