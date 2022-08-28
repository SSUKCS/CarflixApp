package com.example.carflix;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class CarController extends ArduinoBluetooth{
    private static final String TAG = "CarController";
    private byte mode;
    public static final byte DOOR_OPEN = 1;
    public static final byte DOOR_CLOSE = 2;
    public static final byte TRUNK_OPEN = 3;
    public static final byte TRUNK_CLOSE = 4;

    private String modeToString(byte mode){
        switch (mode){
            case DOOR_OPEN:
                return "unlock";
            case DOOR_CLOSE:
                return "lock";
            case TRUNK_OPEN:
                return "trunk_unlock";
            case TRUNK_CLOSE:
                return "trunk_lock";
        }
        return "none";
    }
    public static final String SUCCESSFUL_CONTROL = "successful_control";
    public static final String FAILED_CONTROL = "failed_control";
    public static final String ACTIVITY_DISTROYED = "activity_destroyed";

    public interface CarControlCallback {
        void onStateUpdate(String state);
        void onConnectFailed();
        void onBluetoothNotOn();
    }

    private final CarControlCallback carControlCallback;
    private String mbId;
    private String crId;
    public CarController(Context context, BluetoothAdapter bluetoothAdapter,
                         CarControlCallback carControlCallback, byte mode, String mbId, String crId){
        super(context, bluetoothAdapter);
        this.carControlCallback = carControlCallback;
        this.mode = mode;
        this.mbId = mbId;
        this.crId = crId;
    }

    @Override
    public void onStateUpdate(String state) {
        this.carControlCallback.onStateUpdate(state);
    }

    @Override
    public void onConnected(ArduinoInterpreter arduinoInterpreter, String macAddress) {
        String message = null;
        String param = "cr_id=" + crId + "&mb_id=" + mbId + "&how=" + modeToString(mode);
        //컨트롤 전송후 서버로 데이터 전송
        try{
            ServerData sendData = new ServerData("GET", "vehicle_status/lockunlock", param, null);
            JSONObject serverData = new JSONObject(sendData.get());
            message = serverData.getString("message");
        }
        catch(JSONException e){
            Log.e(TAG, e.toString());
        }

        if(message != null && message.startsWith("success")) {
            //>>>만약 서버로부터 올바르다고 받았다면...
            ArduinoData arduinoData = new ArduinoData.Builder()
                    .setCont(mode)
                    .build();
            arduinoInterpreter.sendToArduino(arduinoData); //컨트롤 전송
            arduinoInterpreter.startListening();
            arduinoInterpreter.listenNext();
        }
        else {
            //>>>만약 올바르지 않다고 받았다면
            onStateUpdate(FAILED_CONTROL);
            return;
        }
        onStateUpdate(SUCCESSFUL_CONTROL);
    }

    @Override
    public void onConnectionFailed() {
        this.carControlCallback.onConnectFailed();
    }

    @Override
    public void onBluetoothNotOn() {
        this.carControlCallback.onBluetoothNotOn();
    }

    @Override
    public void onReceive(ArduinoData arduinoData, ArduinoInterpreter arduinoInterpreter) {
    }
}
