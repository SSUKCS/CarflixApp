package com.example.carflix;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

@SuppressLint("MissingPermission")
public abstract class ArduinoBluetooth extends Thread {
    public ArduinoBluetooth(Context context, BluetoothAdapter bluetoothAdapter) {
        this.context = context;
        this.bluetoothAdapter = bluetoothAdapter;
    }
    private final Context context;
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private ArduinoInterpreter arduinoInterpreter;

    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // 아두이노 uuid
    public static final byte[] arduinoHeader = {(byte)0xA6, 0x12};
    private String targetMacAddress;
    private String foundMacAdress;

    public void setTargetMacAddress(String targetMacAddress){
        this.targetMacAddress = targetMacAddress;
    }
    private static final String ARDUINO_BLUETOOTH_NAME = "CarflixArduino";

    private static final String TAG = "BluetoothThread" ;
    public static final String SEARCHING = "starting_service";
    public static final String FOUND_DEVICE = "found_device";
    public static final String SUCCESSFUL_CONNECTION = "successful_connection";
    public static final String FAILED_CONNECTION = "failed_connection";


    private boolean bluetoothSearch() {

        if (!bluetoothAdapter.isDiscovering()) {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.startDiscovery();
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                context.registerReceiver(onFoundReceiver, filter); //찾았을때 수행할 작업 등록
                return true;
            } else {
                Toast.makeText(context, "주변에 차량이 존재하지 않습니다.", Toast.LENGTH_LONG).show();
                return false; //블루투스가 켜져있지 않다.
            }
        }
        return true;
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method method = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) method.invoke(device, BT_MODULE_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }

    private boolean bluetoothConnect(){
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(foundMacAdress);
        try {
            bluetoothSocket = createBluetoothSocket(device);
            Log.i(TAG, "bluetooth connect: 연결 시도");
            bluetoothSocket.connect();
        }
        catch (IOException e) {
            Log.i(TAG, "bluetoothConnect: 연결 실패.");
            e.printStackTrace();
            return false;
        }
        Log.i(TAG, "bluetoothConnect: 연결 성공!");
        return true;
    }

    private boolean found = false;
    public final BroadcastReceiver onFoundReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if(!found) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {//찾았을때
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    String deviceName = device.getName(); //디바이스 이름
                    String deviceHardwareAddress = device.getAddress(); // MAC address

                    Log.i(TAG, "deviceName : " + deviceName + ", deviceHWAddress : " + deviceHardwareAddress);
                    if (!found && deviceName != null && deviceName.equals(ARDUINO_BLUETOOTH_NAME)){
                        if(targetMacAddress == null ||
                                (deviceHardwareAddress != null && deviceHardwareAddress.equals(targetMacAddress))) {
                            foundMacAdress = deviceHardwareAddress;
                            Log.i(TAG, "onReceive: 기기 발견");
                            found = true;
                            notifyToThread();
                        }
                    }
                }
            }
        }
    };

    private boolean isWait = false;
    public synchronized void waitUntilNotify(){
        isWait = true;
        while(isWait) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public synchronized void notifyToThread(){
        isWait = false;
        notifyAll();
    }

    abstract public void onStateUpdate(String state);

    abstract public void onConnected(ArduinoInterpreter arduinoInterpreter, String macAddress);

    abstract public void onConnectionFailed();

    abstract public void onBluetoothNotOn();

    public void onReceive(ArduinoData arduinoData, ArduinoInterpreter arduinoInterpreter){

    }

    @Override
    public void run() {
        if (!bluetoothSearch()) {
            onBluetoothNotOn();
            return;
        }
        onStateUpdate(SEARCHING);
        waitUntilNotify(); //기기 발견때까지 기다림
        bluetoothAdapter.cancelDiscovery();
        context.unregisterReceiver(onFoundReceiver);
        onStateUpdate(FOUND_DEVICE);
        if (bluetoothConnect()) {
            onStateUpdate(SUCCESSFUL_CONNECTION);
            arduinoInterpreter = new ArduinoInterpreter(bluetoothSocket); //아두이노 통신 개체 생성
            onConnected(arduinoInterpreter, foundMacAdress);
        } else {
            onStateUpdate(FAILED_CONNECTION);
            Log.i(TAG, "run: 블루투스 연결 실패");
            onConnectionFailed();
        }
    }

    public void endConnection(){
        if(arduinoInterpreter != null){
            arduinoInterpreter.terminate();
        }
        if(isAlive())
            interrupt();
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
            context.unregisterReceiver(onFoundReceiver);
        }
    }

    public class ArduinoInterpreter extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private InputStream inputStream;
        private OutputStream outputStream;

        private boolean mIsReceived = false;
        private ArduinoData receivedData = null;

        public boolean isReceived() {
            return mIsReceived;
        }

        public ArduinoData getReceivedData() {
            return receivedData;
        }

        public void listenNext(){
            mIsReceived = false;
            myNotify();
        }

        boolean stopped = false;

        public ArduinoInterpreter(BluetoothSocket socket) {
            bluetoothSocket = socket;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                inputStream = null;
                outputStream = null;
                e.printStackTrace();
            }
        }

        private boolean makeReceivedData(byte preparedHeaderCode) throws IOException, InterruptedException {
            int toRead;
            switch(preparedHeaderCode){
                case ArduinoData.RS_CARCTL:
                case ArduinoData.S_SUCBC:
                    toRead = 1;
                    break;
                case ArduinoData.S_REQON_AVAIL:
                    toRead = 33;
                    break;
                case ArduinoData.S_REQCONT_AVAIL:
                    toRead = 33;
                    break;
                case ArduinoData.S_REQSEND_STATE:
                    toRead = 16;
                    break;
                case ArduinoData.S_REQSEND_OFF:
                    toRead = 16;
                    break;
                case ArduinoData.S_ASSIGN_ID_OK:
                case ArduinoData.S_DELETE_OK:
                    receivedData = new ArduinoData(preparedHeaderCode);
                    return true;
                default:
                    return false;
            }
            byte[] slicedData = new byte[toRead];
            int readData = 0;
            while(readData < toRead) {
                if(inputStream.available() > 0) {
                    readData += inputStream.read(slicedData, readData, toRead - readData);
                    Log.i(TAG, "makeReceivedData: read-l"+ Arrays.toString(slicedData));
                    Thread.sleep(50);
                }
            }
            receivedData = new ArduinoData(preparedHeaderCode, slicedData);
            return true;
        }
        private boolean isMyWait = false;
        private synchronized void myWait(){
            isMyWait = true;
            while(isMyWait) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private synchronized void myNotify(){
            isMyWait = false;
            notifyAll();
        }
        @Override
        public void run() {
            byte[] buffer = new byte[3];
            // Keep listening to the InputStream until an exception occurs
            while (!stopped) {
                try {
                    if (!mIsReceived) {
                        try {
                            if (inputStream.available() > 0) {
                                inputStream.read(buffer, 0, 1); // 첫번째 바이트를 읽는다.
                                Log.i(TAG, "read-1[AD]: " + buffer[0]);

                                if (buffer[0] == arduinoHeader[0]) { //첫번째 바이트가 일치한다면
                                    while (inputStream.available() <= 0) // 두번째 바이트까지 대기
                                        Thread.sleep(100);
                                    inputStream.read(buffer, 0, 1); // 두번째 바이트를 읽는다.
                                    Log.i(TAG, "read-2[AD]: " + buffer[0]);

                                    if (buffer[0] == arduinoHeader[1]) { // 두번째 바이트가 일치한다면
                                        while (inputStream.available() <= 0) // 세번째 바이트까지 대기
                                            Thread.sleep(100);
                                        inputStream.read(buffer, 0, 1); //세번째 바이트를 읽는다.
                                        Log.i(TAG, "read-3[Header]: " + buffer[0]);
                                        if (makeReceivedData(buffer[0])) { //모든 데이터를 읽고 인스턴스로 저장
                                            mIsReceived = true;
                                            onReceive(receivedData, this);
                                            myWait();
                                        }
                                        else{
                                            Log.i(TAG, "run: 오류");
                                            Thread.sleep(300);
                                            sendToArduino(new ArduinoData.Builder()
                                                    .setResend()
                                                    .build()
                                            );
                                        }
                                    }
                                }
                            }
                        } catch (IOException e) {
                            if (!stopped)
                                e.printStackTrace();
                            break;
                        }
                    }
                }
                catch (InterruptedException e){
                }
            }
        }
        public void startListening(){
            start();
        }

        public boolean sendToArduino(ArduinoData arduinoData) {
            byte[] toSend = new byte[1024];
            int ti = 0, i;

            for(i = 0; i < arduinoHeader.length; i++)
                toSend[ti++] = arduinoHeader[i]; //아두이노 헤더

            toSend[ti++] = arduinoData.getHeaderCode(); //헤더 번호

            byte[] data = arduinoData.getData();
            if(data != null) {
                for (i = 0; i < data.length; i++)
                    toSend[ti++] = data[i]; //데이터
            }

            try {
                outputStream.write(toSend, 0, ti);
                outputStream.flush();
                return true;
            }
            catch (IOException e) {
                return false;
            }
        }

        public void terminate() {
            stopped = true;
            if(isAlive())
                interrupt();
            try {
                bluetoothSocket.close();
            } catch (IOException e) {

            }
        }
    }
}


