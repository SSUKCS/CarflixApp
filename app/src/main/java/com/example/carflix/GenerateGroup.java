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
