package com.example.carflix;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executor;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

public class CarList extends AppCompatActivity {
    private final static int DEFAULT = -1;

    private Context context;

    private ProfileMenu profileMenu;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    private ArrayList<CarData> carDataList;
    private CarListAdapter adapter;
    private RecyclerView carListView;
    private TextView listEmpty;

    private String memberID;
    private String groupID;
    private String groupName;
    private JSONObject userData;
    private JSONObject groupData;
    private String status;

    private int nowDriving = -1;

    Integer carImg_default = R.drawable.carimage_default;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_list);
        context = getApplicationContext();

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(CarList.this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(getApplicationContext(),R.string.auth_error_message, Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(getApplicationContext(), R.string.auth_success_message, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(getApplicationContext(), R.string.auth_fail_message, Toast.LENGTH_SHORT).show();
                    }
                });
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("지문 인증")
                .setSubtitle("기기에 등록된 지문을 이용하여 지문을 인증해주세요.")
                .setNegativeButtonText("취소")
                .setAllowedAuthenticators(BIOMETRIC_STRONG)
                .build();

        carListView = findViewById(R.id.carListView);
        //레이아웃메니저: 리사이클러뷰의 항목배치/스크롤 동작을 설정
        carListView.setLayoutManager(new LinearLayoutManager(this));

        memberID = getIntent().getStringExtra("memberID");
        status = getIntent().getStringExtra("status");
        try{
            userData = new JSONObject(getIntent().getStringExtra("userData"));
            groupData = new JSONObject(getIntent().getStringExtra("groupData"));
            groupID = groupData.getString("groupID");
            groupName = groupData.getString("groupName");
        }
        catch(JSONException e){
            Log.e("CarList_OnCreate", e.toString());
        }

        carDataList = new ArrayList<>();
        updateListfromServer();

        adapter = new CarListAdapter(context, carDataList);
        carListView.setAdapter(adapter);

        listEmpty = findViewById(R.id.list_empty);
        if(carDataList.isEmpty())listEmpty.setVisibility(View.VISIBLE);
        else listEmpty.setVisibility(View.INVISIBLE);

        getSupportActionBar().setTitle(groupName);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //프로파일 메뉴
        profileMenu = new ProfileMenu(this);
        profileMenu.settingProfile(userData, groupData);
        if(getIntent().getStringExtra("inviteCode")!=null){
            profileMenu.setInviteCode(getIntent().getStringExtra("inviteCode"));
        }

        adapter.setItemClickListener(new CarListAdapter.itemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                /*DEVICE_CREDENTIAL 및 BIOMETRIC_STRONG | DEVICE_CREDENTIAL 인증자 유형 조합은
        Android 10(API 수준 29) 이하에서 지원되지 않는다*/
                biometricPrompt.authenticate(promptInfo);
                //carInterface로 이동
                if(nowDriving == DEFAULT || nowDriving==position){
                    Log.d("CarList_serItemClickListener_onItemClick: ", "nowDriving"+nowDriving);
                    Intent intent = new Intent(getApplicationContext(), CarInterface.class);
                    CarData carData = carDataList.get(position);
                    intent.putExtra("memberID", memberID);
                    intent.putExtra("carData", carData);
                    intent.putExtra("position", position);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(context, "차량 운전중입니다.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onLookupInfoClick(View v, int position) {
                //lookupInfo로 이동
                Intent intent = new Intent(getApplicationContext(), CarLookupInfo.class);
                intent.putExtra("carName", carDataList.get(position).getCarName());
                startActivity(intent);
            }
        });
        
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateListfromServer();
        adapter.notifyDataSetChanged();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_car_list, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int curId = item.getItemId();
        Intent intent;
        switch(curId){
            case android.R.id.home:
                if(!profileMenu.isMenuOpen()) {
                    profileMenu.openRightMenu();
                }
                else{
                    profileMenu.closeRightMenu();
                }
                break;
            case R.id.addCar:
                Toast.makeText(this, "차량 추가", Toast.LENGTH_LONG).show();
                intent = new Intent(getApplicationContext(), AddCar.class);
                intent.putExtra("memberID", memberID);
                intent.putExtra("groupID", groupID);
                intent.putExtra("status", status);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed(){
        if(profileMenu.isMenuOpen()){
            profileMenu.closeRightMenu();
        }
        else{
            finish();
        }
    }
    private void updateListfromServer(){
        carDataList.clear();
        String params = "mb_id="+memberID+"&group_id="+groupID+"&status="+status;
        Log.d("carList_updateListfromServer", "params :: "+ params);
        String carDataListJSONString = new ServerData("GET", "car/group_show", params, null).get();
        addItem(carDataListJSONString);
    }
    private void addItem(String JSONArrayString){
        String errorMessage = "No car Found";
        if(!JSONArrayString.equals(errorMessage)){
            try{
                JSONArray JSONArray = new JSONArray(JSONArrayString);
                int len = JSONArray.length();
                for(int i=0;i<len;i++){
                    JSONObject jsonObject = JSONArray.getJSONObject(i);
                    CarData carData=new CarData(jsonObject);
                    Log.d("carListList_addItem", jsonObject.getString("status"));

                    Log.d("carList_addItem", "GROUP_item "+i+" :: "+jsonObject.getString("cr_carname"));
                    //cr_id를 통해 차량 상태를 가져오고
                    //가장 최근 상태에서
                    // vs_startup_information ==on :사용 중인 차량
                    String param = "cr_id="+jsonObject.getString("cr_id");
                    String serverData = new ServerData("GET", "vehicle_status/show", param, null).get();
                    if(!serverData.equals("No vehicle_status Found")){
                        String recentVehicleStatus = new JSONArray(serverData).getJSONObject(0).getString("vs_startup_information");
                        if(recentVehicleStatus.equals("on")){
                            carData.setAvailable(false);
                        }
                        else{
                            carData.setAvailable(true);
                        }
                    }
                    carDataList.add(carData);
                }
            }
            catch(JSONException e){
                Log.e("carList_addItem", e.toString());
            }
        }

    }
    private void getPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // Marshmallow부터 지원 가능 체크
            FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

            if (fingerprintManager.isHardwareDetected() == false) { //Manifest에 Fingerprint 퍼미션을 추가해야 사용이 가능함.
                Toast.makeText(this, "지문인식을 사용할수 없는 기기입니다.", Toast.LENGTH_LONG).show();
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "지문 사용을 여부를 허용해 주세요.", Toast.LENGTH_LONG).show();
            } else if (fingerprintManager.hasEnrolledFingerprints() == false) {
                Toast.makeText(this, "등록된 지문정보가 없습니다.", Toast.LENGTH_LONG).show();
            } else {    //  생체 인증 사용가능
                Toast.makeText(this, "지문인식을 해주세요.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
