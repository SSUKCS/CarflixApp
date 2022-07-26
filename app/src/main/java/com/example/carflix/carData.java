package com.example.carflix;
//차량 데이터 클래스
//carImg = 차량의 이미지
//carName = 차량의 이름
public class carData {
    int carImg;
    String carName;

    public carData(int carImg, String carName){
        this.carImg = carImg;
        this.carName = carName;
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
}
