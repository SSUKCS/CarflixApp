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

import java.util.Timer;
import java.util.TimerTask;

public class CarControlService extends Service {
    private static final String TAG = "CarTracingService";

    private String carName;
    private String mbId;
    
    private byte mode;
    public static final byte DOOR_OPEN = 1;
    public static final byte DOOR_CLOSE = 2;
    public static final byte TRUNK_OPEN = 3;
    public static final byte TRUNK_CLOSE = 4;

    private ArduinoBluetooth controlThread;

    private String mState = ArduinoBluetooth.SEARCHING;
    public static final String GOT_AVAIL = "got_avail";

    public static final String SUCCESSFUL_CONTROL = "successful_control";
    public static final String FAILED_CONTROL = "failed_control";
    public static final String ACTIVITY_DISTROYED = "activity_destroyed";

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
    public void unregisterCallback(){
        this.stateUpdateCallBack = null;
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

        @Override
        public void onConnected(ArduinoInterpreter arduinoInterpreter, String macAddress) {
            ArduinoData arduinoData = new ArduinoData.Builder()
                    .setReqcont(mbId, mode)
                    .build();
            arduinoInterpreter.sendToArduino(arduinoData);
            Log.i(TAG, "Request control-"+mode);
            arduinoInterpreter.startListening();
            arduinoInterpreter.listenNext();
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
            if(arduinoData.getHeaderCode() == ArduinoData.S_REQCONT_AVAIL){ //
                ArduinoData.AvailData availData = arduinoData.getReqcontAvail();
                String command = "cr_id="+ availData.getCrId().trim()+"&mb_id="+ availData.getMbId().trim()+"&how=";
                switch (mode){
                    case DOOR_OPEN:
                        command += "unlock";
                        break;
                    case DOOR_CLOSE:
                        command += "lock";
                        break;
                    case TRUNK_OPEN:
                        command += "trunk_unlock";
                        break;
                    case TRUNK_CLOSE:
                        command += "trunk_lock";
                        break;
                }
                String message=null;
                try{
                    ServerData sendData = new ServerData("GET", "vehicle_status/lockunlock", command, null);
                    JSONObject serverData = new JSONObject(sendData.get());
                    message = serverData.getString("message");
                }
                catch(JSONException e){
                    Log.e(TAG, e.toString());
                }

                if(message != null && message.startsWith("success")) {
                    //>>>만약 서버로부터 올바르다고 받았다면...
                    arduinoData = new ArduinoData.Builder()
                            .setCont(mode)
                            .build();
                    arduinoInterpreter.sendToArduino(arduinoData); //컨트롤 전송
                    onStateUpdate(SUCCESSFUL_CONTROL);
                }
                else {
                    //>>>만약 올바르지 않다고 받았다면
                    onStateUpdate(FAILED_CONTROL);
                }
            }
        }
    }

    @Override
    public void onCreate() {
        if (controlThread == null) {
            controlThread = new TracingBluetooth(
                    getApplicationContext(), BluetoothAdapter.getDefaultAdapter()
            );
        }
    }

    public class CarServiceBinder extends Binder {
        CarControlService getService(){
            return CarControlService.this;
        }
    }

    private final IBinder mBinder = new CarServiceBinder();

    @Override
    public IBinder onBind(Intent intent){
        mbId = intent.getStringExtra("mb_id");
        String bluetoothMacAddress = intent.getStringExtra("mac_address");
        if(bluetoothMacAddress != null)
            controlThread.setTargetMacAddress(bluetoothMacAddress);

        mode = intent.getByteExtra("mode", (byte) 0);
        if(mode == 0){
            Log.e(TAG, "onStartCommand: mode isn't be set.", new Exception("CarControlServiceException: not exist mode."));
        }
        controlThread.start();
        return mBinder;
    }

    /**
     * 서비스 종료
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        setState(ACTIVITY_DISTROYED);
        if(stateUpdateCallBack != null) {
            stateUpdateCallBack.onStateUpdate(ACTIVITY_DISTROYED);
        }
        if(controlThread != null){
            controlThread.endConnection();
        }
    }
}