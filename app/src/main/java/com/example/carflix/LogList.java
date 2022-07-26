package com.example.carflix;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class LogList {
    private class UserInfo{
        String userName;
        String status;
        String date;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        private String timeSynchronize(String date){
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(dateFormat.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            cal.add(Calendar.HOUR, 9);
            return dateFormat.format(cal.getTime());
        }
        UserInfo(String userName, String status, String date){
            this.userName = userName;
            this.status = status;
            this.date = timeSynchronize(date);
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
    public String getLocationToString(int position){
        double latitude = locationList.get(position).latitude;
        double longitude = locationList.get(position).longitude;
        return latitude+", "+longitude;
    }
    public String[] getUserInfo(int position){
        return userInfoList.get(position).get();
    }
    public boolean isEmpty(){
        if(size==0) return true;
        else if(size > 0) return false;
        else throw new ArrayIndexOutOfBoundsException(": Index: -1"+", Size: "+size);
    }
    public int getSize(){
        return size;
    }
    public void showByLog(){
        Log.d("LogList_showByLog", "SIZE :: "+size);
        for(int i=0;i<size;i++){
            Log.i("LogList_showByLog()", "ITEM["+i+"] :: "+ locationList.get(i)+"|"+userInfoList.get(i));
        }
        if(size==0){
            Log.i("LogList_showByLog()", "There's No Record");
        }
    }
}

