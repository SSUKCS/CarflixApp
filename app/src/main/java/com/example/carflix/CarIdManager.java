package com.example.carflix;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class CarIdManager extends ArduinoBluetooth{
    public static final byte ASSIGN_MODE = 1;
    public static final byte DELETE_MODE = 2;
    public static final String ID_EXIST = "id_exist";
    public static final String ID_CLEAR = "id_clear";
    public static final String ASSIGN_OK = "assign_ok";
    public static final String DELETE_OK = "delete_ok";
    public static final String DELETE_FAILED = "delete_failed";

    private static final String TAG = "CarIdManager";
    private byte mode;

    public interface CarIdManagerCallback {
        void onStateUpdate(String state);
        void onConnectFailed();
        void onBluetoothNotOn();
    }

    CarIdManagerCallback carIdManagerCallback;

    public static class Userdata{
        public Userdata(String mbId, String groupId, String groupStatus, String numberClassification, String registerationNum, String crCarName) {
            this.mbId = mbId;
            this.groupId = groupId;
            this.groupStatus = groupStatus;
            this.numberClassification = numberClassification;
            this.registerationNum = registerationNum;
            this.crCarName = crCarName;
        }

        public Userdata(String mbId){
            this.mbId = mbId;
        }

        private String mbId;
        private String groupId;
        private String groupStatus;
        private String numberClassification;
        private String registerationNum;

        private String crCarName;

        public String getCrCarName() {
            return crCarName;
        }

        public String getMbId() {
            return mbId;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getGroupStatus() {
            return groupStatus;
        }

        public String getNumberClassification() {
            return numberClassification;
        }

        public String getRegisterationNum() {
            return registerationNum;
        }

    }

    private Userdata userdata;

    public CarIdManager(Context context, BluetoothAdapter bluetoothAdapter, CarIdManagerCallback carIdManagerCallback, Userdata userdata, byte mode){
        super(context, bluetoothAdapter);
        this.carIdManagerCallback = carIdManagerCallback;
        this.mode = mode;
        this.userdata = userdata;
    }

    private long crId;
    private String macAddress;

    @Override
    public void onStateUpdate(String state) {
        this.carIdManagerCallback.onStateUpdate(state);
    }

    @Override
    public void onConnected(ArduinoInterpreter arduinoInterpreter, String macAddress) {
        this.macAddress = macAddress;
        arduinoInterpreter.startListening();
        ArduinoData arduinoData = new ArduinoData.Builder()
                .setReqbc()
                .build();
        arduinoInterpreter.sendToArduino(arduinoData);
        Log.i(TAG, "run: CarId가 있는지 보냄.");
        arduinoInterpreter.listenNext();
    }

    @Override
    public void onConnectionFailed() {
        this.carIdManagerCallback.onConnectFailed();
    }

    @Override
    public void onBluetoothNotOn() {
        this.carIdManagerCallback.onBluetoothNotOn();
    }

    @Override
    public void onReceive(ArduinoData arduinoData, ArduinoInterpreter arduinoInterpreter) {
        super.onReceive(arduinoData, arduinoInterpreter);
        if(arduinoData.getHeaderCode() == ArduinoData.S_SUCBC){
            if(arduinoData.getSucbc() == ArduinoData.EXIST_CR_ID) {
                if(mode == DELETE_MODE) {
                    onStateUpdate(ID_EXIST);
                    JSONObject requestBody = new JSONObject();
                    try {
                        requestBody.put("mb_id", userdata.getMbId());
                        requestBody.put("cr_mac_address", macAddress);
                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                    }
                    ServerData serverData = new ServerData("DELETE", "car/registration_delete_request", requestBody);
                    if (!serverData.get().equals("car delete request fail")) {
                        try {
                            String sCrId = new JSONObject(serverData.get()).getString("cr_id");
                            crId = Long.parseLong(sCrId);
                        } catch (JSONException e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                    arduinoData = new ArduinoData.Builder()
                            .setDeleteId(crId)
                            .build();
                    arduinoInterpreter.sendToArduino(arduinoData);
                    arduinoInterpreter.listenNext();
                }
            }
            else { //차에 아이디가 없다
                if(mode == ASSIGN_MODE){
                    onStateUpdate(ID_CLEAR);
                    try{
                        JSONObject carInfo = new JSONObject();
                        carInfo.put("mb_id", userdata.getMbId());
                        carInfo.put("group_id", userdata.getGroupId());
                        carInfo.put("status", userdata.getGroupStatus());
                        carInfo.put("cr_number_classification", userdata.getNumberClassification());
                        carInfo.put("cr_registeration_number", userdata.getRegisterationNum());
                        carInfo.put("cr_carname", userdata.getCrCarName());
                        carInfo.put("cr_mac_address", macAddress);

                        ServerData serverData = new ServerData("POST", "car/create", carInfo);
                        String sCrId = serverData.get();
                        crId = Long.parseLong(sCrId);
                        Log.i(TAG, "onReceive: crid : "+crId);
                    }
                    catch(JSONException e){
                        Log.e(TAG, e.toString());
                    }
                    arduinoData = new ArduinoData.Builder()
                            .setAssignId(crId)
                            .build();
                    arduinoInterpreter.sendToArduino(arduinoData);
                    arduinoInterpreter.listenNext();
                }
                else{
                    try{
                        JSONObject requestBody = new JSONObject().put("cr_mac_address", macAddress);
                        ServerConnectionThread serverConnectionThread = new ServerConnectionThread("DELETE", "car/macaddress_delete", requestBody);
                        serverConnectionThread.start();
                    }
                    catch(JSONException e){
                        Log.e("CarIdService", e.toString());
                    }
                    onStateUpdate(DELETE_OK);
                }
            }
        }
        else if(arduinoData.getHeaderCode() == ArduinoData.S_ASSIGN_ID_OK) {
            onStateUpdate(ASSIGN_OK);
        }
        else if(arduinoData.getHeaderCode() == ArduinoData.S_DELETE_OK){
            try{
                ServerConnectionThread serverConnectionThread = new ServerConnectionThread("DELETE", "car/delete", new JSONObject().put("cr_id", crId));
                serverConnectionThread.start();
            }
            catch(JSONException e){
                Log.e(TAG, e.toString());
            }
            onStateUpdate(DELETE_OK);
        }
        else if(arduinoData.getHeaderCode() == ArduinoData.S_DELETE_FAILED){
            onStateUpdate(DELETE_FAILED);
        }
    }
}