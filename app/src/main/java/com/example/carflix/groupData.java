package com.example.carflix;

import java.io.Serializable;
import java.util.ArrayList;

//차량 데이터 클래스
//carImg = 차량의 이미지
//carName = 차량의 이름
public class groupData implements Serializable {
    private String groupName;
    private String groupDescription;
    private String status;
    private ArrayList<carData> carDataList;
    public groupData(String groupName, String groupDescription, String status){
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.status = status;

        carDataList = new ArrayList<>();
        //챠량 데이터 입력단(임의로 생성)
        int carimg_default = R.drawable.carimage_default;
        for(int i=1;i<11;i++){
            carDataList.add(new carData(carimg_default, groupName+"의 차량"+i));
        }
    }
    public groupData(String groupName, String groupDescription, String status, ArrayList<carData> carDataList){
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.status = status;
        this.carDataList = carDataList;
    }
    public String getGroupName(){
        return groupName;
    }
    public void setGroupname(String carName){
        this.groupName = carName;
    }
    public String getGroupDescription(){
        return groupDescription;
    }
    public void setGroupDescription(String groupDescription){this.groupDescription = groupDescription; }
    public String getStatus(){ return status; }
    public void setStatus(String status){ this.status = status;}
    public ArrayList<carData> getCarDataList(){return carDataList;}
    public void setCarDataList(ArrayList<carData> carDataList){this.carDataList = carDataList;}
}
