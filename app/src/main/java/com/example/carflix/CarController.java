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
    public static final String SUCCESSFUL_CONTROL = "successful_control";
    public static final String FAILED_CONTROL = "failed_control";
    public static final String ACTIVITY_DISTROYED = "activity_destroyed";

    public interface CarControlCallback {
        void onStateUpdate(String state);
        void onConnectFailed();
        void onBluetoothNotOn();
    }

    CarControlCallback carControlCallback;
    public CarController(Context context, BluetoothAdapter bluetoothAdapter,
                         CarControlCallback carControlCallback, byte mode){
        super(context, bluetoothAdapter);
        this.carControlCallback = carControlCallback;
        this.mode = mode;
    }

    @Override
    public void onStateUpdate(String state) {
        this.carControlCallback.onStateUpdate(state);
    }

    @Override
    public void onConnected(ArduinoInterpreter arduinoInterpreter, String macAddress) {
        ArduinoData arduinoData = new ArduinoData.Builder()
                .setCont(mode)
                .build();
        arduinoInterpreter.sendToArduino(arduinoData); //컨트롤 전송
        //String message=null;
        /*
        try{
            ServerData sendData = new ServerData("GET", "vehicle_status/lockunlock", command, null);
            JSONObject serverData = new JSONObject(sendData.get());
            message = serverData.getString("message");
        }
        catch(JSONException e){
            Log.e(TAG, e.toString());
        }
         */
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
