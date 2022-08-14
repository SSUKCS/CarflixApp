package com.example.carflix;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class logIn extends AppCompatActivity{
    Button logInButton;
    EditText inputID;
    EditText inputPW;
    CheckBox isAutoLogin;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        logInButton = (Button)findViewById(R.id.sendInfotoServer);
        inputID = (EditText)findViewById(R.id.inputID);
        inputPW = (EditText)findViewById(R.id.inputPW);
        isAutoLogin = (CheckBox)findViewById(R.id.isAutoLogin);

        //저장되어있는 userid와 password
        SharedPreferences autoLogin = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        //서버단에 저장되어있는 내용
        String mb_userid_correct="userIDexample";
        String mb_password_correct="passwordExample";

        inputID.setText("");
        inputPW.setText("");

        logInButton.setOnClickListener(new View.OnClickListener(){
            /*
            1. inputID와 inputPW에 적혀있는 내용을 각각 mb_userid와 mb_password에 저장
            2. mb_userid와 mb_password를 userInfo json객체에 저장
            3. json객체를 서버로 보내 인증 시도
            4. 인증이 성공할경우 groupList 로 이동
             */
            @Override
            public void onClick(View view) {

                String mb_userid = inputID.getText().toString();
                String mb_password =inputPW.getText().toString();
                if(isAutoLogin.isChecked()) {
                    Log.d("autoLogin", "isAutoLogin");

                    Log.d("autoLogin", autoLogin.getString(getString(R.string.savedIDKey), getString(R.string.savedIDKey_noValue)));
                    Log.d("autoLogin",autoLogin.getString(getString(R.string.savedPWKey), getString(R.string.savedPWKey_noValue)));
                    //자동 로그인 시, 저장되어있는 userid와 password가 없는 경우
                    if(autoLogin.getString(getString(R.string.savedIDKey), getString(R.string.savedIDKey_noValue)).equals(getString(R.string.savedIDKey_noValue))
                            && autoLogin.getString(getString(R.string.savedPWKey), getString(R.string.savedPWKey_noValue)).equals(getString(R.string.savedPWKey_noValue))) {
                        if(inputID.length()!=0 && inputPW.length()!=0){
                            SharedPreferences.Editor editor = autoLogin.edit();
                            editor.putString(getString(R.string.savedIDKey), mb_userid);
                            editor.putString(getString(R.string.savedPWKey), mb_password);
                            editor.apply();
                        }
                    }
                    else
                    {
                        if(inputID.length()==0 && inputPW.length()==0){
                            inputID.setText(autoLogin.getString(getString(R.string.savedIDKey), getString(R.string.savedIDKey_noValue)));
                            inputPW.setText(autoLogin.getString(getString(R.string.savedPWKey), getString(R.string.savedPWKey_noValue)));
                            mb_userid = inputID.getText().toString();
                            mb_password =inputPW.getText().toString();
                        }
                    }
                }
                if(inputID.length()!=0 && inputPW.length()!=0){
                    serverData serverData = new serverData("GET", "member/login_v3", "mb_userid="+mb_userid+"&mb_password="+mb_password, null);
                    String[] result = serverData.get().split("/");
                    Log.d("login", "LOGIN RESULT :: "+serverData.get());
                    switch(result[0]){
                        case "Successfully Login!":
                            if(isAutoLogin.isChecked()){
                                SharedPreferences autoLogin = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = autoLogin.edit();
                                editor.putString(getString(R.string.savedIDKey), mb_userid);
                                editor.putString(getString(R.string.savedPWKey), mb_password);
                                editor.apply();
                            }
                            String small_groupDataJSONString = new serverData("GET", "small_group/group_info", "mb_id="+result[1], null).get();
                            String ceo_groupDataJSONString = new serverData("GET", "ceo_group/group_info", "mb_id="+result[1], null).get();
                            String rent_groupDataJSONString = new serverData("GET", "rent_group/group_info", "mb_id="+result[1], null).get();
                            Intent intent = new Intent(getApplicationContext(), groupList.class);
                            intent.putExtra("small_groupDataJSONString", small_groupDataJSONString);
                            intent.putExtra("ceo_groupDataJSONString", ceo_groupDataJSONString);
                            intent.putExtra("rent_groupDataJSONString", rent_groupDataJSONString);
                            startActivity(intent);
                            break;
                        case "Invalid Username or Password!":break;
                        default:Log.e("login", "LOGIN ERROR :: invalid output");break;
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "아이디와 비밀번호를 입력해 주세요.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
