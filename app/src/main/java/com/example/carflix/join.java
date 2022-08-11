package com.example.carflix;

import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class join extends AppCompatActivity{
    
    EditText useridEdit;
    
    Button isEnableID;
    TextView isEnableID_result;

    EditText passwordEdit;
    
    Button isEnablePassWord;
    TextView isEnablePassWord_result;

    EditText checkPasswordEdit;

    EditText nickNameEdit;

    EditText emailEdit;

    EditText phoneEdit;

    RadioButton gender_male;
    RadioButton gender_female;
    
    Button joinButton;

    private boolean allEditTextisNotnull=false;
    private boolean checkPasswordisOK;
    private String emailRegex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    private boolean checkEmailisOK;

    Handler handler = new Handler();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join);
        
        connectUI();
        isEnableID.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //서버에 아이디 전송, 비교하여 이미 존재하는 id일 경우 false 반환
                String command = "member/read";
                ConnectionThread thread = new ConnectionThread("GET", "member/read", null);
                try{
                    thread.join();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                String result = thread.getResult();
                Log.d("join", "RESULT :: "+result);
                thread.start();
                Log.d("onClick","아이디 확인 버튼이 클릭되었습니다.");            }
        });
        isEnablePassWord.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //적합한 비밀번호 양식인지 확인, 적합하지 않다면 false 반환
                Log.d("onClick","비밀번호 확인 버튼이 클릭되었습니다.");
            }
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
                    Log.d("생성 불가","비밀번호를 동일하게 입력해야 합니다.");
                }
                if(!checkEmailisOK)
                {
                    Log.d("생성 불가","이메일을 형식에 맞게 입력해야 합니다.");
                }
                if(allEditTextisNotnull&&checkPasswordisOK&&checkEmailisOK)
                {
                    String userid = useridEdit.getText().toString();
                    String password = passwordEdit.getText().toString();
                    String nickName = nickNameEdit.getText().toString();
                    String phone = phoneEdit.getText().toString();
                    String regdate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    int userImg = R.drawable.userimage_default;
                    Log.d("userid:",userid);
                    Log.d("password", password);
                    Log.d("phone", phone);
                    Log.d("nickName", nickName);
                    Log.d("regdate", regdate);
                    if(gender_male.isChecked()){
                        userImg = R.drawable.userimage1_default;
                    }
                    if(gender_female.isChecked()){
                        userImg = R.drawable.userimage2_default;
                    }
                    Log.d("userimg", Integer.toString(userImg));

                    JSONObject userInfo = new JSONObject();
                    try{
                        userInfo.put("mb_userid", userid);
                        userInfo.put("mb_password", password);
                        userInfo.put("mb_nickname", nickName);
                        userInfo.put("mb_phone", phone);
                        userInfo.put("mb_image", userImg);
                        userInfo.put("mb_regdate", regdate);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.e("json", "생성한 json : " + userInfo.toString());
                    //서버와 연결
                    String command = "member/read";
                    ConnectionThread thread = new ConnectionThread("POST", "member/create", userInfo);
                    thread.start();
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

        isEnablePassWord = (Button) findViewById(R.id.isEnablePassWord);
        isEnablePassWord_result = (TextView) findViewById(R.id.isEnablePassWord_result);

        checkPasswordEdit = (EditText) findViewById(R.id.checkPasswordEdit);

        nickNameEdit = (EditText) findViewById(R.id.nickNameEdit);

        emailEdit = (EditText) findViewById(R.id.emailEdit);
        phoneEdit.addTextChangedListener(new TextWatcher() {

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
