package com.example.carflix;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LogList {
    private class UserInfo{
        String userName;
        String status;
        String date;
        UserInfo(String userName, String status, String date){
            this.userName = userName;
            this.status = status;
            this.date = date;
        }
        public void set(String userName, String status, String date){
            this.userName = userName;
            this.status = status;
            this.date = date;
        }
        public String[] get(){
            return new String[]{userName, status, date};
        }
    }//userName(String), status(String), date(String)
    private int size;
    private List<LatLng> locationList;
    private List<UserInfo> userInfoList;
    LogList(){
        size = 0;
        locationList = new ArrayList<>();
        userInfoList = new ArrayList<>();
    }
    public void add(LatLng location, String userName, String status, String date){
        locationList.add(location);
        userInfoList.add(new UserInfo(userName, status, date));
        size +=1;
    }
    public void remove(int position){
        if(size>0){
            locationList.remove(position);
            userInfoList.remove(position);
            size -=1;
        }
        else throw new ArrayIndexOutOfBoundsException(": Index: -1"+", Size: "+size);
    }
    public LatLng getLocation(int position){
        return locationList.get(position);
    }
    public String[] getUserInfo(int position){
        return userInfoList.get(position).get();
    }
    public boolean isEmpty(){
        if(size==0) return true;
        else return false;
    }
    public int getSize(){
        return size;
    }
    public void showByLog(){
        for(int i=0;i<size;i++){
            Log.i("LogList", "ITEM["+i+"] :: "+ locationList.get(i)+"|"+userInfoList.get(i));
        }
    }
}

