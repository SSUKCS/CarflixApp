package com.example.carflix;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class GenerateGroup extends AppCompatActivity {

    Button smallGroupButton;
    Button ceoGroupButton;
    Button rentGroupButton;

    LinearLayout smallGroupLayout;
    EditText smallGroupNameEdit;
    EditText smallGroupInfoEdit;

    LinearLayout bigGroupLayout;
    EditText bigGroupNameEdit;
    EditText bigGroupCompRegNum;
    EditText bigGroupCareerEdit;
    EditText bigGroupInfoEdit;
    TextView bigGroupCertificateEdit;
    Button findGroupCertificate;

    Button generateGroupButton;

    private String generateGroupType;
    private Uri groupCertificateURI;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generate_group);
        connectUI();
        generateGroupType = "sg";
        changeView(generateGroupType);
    }
    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getData()!=null)
                        groupCertificateURI = result.getData().getData();
                }
            }
    );
    private void connectUI(){
        smallGroupButton = (Button) findViewById(R.id.smallGroupButton);
        ceoGroupButton = (Button) findViewById(R.id.ceoGroupButton);
        rentGroupButton = (Button) findViewById(R.id.rentGroupButton);

        smallGroupLayout = (LinearLayout) findViewById(R.id.smallGroupLayout);
        smallGroupNameEdit = (EditText) findViewById(R.id.smallGroupNameEdit);
        smallGroupInfoEdit = (EditText) findViewById(R.id.smallGroupInfoEdit);

        bigGroupLayout = (LinearLayout) findViewById(R.id.bigGroupLayout);
        bigGroupNameEdit = (EditText) findViewById(R.id.bigGroupNameEdit);
        bigGroupCompRegNum = (EditText) findViewById(R.id.bigGroupCompRegNum);
        bigGroupCareerEdit = (EditText) findViewById(R.id.bigGroupCareerEdit);
        bigGroupInfoEdit = (EditText) findViewById(R.id.bigGroupInfoEdit);
        bigGroupCertificateEdit = (TextView)findViewById(R.id.bigGroupCertificateEdit);

        findGroupCertificate = (Button) findViewById(R.id.findGroupCertificate);
        generateGroupButton = (Button) findViewById(R.id.generateGroupButton);


        smallGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateGroupType = "sg";
                changeView(generateGroupType);
            }
        });
        ceoGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateGroupType = "cg";
                changeView(generateGroupType);
            }
        });
        rentGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateGroupType = "rg";
                changeView(generateGroupType);
            }
        });
        findGroupCertificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findCertificate();
            }
        });
        generateGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateGroup();
            }
        });
    }
    private void findCertificate(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        launcher.launch(intent);
    }
    private void changeView(String generateGroupType){
        switch(generateGroupType){
            case "sg":
                smallGroupButton.setBackgroundColor(ContextCompat.getColor(this, R.color.sg_color));
                ceoGroupButton.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
                rentGroupButton.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
                bigGroupLayout.setVisibility(View.INVISIBLE);
                smallGroupLayout.setVisibility(View.VISIBLE);
                break;
            case "cg":
                smallGroupButton.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
                ceoGroupButton.setBackgroundColor(ContextCompat.getColor(this, R.color.cg_color));
                rentGroupButton.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
                smallGroupLayout.setVisibility(View.INVISIBLE);
                bigGroupLayout.setVisibility(View.VISIBLE);
                break;
            case "rg":
                smallGroupButton.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
                ceoGroupButton.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
                rentGroupButton.setBackgroundColor(ContextCompat.getColor(this, R.color.rg_color));
                smallGroupLayout.setVisibility(View.INVISIBLE);
                bigGroupLayout.setVisibility(View.VISIBLE);
                break;
        }
    }
    private void generateGroup(){
        boolean isViewVisible=false;
        switch(generateGroupType){
            case "sg":
                isViewVisible = (smallGroupLayout.getVisibility()==View.VISIBLE);
                break;
            case "cg":
            case "rg":
                isViewVisible = (bigGroupLayout.getVisibility()==View.VISIBLE);
                break;
        }
        Log.d("generateGroup", "layoutVisibility"+isViewVisible);
        if(isViewVisible){
            ServerConnectionThread thread = new ServerConnectionThread("POST", "small_group/create", generateGroupJSONData(generateGroupType));
            thread.start();
            try{
                thread.join();
            }
            catch(Exception e){
                Log.e("generateGroupItem_thread.join()", e.toString());
            }
            Log.d("generateGroup", thread.getResult());
        }
    }
    private JSONObject generateGroupJSONData(String generateGroupType){
        JSONObject groupData = new JSONObject();
        String groupName;
        String groupInfo;
        String status=null;
        switch(generateGroupType){
            case "sg":
                status = "small_group";
                groupName = smallGroupNameEdit.getText().toString();
                groupInfo = smallGroupInfoEdit.getText().toString();
                if(smallGroupNameEdit.length()!=0&&smallGroupInfoEdit.length()!=0)
                {
                    try{
                        groupData.put("mb_id", getIntent().getStringExtra("memberID"));
                        groupData.put(generateGroupType+"_title", groupName);
                        groupData.put(generateGroupType+"_description", groupInfo);
                        groupData.put("status", status);
                    }
                    catch(JSONException e){
                        Log.e("generateGroup_generateGroupItem", e.toString());
                    }
                }
                else Toast.makeText(this, "누락된 정보가 존재합니다.", Toast.LENGTH_SHORT).show();
                break;
            case "cg":status = "ceo_group";
            case "rg":
                if(bigGroupNameEdit.length()!=0 && bigGroupInfoEdit.length()!=0 && bigGroupCareerEdit.length()!=0 &&
                        bigGroupCompRegNum.length()!=0&&bigGroupCertificateEdit.length()!=0){
                    if(status==null)status="rent_group";
                    groupName = bigGroupNameEdit.getText().toString();
                    groupInfo = bigGroupInfoEdit.getText().toString();
                    String career = bigGroupCareerEdit.getText().toString();
                    String registerNumber = bigGroupCompRegNum.getText().toString();
                    String certificate = bigGroupCertificateEdit.getText().toString();
                    try{
                        groupData.put("mb_id", getIntent().getStringExtra("memberID"));
                        groupData.put(generateGroupType+"_career", career);
                        groupData.put(generateGroupType+"_certificate", certificate);
                        groupData.put(generateGroupType+"_company_registernumber", registerNumber);
                        groupData.put(generateGroupType+"_title", groupName);
                        groupData.put(generateGroupType+"_description", groupInfo);
                        groupData.put("status", status);
                    }
                    catch(JSONException e){
                        Log.e("generateGroup_generateGroupItem", e.toString());
                    }
                }
                else Toast.makeText(this, "누락된 정보가 존재합니다.", Toast.LENGTH_SHORT).show();
                break;
        }
        Log.d("generateGroup_generateGroupJSONData", "JSONDATA :: "+ groupData);
        return groupData;
    }
}
