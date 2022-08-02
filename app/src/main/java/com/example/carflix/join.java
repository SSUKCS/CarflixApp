package com.example.carflix;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

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
    
    RadioButton gender_male;
    RadioButton gender_female;
    
    Button joinButton;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join);
        
        connectUI();
        isEnableID.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //서버에 아이디 전송, 비교하여 이미 존재하는 id일 경우 false 반환
            }
        });
        isEnablePassWord.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //적합한 비밀번호 양식인지 확인, 적합하지 않다면 false 반환
            }
        });
        joinButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                /*1. 아이디가 중복되지 않고
                * 2. 비밀번호가 적합한 양식이며
                * 3. 비밀번호 재확인과 비밀번호의 editText 내용이 동일하며
                * 4. 내용이 전부 작성되어있는 경우에 회원 가입 요청을 서버로 전달*/
                String userid = useridEdit.getText().toString();
                String password = passwordEdit.getText().toString();

                
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

        gender_male = (RadioButton) findViewById(R.id.gender_male);
        gender_female = (RadioButton) findViewById(R.id.gender_female);

        joinButton = (Button) findViewById(R.id.joinButton);
    }
}
