package com.example.carflix;

import android.util.Log;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ConnectThread extends Thread{
    String hostName;
    public ConnectThread(String addr){
        hostName = addr;
    }
    public void run()
    {
        try{

            int port = 11001;
            Socket socket = new Socket(hostName, port);
            DataOutputStream outstream = new DataOutputStream(socket.getOutputStream());
            outstream.writeChars("Hello, there!");
            outstream.flush();

            DataInputStream instream = new DataInputStream(socket.getInputStream());

            Log.d("MainActivity", "서버에서 받은 메시지 : "+instream.read());

            socket.close();
            Log.e("thread", "thread 끝");
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
