package com.example.carflix;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


//처음 시작 화면
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionbar = getSupportActionBar();
        actionbar.hide();
        //20220726: 아직 로그인/회원가입을 구현하지 않았으므로
        //로그인 버튼 클릭시 바로 carList 화면으로 이동
        //로그인 버튼 클릭시 로그인 화면으로 이동
        Button logInButton = (Button)findViewById(R.id.logInButton);
        logInButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                SharedPreferences autoLogin = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                if(savedIDPWExist(autoLogin)){
                    String mb_userid = autoLogin.getString(getString(R.string.savedIDKey), getString(R.string.savedIDKey_noValue));
                    String mb_password = autoLogin.getString(getString(R.string.savedPWKey), getString(R.string.savedPWKey_noValue));
                    ServerData serverData = new ServerData("GET", "member/login_v3", "mb_userid="+mb_userid+"&mb_password="+mb_password, null);
                    try{
                        JSONObject result;
                        result = new JSONObject(serverData.get());
                        Log.d("login", "LOGIN RESULT :: "+result);
                        switch(result.getString("message")){
                            case "Successfully Login!":
                                Intent intent = new Intent(getApplicationContext(), GroupList.class);
                                intent.putExtra("mb_id", result.getString("mb_id"));
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
                else{
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    startActivity(intent);
                }
            }
        });

        //회원가입 버튼 클릭시 회원가입 화면으로 이동
       Button joinButton = (Button)findViewById(R.id.joinButton);
        joinButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Join.class);
                startActivity(intent);
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
}