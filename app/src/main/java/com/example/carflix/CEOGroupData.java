package com.example.carflix;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

//{
//    "mb_id" :,
//    "cg_title" :,
//    "cg_description" :,
//    "status" :,

//    "cg_career" :,
//    "cg_certificate" :,
//    "cg_company_registernumber" :,
//    "cg_regdate" :
//}
public class CEOGroupData extends SmallGroupData {
    protected String career;
    protected String certificate;
    protected String companyRegisterNumber;

    public CEOGroupData(JSONObject groupData){
        super(groupData);
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
    protected CEOGroupData(){
        super();
    };
    @Override
    protected JSONObject toJSONObject(){
        JSONObject  jsonObject = super.toJSONObject();
        try{
            jsonObject.put("career", career);
            jsonObject.put("certificate", certificate);
            jsonObject.put("companyRegisterNumber", companyRegisterNumber);
        }
        catch(JSONException e){
            Log.d(getClass().getSimpleName(), e.toString());
        }
        return jsonObject;
    }
    @Override
    public String toJSONString(){
        return super.toJSONString();
    }
    @Override
    public String getCreatorID(){return super.getCreatorID();}
    @Override
    public String getGroupID() {return super.getGroupID();}
    @Override
    public String getGroupName() {return super.getGroupName();}
    @Override
    public void setGroupName(String carName) {super.setGroupName(carName);}
    @Override
    public String getGroupDescription() {return super.getGroupDescription();}
    @Override
    public void setGroupDescription(String groupDescription) {super.setGroupDescription(groupDescription);}
    @Override
    public String getStatus() {return super.getStatus();}
    @Override
    public void setStatus(String status) {super.setStatus(status);}

    public String getCareer() {return career;}
    public void setCareer(String career) {this.career = career;}
    public String getCertificate() {return certificate;}
    public void setCertificate(String certificate) {this.certificate = certificate;}
    public String getCompanyRegisterNumber() {return companyRegisterNumber;}

    public void setCompanyRegisterNumber(String companyRegisterNumber) {this.companyRegisterNumber = companyRegisterNumber;}

}
