package com.example.carflix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class ProfileMenu {
    private class ConsumeTouchEvent implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }
    private Activity baseActivity;

    private LinearLayout slideMenu;
    private View darkBackground;

    private TextView userName;
    private TextView userEmail;

    private View usageView;
    private View outView;
    private View settingsView;

    private Animation menuOpenAnim;
    private Animation menuCloseAnim;
    private Animation onDarkAnim;
    private Animation offDarkAnim;

    boolean menuOpen = false;

    private JSONObject userData;
    private void setMenuOpen(boolean opened){menuOpen = opened;}
    public boolean isMenuOpen(){return menuOpen;}

    public void openRightMenu() {
        setMenuOpen(true);
        darkBackground.setVisibility(View.VISIBLE);
        slideMenu.setVisibility(View.VISIBLE);
        slideMenu.startAnimation(menuOpenAnim);
        darkBackground.startAnimation(onDarkAnim);
    }
    public void closeRightMenu() {
        setMenuOpen(false);
        slideMenu.startAnimation(menuCloseAnim);
        darkBackground.startAnimation(offDarkAnim);
        darkBackground.setVisibility(View.GONE);
        slideMenu.setVisibility(View.GONE);
    }
    private void setTouchEventWhenSlide(){
        slideMenu.setOnTouchListener(new ConsumeTouchEvent());
        darkBackground.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isMenuOpen() && event.getAction() == 1) {
                    closeRightMenu();
                }
                return true;
            }
        });
    }
    private void setAnimation(){
        onDarkAnim = AnimationUtils.loadAnimation(baseActivity, R.anim.alpha_on);
        onDarkAnim.setFillAfter(true);
        offDarkAnim = AnimationUtils.loadAnimation(baseActivity, R.anim.alpha_off);
        menuOpenAnim = AnimationUtils.loadAnimation(baseActivity, R.anim.menu_open_to_right);
        menuOpenAnim.setFillAfter(true);
        menuCloseAnim = AnimationUtils.loadAnimation(baseActivity, R.anim.menu_close_to_left);
    }
    private void setMenuOption(){
        usageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String baseActivityClass = baseActivity.getClass().toString();
                switch(baseActivityClass){
                    case "class com.example.carflix.GroupList"://프로필 변경
                        break;
                    case "class com.example.carflix.CarList"://초대코드 생성
                        Context context = baseActivity.getApplicationContext();
                        Intent intent = new Intent(context, GenerateCode.class);
                        Log.d(baseActivityClass, "USERDATA :: "+ userData.toString());
                        try{
                            intent.putExtra("memberID", userData.getString("mb_id"));
                            intent.putExtra("groupID", userData.getString("group_id"));
                            intent.putExtra("groupName", userData.getString("group_name"));
                            intent.putExtra("status", userData.getString("status"));
                            baseActivity.startActivity(intent);
                        }
                        catch(JSONException e){
                            Log.e(baseActivity.getClass().toString()+"ProfileMenu", e.toString());
                        }
                        break;
                }
            }
        });
        outView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switch(baseActivity.getClass().toString()){
                    case "class com.example.carflix.GroupList"://로그아웃
                        //1.저장되어있는 id/비밀번호 초기화
                        Context context = baseActivity.getApplicationContext();
                        SharedPreferences autoLogin = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = autoLogin.edit();
                        editor.putString(context.getString(R.string.savedIDKey), context.getString(R.string.savedIDKey_noValue));
                        editor.putString(context.getString(R.string.savedPWKey), context.getString(R.string.savedPWKey_noValue));
                        editor.commit();
                        quitProgram();
                    case "class com.example.carflix.CarList"://그룹 탈퇴
                        break;
                }
            }
        });
        settingsView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
            }
        });
    }
    private void quitProgram(){
        baseActivity.moveTaskToBack(true);
        baseActivity.finish();
    }

    public ProfileMenu(Activity activity) {
        baseActivity = activity;
        slideMenu = baseActivity.findViewById(R.id.slide_menu);
        darkBackground = baseActivity.findViewById(R.id.dark_background);
        setTouchEventWhenSlide();
        setAnimation();
        userName = baseActivity.findViewById(R.id.userName);
        userEmail = baseActivity.findViewById(R.id.userEmail);
        Log.d("ProfileMenu", activity.getClass().toString());
        switch(activity.getClass().toString()){
            case "class com.example.carflix.GroupList":
                ((TextView)baseActivity.findViewById(R.id.usageText)).setText("프로필 변경");
                ((ImageView)baseActivity.findViewById(R.id.usageImg)).setImageResource(R.drawable.ic_round_person_24);
                ((TextView)baseActivity.findViewById(R.id.outText)).setText("로그아웃");
                ((ImageView)baseActivity.findViewById(R.id.outImg)).setImageResource(R.drawable.image_logout_resize);

                break;
            case "class com.example.carflix.CarList":
                ((TextView)baseActivity.findViewById(R.id.usageText)).setText("초대코드 생성");
                ((ImageView)baseActivity.findViewById(R.id.usageImg)).setImageResource(R.drawable.ic_round_car_rental);
                ((TextView)baseActivity.findViewById(R.id.outText)).setText("그룹 탈퇴");
                ((ImageView)baseActivity.findViewById(R.id.outImg)).setImageResource(R.drawable.ic_group_off);
                //만약 그룹 생성자일 경우 "그룹 삭제"로 text설정
                break;
        }

        usageView = baseActivity.findViewById(R.id.usageView);
        outView = baseActivity.findViewById(R.id.outView);
        settingsView = baseActivity.findViewById(R.id.settings);
        setMenuOption();
    }
    public void settingProfile(JSONObject userData){
        this.userData = userData;
        try{
            userName.setText(userData.getString("mb_nickname")/*+("mb의 직책")*/);
            userEmail.setText(userData.getString("mb_email"));
        }
        catch(JSONException e){
            Log.e("ProfileMenu", e.toString());
        }
    }
}
