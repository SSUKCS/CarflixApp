package com.example.carflix;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
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
    private TextView snackBarTextView;

    private String memberID;
    private String creatorID;
    private String groupID;
    private String groupName;
    private JSONObject userData;
    private JSONObject groupData;
    private String status;

    private int nowDriving = -1;

    LoadingDialog dialog;

    private CarIdService carIdService;
    private CarIdService.CarIdServiceCallback CarIdServiceCallback = this::CarIdStateUpdateCallback;
    private final ServiceConnection carIdServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i("CarInterface_carControlServiceBindConnection", "onServiceConnected: connected");
            CarIdService.CarServiceBinder carServiceBinder = (CarIdService.CarServiceBinder) iBinder;
            carIdService = carServiceBinder.getService();
            carIdService.registerCallback(CarIdServiceCallback);
            CarIdStateUpdateCallback(carIdService.getState());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i("CarInterface_CarInterface_carControlServiceBindConnection", "onServiceDisconnected: disconnected");
            carIdService = null;
        }
    };
    private void CarIdStateUpdateCallback(String state){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch(state){
                    case ArduinoBluetooth.SEARCHING:
                        dialog.show();
                        dialog.setText("기기 탐색중....");
                        dialog.setTextColor(Color.parseColor("#5DC19B"));
                        break;
                    case ArduinoBluetooth.FOUND_DEVICE:
                        dialog.show();
                        dialog.setText("기기 연결중...");
                        dialog.setTextColor(Color.parseColor("#5DC19B"));
                        break;
                    case ArduinoBluetooth.SUCCESSFUL_CONNECTION:
                        dialog.show();
                        dialog.setText("연결 성공.");
                        dialog.setTextColor(Color.parseColor("#9911BB"));
                        break;
                    case ArduinoBluetooth.FAILED_CONNECTION:
                        dialog.setText("차량과 연결이 실패하였습니다.");
                        dialog.setTextColor(Color.parseColor("#F23920"));
                        dialog.dismiss();
                        break;
                    case CarIdService.ASSIGN_OK:
                        dialog.setText("성공적으로 제거되었습니다.");
                        unbindService(carIdServiceConnection);
                        dialog.dismiss();
                        break;
                }
            }
        });
    }
    Integer carImg_default = R.drawable.carimage_default;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_list);
        context = getApplicationContext();

        dialog = new LoadingDialog(this);

        //생체인식 관련
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
            creatorID = groupData.getString("creatorID");
            Log.d("CarList", "memberID :: "+memberID+" , creatorID :: "+creatorID);
            groupID = groupData.getString("groupID");
            groupName = groupData.getString("groupName");
        }
        catch(JSONException e){
            Log.e("CarList_OnCreate", e.toString());
        }

        carDataList = new ArrayList<>();
        updateListFromServer();

        adapter = new CarListAdapter(context, carDataList);
        carListView.setAdapter(adapter);

        listEmpty = findViewById(R.id.list_empty);
        if(carDataList.isEmpty())listEmpty.setVisibility(View.VISIBLE);
        else listEmpty.setVisibility(View.INVISIBLE);

        //액션바
        getSupportActionBar().setTitle(groupName);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //프로파일 메뉴
        profileMenu = new ProfileMenu(this);
        profileMenu.settingProfile(userData, groupData);
        if(getIntent().getStringExtra("inviteCode")!=null){
            String inviteCode = getIntent().getStringExtra("inviteCode");
            profileMenu.setInviteCode(inviteCode);
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
            public void onDeleteCarButtonClick(View v, int position){
                showDeleteMessage(position);
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
        updateListFromServer();
        adapter.notifyDataSetChanged();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_car_list, menu);
        MenuItem addCarButton = menu.findItem(R.id.addCar);
        addCarButton.setVisible(memberID.equals(creatorID));
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
    public void setMode(boolean mode){
        adapter.setDeleteMode(mode);
        updateListFromServer();
        adapter.notifyDataSetChanged();
    }
    public void showDeleteMessage(int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("차량을 삭제하시겠습니까?");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String numberClassification = carDataList.get(position).getClassification();
                String registerationNum = carDataList.get(position).getRegisterationNumber();
                String carName = carDataList.get(position).getCarName();

                Intent bindServiceIntent = new Intent(getApplicationContext(), CarIdService.class);
                bindServiceIntent.putExtra("mb_id", memberID);
                bindServiceIntent.putExtra("group_id", groupID);
                bindServiceIntent.putExtra("status", status);
                bindServiceIntent.putExtra("numberClassification", numberClassification);
                bindServiceIntent.putExtra("registerationNum", registerationNum);
                bindServiceIntent.putExtra("carName", carName);
                bindServiceIntent.putExtra("mode", CarIdService.DELETE_MODE);
                bindService(bindServiceIntent, carIdServiceConnection, BIND_AUTO_CREATE);

                updateListFromServer();
                adapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void updateListFromServer(){
        carDataList.clear();
        String params = "mb_id="+creatorID+"&group_id="+groupID+"&status="+status;;
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
