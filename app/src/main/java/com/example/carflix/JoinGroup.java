package com.example.carflix;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class JoinGroup extends AppCompatActivity {
    EditText inviteCodeEditText;
    Button joinButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_group);
        connectUI();
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
    private void connectUI(){
        inviteCodeEditText = findViewById(R.id.inviteCodeEditText);
        joinButton = findViewById(R.id.joinGroupButton);
    }
}
