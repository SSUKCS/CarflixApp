package com.example.carflix;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ArduinoData {
    public static final byte RS_CARCTL = 25;
    public static final byte R_REQON = 57;
    public static final byte R_START = 59;
    public static final byte R_REQBC = 65;
    public static final byte R_ASSIGN_ID = 68;
    public static final byte R_DELETE_ID = 72;
    public static final byte R_REQCONT = 74;
    public static final byte R_CONT = 76;
    public static final byte S_REQON_AVAIL = 58;
    public static final byte S_REQSEND_STATE = 60;
    public static final byte S_REQSEND_OFF = 63;
    public static final byte S_SUCBC = 66;
    public static final byte S_ASSIGN_ID_OK = 69;
    public static final byte S_DELETE_OK = 73;
    public static final byte S_DELETE_FAILED = 80;
    public static final byte S_REQCONT_AVAIL = 75;
    public static final byte R_OFF_OK = 81;
    public static final byte R_RESEND = 21;

    public static final byte DOOR_OPEN = 1;
    public static final byte DOOR_CLOSE = 2;
    public static final byte TRUNK_OPEN = 3;
    public static final byte TRUNK_CLOSE = 4;
    public static final byte CLEAR = 11;
    public static final byte EXIST_CR_ID = 12;

    private byte headerCode;
    private static final String TAG = "ArduinoData";

    public byte[] getData() {
        return data;
    }

    private byte[] data;

    public ArduinoData(byte headerCode) {
        this.headerCode = headerCode;
        this.data = null;
    }

    public ArduinoData(byte headerCode, byte[] data) {
        this.headerCode = headerCode;
        this.data = data;
    }

    public ArduinoData(Builder builder){
        this.headerCode = builder.headerCode;
        this.data = builder.data;
    }

    public byte getHeaderCode() {
        return headerCode;
    }


    //아두이노로부터 받은 데이터를 얻는 메서드들
    long getReqonAvail(){
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(this.data);
        buffer.flip();//need flip
        Log.i(TAG, "getReqonAvail: crid : "+buffer.getLong());
        return buffer.getLong();
    }

    byte getSucbc(){
        return data[0];
    }

    long getReqcontAvail(){
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(this.data);
        buffer.flip();//need flip
        Log.i(TAG, "getReqonAvail: crid : "+buffer.getLong());
        return buffer.getLong();
    }


    public static class Builder{
        private byte headerCode;
        private byte[] data;

        ArduinoData build(){
            return new ArduinoData(this);
        }

        //아두이노로 보내기 위해 set 메서드 수행
        Builder setReqon(){
            this.headerCode = R_REQON;
            return this;
        }

        Builder setStart(){
            this.headerCode = R_START;
            return this;
        }

        Builder setReqbc(){
            this.headerCode = R_REQBC;
            return this;
        }

        Builder setAssignId(long crId){
            this.headerCode = R_ASSIGN_ID;
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.putLong(crId);
            this.data = buffer.array();
            return this;
        }

        Builder setDeleteId(long crId){
            this.headerCode = R_DELETE_ID;
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.putLong(crId);
            this.data = buffer.array();
            return this;
        }

        Builder setReqcont(){
            this.headerCode = R_REQCONT;
            return this;
        }

        Builder setCont(byte how){
            this.headerCode = R_CONT;
            this.data = new byte[1];
            this.data[0] = how;
            return this;
        }

        Builder setSendOffOk(){
            this.headerCode = R_OFF_OK;
            this.data = null;
            return this;
        }

        Builder setResend(){
            this.headerCode = R_RESEND;
            this.data = null;
            return this;
        }
    }

}
