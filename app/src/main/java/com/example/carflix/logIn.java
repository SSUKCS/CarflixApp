package com.example.carflix;

import android.content.Intent;
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
        String mb_userid_saved="userIDexample";
        String mb_password_saved="passwordExample";

        String mb_userid_correct="userIDexample";
        String mb_password_correct="passwordExample";

        logInButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(isAutoLogin.isChecked()) {
                    inputID.setText(mb_userid_saved);
                    inputPW.setText(mb_password_saved);
                }
                String mb_userid = inputID.getText().toString();
                String mb_password =inputPW.getText().toString();

                JSONObject userInfo = new JSONObject();
                try{
                    userInfo.put("mb_userid", mb_userid);
                    userInfo.put("mb_password", mb_password);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e("json", "생성한 json : " + userInfo.toString()); //log로 JSON오브젝트가 잘생성되었는지 확인

                try {
                    if(userInfo.getString("mb_userid").equals(mb_userid_correct)&&
                            userInfo.getString("mb_password").equals(mb_password_correct)) {
                        Log.e("인증", "성공");
                        Intent intent = new Intent(getApplicationContext(), groupList.class);
                        startActivity(intent);
                    }
                    else {
                        Log.e("인증", "실패");
                        Toast.makeText(getApplicationContext(), "다시 입력해 주십시오",Toast.LENGTH_LONG).show();
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
