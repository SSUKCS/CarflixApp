package com.example.carflix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //20220726: 아직 로그인/회원가입을 구현하지 않았으므로
        //로그인 버튼 클릭시 바로 carList 화면으로 이동
        //로그인 버튼 클릭시 로그인 화면으로 이동
        Button logInButton = (Button)findViewById(R.id.logInButton);
        logInButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),carList.class);
                Toast.makeText(getApplicationContext(), "로그인", Toast.LENGTH_LONG).show();
                startActivity(intent);
            }
        });

        //회원가입 버튼 클릭시 회원가입 화면으로 이동
       Button joinButton = (Button)findViewById(R.id.joinButton);
        joinButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), logIn.class);
                Toast.makeText(getApplicationContext(), "회원가입", Toast.LENGTH_LONG).show();
                startActivity(intent);
            }
        });
    }
}