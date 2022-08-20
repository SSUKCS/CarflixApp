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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity{
    Button logInButton;
    EditText inputID;
    EditText inputPW;
    TextView failedLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        logInButton = (Button)findViewById(R.id.sendInfotoServer);
        inputID = (EditText)findViewById(R.id.inputID);
        inputPW = (EditText)findViewById(R.id.inputPW);
        failedLogin = (TextView)findViewById(R.id.failedLogin);
        //저장되어있는 userid와 password
        SharedPreferences autoLogin = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);




        logInButton.setOnClickListener(new View.OnClickListener(){
            /*
            1. inputID와 inputPW에 적혀있는 내용을 각각 mb_userid와 mb_password에 저장
            2. mb_userid와 mb_password를 userInfo json객체에 저장
            3. json객체를 서버로 보내 인증 시도
            4. 인증이 성공할경우 groupList 로 이동
             */
            @Override
            public void onClick(View view) {

                String mb_userid = null;
                String mb_password =null;

                //저장되어있는 userid와 password가 있는 경우
                if(savedIDPWExist(autoLogin)) {
                    mb_userid = autoLogin.getString(getString(R.string.savedIDKey), getString(R.string.savedIDKey_noValue));
                    mb_password =autoLogin.getString(getString(R.string.savedPWKey), getString(R.string.savedPWKey_noValue));
                }
                //저장되어있는 userid와 password가 없는 경우
                else
                {
                    if(inputIDPWExist()){
                        mb_userid = inputID.getText().toString();
                        mb_password =inputPW.getText().toString();
                    }
                }
                Log.d("login", "USER ID :: "+mb_userid);
                Log.d("login", "USER PASSWORD :: "+mb_password);
                if(savedIDPWExist(autoLogin)||inputIDPWExist()){
                    ServerData serverData = new ServerData("GET", "member/login_v3", "mb_userid="+mb_userid+"&mb_password="+mb_password, null);

                    try{
                        JSONObject userData;
                        userData = new JSONObject(serverData.get());
                        Log.d("login", "LOGIN RESULT :: "+userData);
                        switch(userData.getString("message")){
                            case "Successfully Login!":
                                Intent intent = new Intent(getApplicationContext(), GroupList.class);
                                intent.putExtra("userData", userData.toString());
                                startActivity(intent);
                                break;
                            case "Invalid Username or Password!":
                                break;
                            default:Log.e("login", "LOGIN ERROR :: invalid output");break;
                        }
                    }
                    catch(JSONException e){
                        Log.e("MainActivity", e.toString());
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "아이디와 비밀번호를 입력해 주세요.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private boolean savedIDPWExist(SharedPreferences autoLogin){
        //저장되어있는 userid와 password가 있는경우 true 반환
        return !(autoLogin.getString(getString(R.string.savedIDKey), getString(R.string.savedIDKey_noValue)).
                equals(getString(R.string.savedIDKey_noValue))
                && autoLogin.getString(getString(R.string.savedPWKey), getString(R.string.savedPWKey_noValue)).
                equals(getString(R.string.savedPWKey_noValue)));
    }
    private boolean inputIDPWExist(){
        //inputID와 input password가 둘 다 비어있을 경우 false 반환
        return (inputID.length()!=0 && inputPW.length()!=0);
    }
}
