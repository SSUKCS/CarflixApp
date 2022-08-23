package com.example.carflix;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CarList extends AppCompatActivity {
    private final static int DEFAULT = -1;

    private Context context;

    private ProfileMenu profileMenu;

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
}
