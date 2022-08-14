package com.example.carflix;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class join extends AppCompatActivity{
    
    EditText useridEdit;
    
    Button isEnableID;
    TextView isEnableID_result;

    EditText passwordEdit;

    EditText checkPasswordEdit;

    EditText nickNameEdit;

    EditText emailEdit;

    EditText phoneEdit;

    RadioButton gender_male;
    RadioButton gender_female;
    
    Button joinButton;

    private boolean allEditTextisNotnull=false;
    private boolean checkIDisOK=false;
    private boolean checkPasswordisOK=false;
    private String emailRegex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    private boolean checkEmailisOK=false;

    Handler handler = new Handler();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join);
        
        connectUI();
        isEnableID.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String params =  "mb_userid="+useridEdit.getText().toString();
                serverData data = new serverData("GET", "member/show_single_name", params, null);
                //아이디가 입력되지 않았을 경우 false 반환
                if(useridEdit.length()==0){
                    isEnableID_result.setText("아이디가 입력되지 않았습니다.");
                    isEnableID_result.setTextColor(Color.RED);
                    checkIDisOK = false;
                }
                //서버에 아이디 전송, 비교하여 이미 존재하는 id일 경우 false 반환
                else if(data.get().equals(useridEdit.getText().toString()))
                {
                    isEnableID_result.setText("이미 존재하는 아이디 입니다.");
                    isEnableID_result.setTextColor(Color.RED);
                    checkIDisOK = false;
                }
                else
                {
                    isEnableID_result.setText("사용 가능한 아이디 입니다.");
                    isEnableID_result.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.teal_200));
                    checkIDisOK = true;
                }
                Log.d("onClick","아이디 확인 버튼이 클릭되었습니다.");}
        });
        joinButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                /*1. 아이디(userid)가 중복되지 않고
                * 2. 비밀번호(password)가 적합한 양식이며
                * 3. 비밀번호 재확인과 비밀번호의 editText 내용이 동일하며
                * 4. 내용이 전부 작성되어있는 경우에 회원 가입 요청을 서버로 전달*/
                Log.d("onClick","회원 가입 버튼이 클릭되었습니다.");
                if(useridEdit.length()!=0 && passwordEdit.length()!=0 &&
                        checkPasswordEdit.length()!=0 && nickNameEdit.length()!=0 && emailEdit.length()!=0 && phoneEdit.length()!=0) {
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
                if(allEditTextisNotnull&&checkIDisOK&&checkPasswordisOK&&checkEmailisOK)
                {
                    serverData data = new serverData("GET", "member/read", null);
                    String userid = useridEdit.getText().toString();
                    String password = passwordEdit.getText().toString();
                    String email = emailEdit.getText().toString();
                    String phone = phoneEdit.getText().toString();
                    String nickName = nickNameEdit.getText().toString();
                    String regdate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    int userImg = R.drawable.userimage_default;
                    Log.d("userid:",userid);
                    Log.d("password", password);
                    Log.d("email", email);
                    Log.d("phone", phone);
                    Log.d("nickName", nickName);
                    if(gender_male.isChecked()){
                        userImg = R.drawable.userimage1_default;
                    }
                    if(gender_female.isChecked()){
                        userImg = R.drawable.userimage2_default;
                    }
                    Log.d("userimg", Integer.toString(userImg));
                    Log.d("regdate", regdate);
                    JSONObject userInfo = new JSONObject();
                    try{
                        userInfo.put("mb_userid", userid);
                        userInfo.put("mb_password", password);
                        userInfo.put("mb_email", email);
                        userInfo.put("mb_phone", phone);
                        userInfo.put("mb_nickname", nickName);
                        userInfo.put("mb_image", userImg);
                        userInfo.put("mb_is_admin", "n");
                        userInfo.put("mb_register_car", "n");
                        userInfo.put("mb_lastlogin_datetime", regdate);
                        userInfo.put("mb_regdate", regdate);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.e("json", "생성한 json : " + userInfo);
                    //서버와 연결
                    serverConnectionThread thread = new serverConnectionThread("POST", "member/create", userInfo);
                    thread.start();
                    Intent intent = new Intent(getApplicationContext(), groupList.class);
                    startActivity(intent);
                }
            }
        });
    }
    private void connectUI()
    {
        useridEdit = (EditText) findViewById(R.id.useridEdit);

        isEnableID = (Button) findViewById(R.id.isEnableID);
        isEnableID_result = (TextView) findViewById(R.id.isEnableID_result);

        passwordEdit = (EditText) findViewById(R.id.passwordEdit);

        checkPasswordEdit = (EditText) findViewById(R.id.checkPasswordEdit);

        nickNameEdit = (EditText) findViewById(R.id.nickNameEdit);

        emailEdit = (EditText) findViewById(R.id.emailEdit);
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
        phoneEdit.addTextChangedListener(new PhoneNumberFormattingTextWatcher("KR"));

        gender_male = (RadioButton) findViewById(R.id.gender_male);
        gender_female = (RadioButton) findViewById(R.id.gender_female);

        joinButton = (Button) findViewById(R.id.joinButton);
    }
}
