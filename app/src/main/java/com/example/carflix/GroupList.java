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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//자신의 그룹 화면을 표시
public class GroupList extends AppCompatActivity {

    private Context context;

    private String memberID;
    JSONObject userData;

    private ArrayList groupDataList;
    private GroupListAdapter adapter;
    private RecyclerView groupListView;
    private TextView listEmpty;
    private ProfileMenu profileMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_list);
        context = getApplicationContext();
        groupListView = findViewById(R.id.groupListView);

        //레이아웃메니저: 리사이클러뷰의 항목배치/스크롤 동작을 설정
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        groupListView.setLayoutManager(layoutManager);

        memberID = getIntent().getStringExtra("mb_id");
        ServerData serverData = new ServerData("GET", "member/show", "mb_id="+memberID, null);
        try{
            userData = new JSONObject(serverData.get());

            //프로파일 메뉴
            profileMenu = new ProfileMenu(this);
            profileMenu.settingProfile(userData);
        }
        catch(JSONException e){
            Log.e("GroupList", e.toString());
        }
        getSupportActionBar().setTitle("그룹 선택");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupDataList = new ArrayList<>();
        adapter = new GroupListAdapter(context, groupDataList);
        groupListView.setAdapter(adapter);

        listEmpty = findViewById(R.id.list_empty);
        Log.d("carList", "isempty :: "+groupDataList.isEmpty());

        adapter.setItemClickListener(new GroupListAdapter.itemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent(getApplicationContext(), CarList.class);
                String groupID, groupName, status;

                groupID = ((SmallGroupData) groupDataList.get(position)).getGroupID();
                groupName = ((SmallGroupData) groupDataList.get(position)).getGroupName();
                status = ((SmallGroupData) groupDataList.get(position)).getStatus();

                intent.putExtra("memberID", memberID);
                intent.putExtra("userData", userData.toString());
                intent.putExtra("groupID", groupID);
                intent.putExtra("groupName", groupName);
                intent.putExtra("status", status);

                startActivity(intent);
            }
        });
    }
    @Override
    protected void onResume() {
        Log.i("GroupList", "onResume: ");
        super.onResume();
        //서버로부터 데이터 입력
        updateListfromServer();
        if(groupDataList.isEmpty())listEmpty.setVisibility(View.VISIBLE);
        else listEmpty.setVisibility(View.INVISIBLE);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_group_list, menu);
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
            case R.id.generateGroup:
                Toast.makeText(this, "그룹 생성", Toast.LENGTH_LONG).show();
                intent = new Intent(context, GenerateGroup.class);
                intent.putExtra("memberID", memberID);
                startActivity(intent);
                break;
            case R.id.joinGroup:
                Toast.makeText(this, "그룹 가입", Toast.LENGTH_LONG).show();
                intent = new Intent(context, JoinGroup.class);
                intent.putExtra("memberID", memberID);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateListfromServer(){
        groupDataList.clear();
        new Thread(() -> {
            Log.d("Annonymus thread", "run");
            String small_groupDataJSONString = new ServerData("GET", "small_group/group_info", "mb_id="+memberID, null).get();
            String ceo_groupDataJSONString = new ServerData("GET", "ceo_group/group_info", "mb_id="+memberID, null).get();
            String rent_groupDataJSONString = new ServerData("GET", "rent_group/group_info", "mb_id="+memberID, null).get();
            addItem(small_groupDataJSONString, "sg");
            addItem(ceo_groupDataJSONString, "cg");
            addItem(rent_groupDataJSONString, "rg");
        }).run();
        Log.d("carList", "isempty :: "+groupDataList.isEmpty());
        adapter.notifyDataSetChanged();
    }
    private void addItem(String JSONArrayString, String groupType){
        String errorMessage;
        switch(groupType){
            case "sg":errorMessage = "No small_group Found";break;
            case "cg":errorMessage = "No ceo_group Found";break;
            case "rg":errorMessage = "No rent_group Found";break;
            default: errorMessage = "INVALID GROUPTYPE";
            Log.e("groupList_addItem", "ERROR :: INVALID GROUPTYPE");
        }
        if(!JSONArrayString.equals(errorMessage)&&!errorMessage.equals("INVALID GROUPTYPE")){
            try{
                JSONArray JSONArray = new JSONArray(JSONArrayString);
                int len = JSONArray.length();
                for(int i=0;i<len;i++){
                    JSONObject jsonObject = JSONArray.getJSONObject(i);
                    Log.d("groupList_addItem", jsonObject.getString("status"));
                    switch(jsonObject.getString("status")){
                        case"small_group":
                            groupDataList.add(new SmallGroupData(JSONArray.getJSONObject(i)));
                            Log.d("groupList.addItem", "GROUP_item "+i+" :: "+jsonObject.getString("sg_title"));
                            break;
                        case"ceo_group":
                            groupDataList.add(new CEOGroupData(JSONArray.getJSONObject(i)));
                            Log.d("groupList.addItem", "GROUP_item "+i+" :: "+jsonObject.getString("cg_title"));
                            break;
                        case"rent_group":
                            groupDataList.add(new RentGroupData(JSONArray.getJSONObject(i)));
                            Log.d("groupList.addItem", "GROUP_item "+i+" :: "+jsonObject.getString("rg_title"));
                            break;
                    }
                }
            }
            catch(JSONException e){
                Log.e("groupList.addItem", e.toString());
            }
        }

    }
    @Override protected void onDestroy(){
        Log.i("GroupList", "onDestroy: ");
        super.onDestroy();
    }
    @Override public void onBackPressed() {
        Log.i("GroupList", "onBackPressed: ");
        if(profileMenu.isMenuOpen()){
            profileMenu.closeRightMenu();
        }
        else{
            moveTaskToBack(true);
        }
    }
}
