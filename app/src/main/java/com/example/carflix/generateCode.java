package com.example.carflix;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class generateCode extends AppCompatActivity {
    TextView inviteCode;
    Button generateCodeButton;
    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generate_code);
        inviteCode = findViewById(R.id.inviteCode);
        generateCodeButton = findViewById(R.id.generateCodeButton);
        generateCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //서버로부터 코드를 받아온다.
                String command = "member/read";
                serverConnectionThread thread = new serverConnectionThread("GET", command, null);
                thread.setMonitor(handler, inviteCode);
                thread.start();
            }
        });
    }
}
