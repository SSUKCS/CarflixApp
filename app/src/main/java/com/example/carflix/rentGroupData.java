package com.example.carflix;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
//{
//    "mb_id" :,
//    "cg_career" :,
//    "cg_certificate" :,
//    "cg_company_registernumber" :,
//    "cg_title" :,
//    "cg_description" :,
//    "status" :,
//    "cg_regdate" :
//}
public class rentGroupData extends ceoGroupData{
    public rentGroupData(JSONObject groupData) {super(groupData);}
}