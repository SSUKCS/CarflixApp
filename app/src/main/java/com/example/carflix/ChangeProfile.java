package com.example.carflix;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class ChangeProfile extends AppCompatActivity{

    TextView isEnableID_result;

    EditText passwordEdit;
    EditText checkPasswordEdit;

    EditText nickNameEdit;
    private String nickNameHint;
    EditText emailEdit;
    private String emailHint;
    EditText phoneEdit;
    private String phoneHint;

    RadioButton gender_male;
    RadioButton gender_female;
    
    Button joinButton;

    JSONObject userData;

    private boolean allEditTextisNotnull=false;
    private boolean checkPasswordisOK=false;
    private String emailRegex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    private boolean checkEmailisOK=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_profile);
        try{
            userData = new JSONObject(getIntent().getStringExtra("userData"));
            nickNameHint = userData.getString("mb_nickname");
            emailHint = userData.getString("mb_email");
            phoneHint = userData.getString("mb_phone");
        }
        catch(JSONException e){
            Log.e("ChangeProfile", e.toString());
        }
        connectUI();
        joinButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                /*1. 아이디(userid)가 중복되지 않고
                * 2. 비밀번호(password)가 적합한 양식이며
                * 3. 비밀번호 재확인과 비밀번호의 editText 내용이 동일하며
                * 4. 내용이 전부 작성되어있는 경우에 회원 가입 요청을 서버로 전달*/
                Log.d("onClick","변경 내용이 입력되었습니다.");
                if(passwordEdit.length()!=0 && checkPasswordEdit.length()!=0 &&
                        nickNameEdit.length()!=0 && emailEdit.length()!=0 && phoneEdit.length()!=0) {
                    allEditTextisNotnull = true;
                }
                else
                {
                    Log.d("생성 불가","공란이 존재합니다.");
                    allEditTextisNotnull = false;
                }
                checkPasswordisOK = passwordEdit.getText().toString().equals(checkPasswordEdit.getText().toString());
                if(!checkPasswordisOK)
                {
                    Toast.makeText(getApplicationContext(), "비밀번호를 동일하게 입력해야 합니다.", Toast.LENGTH_SHORT).show();
                    Log.d("생성 불가","비밀번호를 동일하게 입력해야 합니다.");
                }
                if(!checkEmailisOK)
                {
                    Toast.makeText(getApplicationContext(), "이메일을 형식에 맞게 입력해야 합니다.", Toast.LENGTH_SHORT).show();
                    Log.d("생성 불가","이메일을 형식에 맞게 입력해야 합니다.");
                }
                if(allEditTextisNotnull&&checkPasswordisOK&&checkEmailisOK)
                {
                    String changedPassword = passwordEdit.getText().toString();
                    String changedEmail = emailEdit.getText().toString();
                    String changedPhone = phoneEdit.getText().toString();
                    String changedNickName = nickNameEdit.getText().toString();
                    int userImg = R.drawable.userimage1_default;

                    if(gender_male.isChecked()){
                        userImg = R.drawable.userimage3_default;
                    }
                    if(gender_female.isChecked()){
                        userImg = R.drawable.userimage2_default;
                    }
                    //이미지를 base64로 변환(bitmap -> base64)
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), userImg);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    byte[] byteArray = outputStream.toByteArray();
                    String base64EncodingImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

                    JSONObject userInfo = new JSONObject();
                    try{
                        //"mb_userid" : "testtesttest12341213414",
                        //    "mb_password" : "testtesttest12341213414",
                        //    "mb_email" : "testtesttest12341213414@test.com",
                        //    "mb_phone" : "testtesttest12341213414",
                        //    "mb_nickname" : "testtesttest12341213414",
                        //    "mb_image" : "testtesttest12341213414",
                        //    "mb_is_admin" : "testtesttest12341213414",
                        //    "mb_register_car" : "y",
                        //    "mb_id": "28"
                        userInfo.put("mb_userid",userData.get("mb_userid"));
                        userInfo.put("mb_password", changedPassword);
                        userInfo.put("mb_email", changedEmail);
                        userInfo.put("mb_phone", changedPhone);
                        userInfo.put("mb_nickname", changedNickName);
                        userInfo.put("mb_image", base64EncodingImage);
                        userInfo.put("mb_is_admin", userData.get("mb_is_admin"));
                        userInfo.put("mb_register_car", userData.get("mb_register_car"));
                        userInfo.put("mb_id",userData.get("mb_id"));
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //서버와 연결
                    ServerConnectionThread thread = new ServerConnectionThread("PUT", "member/update", userInfo);
                    thread.start();
                    try{
                        thread.join();
                    }
                    catch(InterruptedException e){
                        Log.e("ChangeProfile_join", e.toString());
                    }
                    String result = thread.getResult();
                    Log.d("ChangeProfile", "RESULT :: " + result);
                    finish();
                }
            }
        });
    }
    private void connectUI()
    {
        passwordEdit = (EditText) findViewById(R.id.passwordEdit);
        checkPasswordEdit = (EditText) findViewById(R.id.checkPasswordEdit);

        nickNameEdit = (EditText) findViewById(R.id.nickNameEdit);
        nickNameEdit.setHint(nickNameHint);
        emailEdit = (EditText) findViewById(R.id.emailEdit);
        emailEdit.setHint(emailHint);
        emailEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = emailEdit.getText().toString().trim();
                if(email.matches(emailRegex))checkEmailisOK=true;
                else checkEmailisOK = false;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        phoneEdit = (EditText) findViewById(R.id.phoneEdit);
        phoneEdit.setHint(phoneHint);
        phoneEdit.addTextChangedListener(new PhoneNumberFormattingTextWatcher("KR"));

        gender_male = (RadioButton) findViewById(R.id.gender_male);
        gender_female = (RadioButton) findViewById(R.id.gender_female);

        joinButton = (Button) findViewById(R.id.joinButton);
    }
}
