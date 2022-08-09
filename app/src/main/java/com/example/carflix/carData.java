package com.example.carflix;

import java.io.Serializable;

//차량 데이터 클래스
//carImg = 차량의 이미지
//carName = 차량의 이름
public class carData implements Serializable {
    int carImg;
    String carName;
    boolean Available;
    String status;

    public carData(int carImg, String carName){
        this.carImg = carImg;
        this.carName = carName;
        this.Available = true;
        status = "운전 가능";
    }
    public carData(int carImg, String carName, String status){
        this.carImg = carImg;
        this.carName = carName;
        this.status = status;
    }
    public carData(int carImg, String carName, boolean isAvailable){
        this.carImg = carImg;
        this.carName = carName;
        this.Available = isAvailable;
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
