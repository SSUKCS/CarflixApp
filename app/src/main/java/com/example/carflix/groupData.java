package com.example.carflix;
//차량 데이터 클래스
//carImg = 차량의 이미지
//carName = 차량의 이름
public class groupData {
    String groupName;
    String groupDescription;
    String status;
    public groupData(String groupName, String groupDescription, String status){
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.status = status;
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
    public void setStatus(String status){ this.status = status; }
}
