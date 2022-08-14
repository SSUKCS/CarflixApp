package com.example.carflix;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class ceoGroupData extends groupData{
    protected String career;
    protected String certificate;
    protected String companyRegisterNumber;

    public ceoGroupData(String groupName, String groupDescription, String status){
        super(groupName, groupDescription, status);
    }
    public ceoGroupData(JSONObject groupData){
        super();
        String groupStatus=null;

        try{
            switch(groupData.getString("status")){
                case "ceo_group":groupStatus =  "cg";break;
                case "rent_group":groupStatus = "rg";break;
            }
            this.career = groupData.getString(groupStatus+"_career");
            this.certificate = groupData.getString(groupStatus+"_certificate");
            this.companyRegisterNumber = groupData.getString(groupStatus+"_company_registernumber");
        }
        catch(JSONException e){
            Log.e("ceo_groupData", e.toString());
        }
    }
    protected ceoGroupData(){
        super();
    };
    public String getCareer() {return career;}
    public void setCareer(String career) {this.career = career;}
    public String getCertificate() {return certificate;}
    public void setCertificate(String certificate) {this.certificate = certificate;}
    public String getCompanyRegisterNumber() {return companyRegisterNumber;}
    public void setCompanyRegisterNumber(String companyRegisterNumber) {this.companyRegisterNumber = companyRegisterNumber;}
}
