package com.example.carflix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Base64;
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

import java.io.ByteArrayInputStream;
//userData
//{
//    "mb_id": 사용자의 ID값
//    "mb_userid": 사용자 아이디,
//    "mb_password": 사용자 비밀번호,
//    "mb_email": 사용자의 이메일,
//    "mb_phone": "사용자의 휴대폰 번호,
//    "mb_nickname": 사용자의 별명,
//    "mb_image": 사용자 이미지,
//    "mb_is_admin": 시스템 관리자인지 여부,
//    "mb_register_car": 소유한 그룹 내에 차량 등록 여부,
//    "mb_lastlogin_datetime": 마지막으로 로그인한 시간,
//    "mb_regdate": 회원가입한 시간
//}
//groupData
//{
//    creatorID;
//    groupID;
//    groupName;
//    groupDescription;
//    status;
//    career;
//    certificate;
//    companyRegisterNumber;



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

    private ImageView userImage;
    private TextView userName;
    private TextView userEmail;

    private View usageView;
    private View outView;
    private View deleteCarView;
    private View settingsView;

    private Animation menuOpenAnim;
    private Animation menuCloseAnim;
    private Animation onDarkAnim;
    private Animation offDarkAnim;

    boolean menuOpen;

    private JSONObject userData;
    private JSONObject groupData;
    private String inviteCode = "No Invite Code";
    private boolean isGroupCreator;

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
                Context context = baseActivity.getApplicationContext();
                Intent intent;
                String baseActivityClass = baseActivity.getClass().toString();
                switch(baseActivityClass){
                    case "class com.example.carflix.GroupList"://프로필 변경
                        intent = new Intent(context, ChangeProfile.class);
                        Log.d(baseActivityClass, "USERDATA :: "+ userData.toString());
                        intent.putExtra("userData", userData.toString());
                        baseActivity.startActivity(intent);
                        break;
                    case "class com.example.carflix.CarList"://초대코드 생성
                        intent = new Intent(context, GenerateCode.class);
                        Log.d(baseActivityClass, "USERDATA :: "+ userData.toString());
                        try{
                            intent.putExtra("memberID", userData.getString("mb_id"));
                            intent.putExtra("groupID", userData.getString("group_id"));
                            intent.putExtra("groupName", userData.getString("group_name"));
                            intent.putExtra("status", userData.getString("status"));
                            baseActivity.startActivity(intent);
                        }
                        catch(JSONException e){
                            Log.e(baseActivityClass+"ProfileMenu", e.toString());
                        }
                        break;
                }
            }
        });
        outView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor;
                switch(baseActivity.getClass().toString()){
                    case "class com.example.carflix.GroupList"://로그아웃
                        //1.저장되어있는 id/비밀번호 초기화
                        Context context = baseActivity.getApplicationContext();
                        SharedPreferences autoLogin = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                        editor = autoLogin.edit();
                        editor.putString(context.getString(R.string.savedIDKey), context.getString(R.string.savedIDKey_noValue));
                        editor.putString(context.getString(R.string.savedPWKey), context.getString(R.string.savedPWKey_noValue));
                        editor.commit();
                        quitProgram();
                        break;
                    case "class com.example.carflix.CarList"://그룹 탈퇴
                        if(isGroupCreator&&inviteCode.equals("No Invite Code")){
                            //사용자가 그룹 생성자일 경우에만 가능
                            try{
                                String status = groupData.getString("status");
                                JSONObject requestBody = new JSONObject();
                                switch(status){
                                    case"small_group": requestBody.put("sg_id", groupData.getString("groupID"));break;
                                    case"ceo_group": requestBody.put("cg_id", groupData.getString("groupID"));break;
                                    case"rent_group": requestBody.put("rg_id", groupData.getString("groupID"));break;
                                }
                                ServerConnectionThread serverConnectionThread = new ServerConnectionThread("DELETE", status+"/delete", requestBody);
                                serverConnectionThread.start();
                            }
                            catch(JSONException e){
                                Log.e(baseActivity.getClass()+"ProfileMenu", e.toString());
                            }
                            closeRightMenu();
                            baseActivity.finish();
                        }
                        else{
                            String message="";
                            //사용자가 그룹에 초대된 사람인 경우
                            //1.invitecode, memberID를 인자로 code-car 제거
                            try{
                                JSONObject requestBody = new JSONObject();
                                requestBody.put("ic_number", inviteCode);
                                requestBody.put("mb_id", userData.getString("mb_id"));
                                ServerConnectionThread serverConnectionThread = new ServerConnectionThread("DELETE", "code_car/delete", requestBody);
                                serverConnectionThread.start();
                                serverConnectionThread.join();
                                message = new JSONObject(serverConnectionThread.getResult()).getString("message");
                            }
                            catch(JSONException | InterruptedException e){
                                Log.e(baseActivity.getClass()+"ProfileMenu", e.toString());
                            }
                            if(message.equals("group deleted")){
                                //2. shardpreference에 저장되어있는 invitecode 삭제
                                /*
                                 * savedInviteGroupData  -   small_group:[{"ic_number": "...."}, ....],
                                 *                           ceo_group:[{"ic_number": "...."}, ....],
                                 *                           rent_group:[{"ic_number": "...."}, ....]
                                 */
                                SharedPreferences savedInviteGroupData = baseActivity.getSharedPreferences(baseActivity.getString(R.string.invite_Group_Data), baseActivity.MODE_PRIVATE);
                                String savedGroupJSONArrayString = "";
                                try{
                                    switch(groupData.getString("status")){
                                        case"small_group":
                                            savedGroupJSONArrayString = savedInviteGroupData.getString(baseActivity.getString(R.string.smallGroupKey), baseActivity.getString(R.string.groupDataKey_noValue));
                                            break;
                                        case"ceo_group":
                                            savedGroupJSONArrayString = savedInviteGroupData.getString(baseActivity.getString(R.string.ceoGroupKey), baseActivity.getString(R.string.groupDataKey_noValue));
                                            break;
                                        case"rent_group":
                                            savedGroupJSONArrayString = savedInviteGroupData.getString(baseActivity.getString(R.string.rentGroupKey), baseActivity.getString(R.string.groupDataKey_noValue));
                                            break;
                                    }

                                    Log.d(baseActivity.getClass()+"ProfileMenu_delete_savedInviteGroupData", "BEFORE :: "+savedGroupJSONArrayString);
                                    String targetString = "{\"ic_number\":\""+inviteCode+"\"}";
                                    Integer index = savedGroupJSONArrayString.indexOf(targetString);
                                    if(index>=0){
                                        Log.d(baseActivity.getClass()+"ProfileMenu", "index :: "+index);

                                        if(index == 1){//맨 앞에 있는 경우
                                            if(savedGroupJSONArrayString.length()==targetString.length()+2)
                                                savedGroupJSONArrayString = savedGroupJSONArrayString.replace(targetString,"");
                                            else
                                                savedGroupJSONArrayString = savedGroupJSONArrayString.replace(targetString+",","");
                                        }
                                        else{
                                            savedGroupJSONArrayString = savedGroupJSONArrayString.replace(","+targetString,"");
                                        }
                                    }
                                    Log.d(baseActivity.getClass()+"ProfileMenu_delete_savedInviteGroupData", "AFTER :: "+savedGroupJSONArrayString);
                                    editor = savedInviteGroupData.edit();
                                    String status = groupData.getString("status");
                                    switch(status){
                                        case"small_group":
                                            editor.putString(baseActivity.getString(R.string.smallGroupKey), savedGroupJSONArrayString);
                                            break;
                                        case"ceo_group":
                                            editor.putString(baseActivity.getString(R.string.ceoGroupKey), savedGroupJSONArrayString);
                                            break;
                                        case"rent_group":
                                            editor.putString(baseActivity.getString(R.string.rentGroupKey), savedGroupJSONArrayString);
                                            break;
                                    }
                                    editor.apply();
                                }
                                catch(JSONException e){
                                    Log.e(baseActivity.getClass()+"ProfileMenu_delete_savedInviteGroupData", e.toString());
                                }
                            }
                            else{
                                Log.d(baseActivity.getClass()+"ProfileMenu_delete_savedInviteGroupData", "server failed to delete data");
                            }
                            closeRightMenu();
                            baseActivity.finish();
                        }
                        break;
                }
            }
        });
        if(baseActivity.getClass().toString().equals("class com.example.carflix.CarList")){
            deleteCarView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CarList)baseActivity).setMode(true);
                    closeRightMenu();
                }
            });
        }
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

        menuOpen = false;

        userImage = baseActivity.findViewById(R.id.userImage);
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
                //만약 그룹 생성자일 경우 "그룹 삭제"로 text설정
                ((TextView)baseActivity.findViewById(R.id.outText)).setText("그룹 탈퇴");
                ((ImageView)baseActivity.findViewById(R.id.outImg)).setImageResource(R.drawable.ic_group_off);
                deleteCarView = baseActivity.findViewById(R.id.deleteCarView);
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
            String userImageBase64 = userData.getString("mb_image");
            Log.d("settingProfile", "mb_image :: "+userImageBase64);
            if(!userImageBase64.equals("")){

                byte[] image = Base64.decode(userImageBase64, 0);

                //byte[] 데이터  stream 데이터로 변환 후 bitmapFactory로 이미지 생성
                ByteArrayInputStream inStream = new ByteArrayInputStream(image);
                Bitmap bitmap = BitmapFactory.decodeStream(inStream) ;
                userImage.setImageBitmap(bitmap);
            }
            else{
                userImage.setImageResource(R.drawable.userimage1_default);
            }
        }
        catch(IllegalArgumentException e){//ava.lang.IllegalArgumentException: bad base-64
            userImage.setImageResource(R.drawable.userimage1_default);
            Log.e("ProfileMenu_settingProfile_getImage", e.toString());
        }
        catch(JSONException e){
            Log.e("ProfileMenu_settingProfile_getImage", e.toString());
        }
        try{
            userName.setText(userData.getString("mb_nickname")/*+("mb의 직책")*/);
            userEmail.setText(userData.getString("mb_email"));
        }
        catch(JSONException e){
            Log.e("ProfileMenu", e.toString());
        }
    }
    public void settingProfile(JSONObject userData, JSONObject groupData){
        settingProfile(userData);
        this.groupData = groupData;
        try{
            String memberID = userData.getString("mb_id");
            String creatorID = groupData.getString("creatorID");
            Log.d("ProfileMenu_settingProfile", "memberID :: "+memberID+", creatorID :: "+ creatorID);
            isGroupCreator =memberID.equals(creatorID);
            if(isGroupCreator){
                //사용자 == 생성자 => 그룹 삭제 가능
                ((TextView)baseActivity.findViewById(R.id.outText)).setText("그룹 삭제");
                baseActivity.findViewById(R.id.deleteCarView).setVisibility(View.VISIBLE);
            }
            else{
                baseActivity.findViewById(R.id.deleteCarView).setVisibility(View.INVISIBLE);
            }
        }
        catch(JSONException e){
            Log.e("ProfileMenu_settingProfile", e.toString());
        }
    }
    public void setInviteCode(String inviteCode){
        this.inviteCode = inviteCode;
        Log.d("ProfileMenu_setInviteCode", inviteCode);
    }
}
