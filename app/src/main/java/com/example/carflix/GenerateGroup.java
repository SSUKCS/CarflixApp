package com.example.carflix;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class GenerateGroup extends AppCompatActivity {
    ContentResolver contentResolver;

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
    TextView bigGroupCertificateURI;
    Button findGroupCertificate;

    Button generateGroupButton;

    private String generateGroupType;
    private Uri groupCertificateURI;
    private String base64EncodingImage;
    private String defaultImage = "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";

    LoadingDialog dialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generate_group);

        dialog = new LoadingDialog(this);

        contentResolver = getContentResolver();
        connectUI();
        generateGroupType = "sg";
        changeView(generateGroupType);
    }
    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == RESULT_OK && result.getData()!=null) {
                        groupCertificateURI = result.getData().getData();
                        bigGroupCertificateURI.setText(groupCertificateURI.toString());
                        Log.d("GenerateGroup_launcher", "groupCertificateURI : "+groupCertificateURI);
                        //1.uri -> bitmap
                        Bitmap bitmap=null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, groupCertificateURI);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //bitmap->base64
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        byte[] byteArray = outputStream.toByteArray();
                        base64EncodingImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
                    }
                    else{
                        if(result.getResultCode() != RESULT_OK)
                            Log.d("GenerateGroup_launcher", "RESULT_NOT_OK");
                        if(result.getData()==null)
                            Log.d("GenerateGroup_launcher", "result.getData() is null");
                    }
                }
            }
    );
    private void connectUI(){
        smallGroupButton = findViewById(R.id.smallGroupButton);
        ceoGroupButton = findViewById(R.id.ceoGroupButton);
        rentGroupButton = findViewById(R.id.rentGroupButton);

        smallGroupLayout = findViewById(R.id.smallGroupLayout);
        smallGroupNameEdit = findViewById(R.id.smallGroupNameEdit);
        smallGroupInfoEdit = findViewById(R.id.smallGroupInfoEdit);

        bigGroupLayout = findViewById(R.id.bigGroupLayout);
        bigGroupNameEdit = findViewById(R.id.bigGroupNameEdit);
        bigGroupCompRegNum = findViewById(R.id.bigGroupCompRegNum);
        bigGroupCareerEdit = findViewById(R.id.bigGroupCareerEdit);
        bigGroupInfoEdit = findViewById(R.id.bigGroupInfoEdit);
        bigGroupCertificateURI = findViewById(R.id.bigGroupCertificateURI);

        findGroupCertificate = findViewById(R.id.findGroupCertificate);
        generateGroupButton = findViewById(R.id.generateGroupButton);


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
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
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
        if(isViewVisible){
            String command = "/create";
            switch(generateGroupType){
                case "sg": command = "small_group"+command;break;
                case "cg": command = "ceo_group"+command;break;
                case "rg": command = "rent_group"+command;break;
            }
            ServerConnectionThread thread = new ServerConnectionThread("POST", command, generateGroupJSONData(generateGroupType));
            thread.start();
            try{
                thread.join();
            }
            catch(InterruptedException e){
                Log.e("thread.join()", e.toString());
            }
            Log.d("thread.result", thread.getResult());
        }
        finish();
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
                break;
            case "cg":status = "ceo_group";
            case "rg":
                if(bigGroupNameEdit.length()!=0 && bigGroupInfoEdit.length()!=0 && bigGroupCareerEdit.length()!=0 &&
                        bigGroupCompRegNum.length()!=0&&bigGroupCertificateURI.length()!=0){
                    if(status==null)status="rent_group";
                    groupName = bigGroupNameEdit.getText().toString();
                    groupInfo = bigGroupInfoEdit.getText().toString();
                    String career = bigGroupCareerEdit.getText().toString();
                    String registerNumber = bigGroupCompRegNum.getText().toString();
                    try{
                        groupData.put("mb_id", getIntent().getStringExtra("memberID"));
                        groupData.put(generateGroupType+"_career", career);
                        groupData.put(generateGroupType+"_certificate", base64EncodingImage);
                        groupData.put(generateGroupType+"_company_registernumber", registerNumber);
                        groupData.put(generateGroupType+"_title", groupName);
                        groupData.put(generateGroupType+"_description", groupInfo);
                        groupData.put("status", status);
                        Log.d("jsonData", groupData.getString("mb_id"));
                        Log.d("jsonData", groupData.getString(generateGroupType+"_career"));
                        Log.d("jsonData", groupData.getString(generateGroupType+"_certificate"));
                        Log.d("jsonData", groupData.getString(generateGroupType+"_company_registernumber"));
                        Log.d("jsonData", groupData.getString(generateGroupType+"_title"));
                        Log.d("jsonData", groupData.getString(generateGroupType+"_description"));
                        Log.d("jsonData", groupData.getString("status"));

                    }
                    catch(JSONException e){
                        Log.e("generateGroup_generateGroupItem", e.toString());
                    }
                }
                else{
                    Toast.makeText(this, "누락된 정보가 존재합니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return groupData;
    }
}
