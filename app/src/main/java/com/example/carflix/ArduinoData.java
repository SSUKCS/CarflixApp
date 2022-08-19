package com.example.carflix;

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

    public static final byte DOOR_OPEN = 1;
    public static final byte DOOR_CLOSE = 2;
    public static final byte TRUNK_OPEN = 3;
    public static final byte TRUNK_CLOSE = 4;
    public static final byte CLEAR = 11;
    public static final byte EXIST_CR_ID = 12;

    private byte headerCode;

    public byte[] getData() {
        return data;
    }

    private byte[] data;

    public ArduinoData(){
        this.headerCode = 0;
        this.data = null;
    }

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

    public class AvailData{
        private String crId;
        private String mbId;
        private byte how;

        public AvailData(String crId, String mbId) {
            this.crId = crId;
            this.mbId = mbId;
            this.how = 0;
        }

        public AvailData(String crId, String mbId, byte how) {
            this.crId = crId;
            this.mbId = mbId;
            this.how = how;
        }
        public String getCrId() {
            return crId;
        }
        public String getMbId() {
            return mbId;
        }
        public byte getHow() {
            return how;
        }
    }
    //아두이노로부터 받은 데이터를 얻는 메서드들
    AvailData getReqonAvail(){
        AvailData availData = new AvailData(
            new String(Arrays.copyOfRange(data,0,16)),
            new String(Arrays.copyOfRange(data, 16, data.length))
        );
        return availData;
    }

    String getReqsendState(){
        return new String(data);
    }

    String getReqsendOff(){
        return new String(data);
    }

    byte getSucbc(){
        return data[0];
    }

    AvailData getReqcontAvail(){
        AvailData availData = new AvailData(
            new String(Arrays.copyOfRange(data,0,16)),
            new String(Arrays.copyOfRange(data, 16, 32)),
            data[32]
        );
        return availData;
    }

    byte getRsCarctl(){
        return data[0];
    }

    public static class Builder{
        private byte headerCode;
        private byte[] data;

        ArduinoData build(){
            return new ArduinoData(this);
        }

        //아두이노로 보내기 위해 set 메서드 수행
        Builder setReqon(String mbId){
            this.headerCode = R_REQON;
            this.data = Arrays.copyOf(mbId.getBytes(), 16);
            return this;
        }

        Builder setStart(String vsStartupInformation){
            this.headerCode = R_START;
            this.data = Arrays.copyOf(vsStartupInformation.getBytes(), 50);
            return this;
        }

        Builder setReqbc(){
            this.headerCode = R_REQBC;
            this.data = null;
            return this;
        }

        Builder setAssignId(String crId){
            this.headerCode = R_ASSIGN_ID;
            this.data = Arrays.copyOf(crId.getBytes(), 16);
            return this;
        }

        Builder setDeleteId(String crId){
            this.headerCode = R_DELETE_ID;
            this.data = Arrays.copyOf(crId.getBytes(), 16);
            return this;
        }

        Builder setReqcont(String mbId, byte how){
            this.headerCode = R_REQCONT;
            this.data = Arrays.copyOf(mbId.getBytes(), 17);
            this.data[16] = how;
            return this;
        }

        Builder setCont(byte how){
            this.headerCode = R_CONT;
            this.data = new byte[1];
            this.data[0] = how;
            return this;
        }

        Builder setRsCarctl(byte b){
            this.headerCode = RS_CARCTL;
            this.data = new byte[1];
            this.data[0] = b;
            return this;
        }

        Builder setSendOffOk(){
            this.headerCode = R_OFF_OK;
            this.data = null;
            return this;
        }
    }

}
