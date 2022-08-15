package com.example.carflix;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import androidx.annotation.NonNull;

//차량 데이터 클래스
//carImg = 차량의 이미지
//carName = 차량의 이름
public class carData implements Serializable {
    int carImg;
    String carName;
    String carID;
    String classification;
    String registerationNumber;
    String mac_address;
    boolean available;//vs_authentication_value - ok/
    String status;//vs_startup_infomation

    public carData(int carImg, String carName){
        this.carImg = carImg;
        this.carName = carName;
        this.available = true;
        status = "운전 가능";
    }
    public carData(int carImg, String carID, String carName){
        this.carImg = carImg;
        this.carID = carID;
        this.carName = carName;
        this.available = true;
        status = "운전 가능";
    }
    public carData(JSONObject carData){
        //{
        //            "cr_id":,
        //            "cr_number_classification":,
        //            "cr_registeration_number":,
        //            "cr_carname":,
        //            "cr_mac_address":,
        //            "cr_regdate":
        //        },
        this.carImg = R.drawable.carimage_default;
        try{
            this.carID = carData.getString("cr_id");
            this.classification = carData.getString("cr_number_classification");
            this.registerationNumber = carData.getString("cr_registeration_number");
            this.carName = carData.getString("cr_carname");
            this.mac_address = carData.getString("cr_mac_address");
            status = "운전 가능";
        }
        catch(JSONException e){
            Log.e("carData", e.toString());
        }


    }
    public String getCarName(){
        return carName;
    }
    public void setCarName(String carName){
        this.carName = carName;
    }
    public int getcarImg(){
        return carImg;
    }
    public void setCarImg(int carImg){
        this.carImg = carImg;
    }
    public boolean isAvailable(){return available;}
    public void setAvailable(boolean isAvailable){this.available = isAvailable;}
    public String getStatus(){return status;}
    public void setStatus(String status){this.status = status;}
    public String getCarID() {return carID;}
    public void setCarID(String carID) {this.carID = carID;}
    public String getClassification() {return classification;}
    public void setClassification(String classification) {this.classification = classification;}
    public String getMac_address() {return mac_address;}
    public void setMac_address(String mac_address) {this.mac_address = mac_address;}
    public String getRegisterationNumber() {return registerationNumber;}
    public void setRegisterationNumber(String registerationNumber) {this.registerationNumber = registerationNumber;}
}
