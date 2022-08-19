package com.example.carflix;

import android.app.Activity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

    private View invitationListView;
    private View friendsView;
    private View myAccountsView;
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
        invitationListView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Toast.makeText(baseActivity.getApplicationContext(), "초대 목록 기능", Toast.LENGTH_SHORT).show();
            }
        });
        friendsView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Toast.makeText(baseActivity.getApplicationContext(), "친구목록 기능", Toast.LENGTH_SHORT).show();
            }
        });
        myAccountsView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Toast.makeText(baseActivity.getApplicationContext(), "내 계정 기능", Toast.LENGTH_SHORT).show();
            }
        });
        settingsView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Toast.makeText(baseActivity.getApplicationContext(), "설정 기능", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public ProfileMenu(Activity activity) {
        baseActivity = activity;
        slideMenu = baseActivity.findViewById(R.id.slide_menu);
        darkBackground = baseActivity.findViewById(R.id.dark_background);
        setTouchEventWhenSlide();
        setAnimation();
        userName = baseActivity.findViewById(R.id.userName);
        userEmail = baseActivity.findViewById(R.id.userEmail);

        invitationListView = baseActivity.findViewById(R.id.invitation_list);
        friendsView = baseActivity.findViewById(R.id.friends);
        myAccountsView = baseActivity.findViewById(R.id.my_account);
        settingsView = baseActivity.findViewById(R.id.settings);
        setMenuOption();
    }
    public void settingProfile(JSONObject userData){
        this.userData = userData;
        try{
            userName.setText(userData.getString("mb_nickname"));
            userEmail.setText(userData.getString("mb_email"));
        }
        catch(JSONException e){
            Log.e("ProfileMenu", e.toString());
        }
    }
}
