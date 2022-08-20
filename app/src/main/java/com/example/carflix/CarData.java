package com.example.carflix;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import androidx.annotation.NonNull;

//차량 데이터 클래스
//carImg = 차량의 이미지
//carName = 차량의 이름
public class CarData implements Serializable {
    int carImg;
    String carName;
    String carID;
    String classification;
    String registerationNumber;
    String mac_address;
    boolean available;//vs_authentication_value - ok/
    String status;//vs_startup_infomation

    public CarData(JSONObject carData){
        //{
        //            "cr_id":,
        //            "cr_number_classification":,예)서울 5 바 : 서울(등록지역), 차종기호(승용차/승합차)/...., 용도기호(자가용/영업용)
        //            "cr_registeration_number":, 예) 1234
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
            available = true;
            status = "운전 가능";
        }
        catch(JSONException e){
            Log.e("carData", e.toString());
        }
    }
    public String getCarName(){
        return carName;
    }
    public void setCarName(String carName){ this.carName = carName;}
    public int getcarImg(){
        return carImg;
    }
    public void setCarImg(int carImg){ this.carImg = carImg;}
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
