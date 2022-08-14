package com.example.carflix;

import java.io.Serializable;

//차량 데이터 클래스
//carImg = 차량의 이미지
//carName = 차량의 이름
public class carData implements Serializable {
    int carImg;
    String carName;
    int carID;
    boolean Available;//vs_authentication_value - ok/
    String status;//vs_startup_infomation

    public carData(int carImg, String carName){
        this.carImg = carImg;
        this.carName = carName;
        this.Available = true;
        status = "운전 가능";
    }
    public carData(int carImg, int carID, String carName){
        this.carImg = carImg;
        this.carID = carID;
        this.carName = carName;
        this.Available = true;
        status = "운전 가능";
    }
    public String getCarName(){
        return carName;
    }
    public void setCarname(String carName){
        this.carName = carName;
    }
    public int getcarImg(){
        return carImg;
    }
    public void setCarImg(int carImg){
        this.carImg = carImg;
    }
    public boolean isAvailable(){return Available;}
    public void setAvailable(boolean isAvailable){this.Available = isAvailable;}
    public String getStatus(){return status;}
    public void setStatus(String status){this.status = status;}
}
