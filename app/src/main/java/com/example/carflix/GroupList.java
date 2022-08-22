package com.example.carflix;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
            String smallGroupDataJSONString = new ServerData("GET", "small_group/group_info", "mb_id="+memberID, null).get();
            String ceoGroupDataJSONString = new ServerData("GET", "ceo_group/group_info", "mb_id="+memberID, null).get();
            String rentGroupDataJSONString = new ServerData("GET", "rent_group/group_info", "mb_id="+memberID, null).get();
            addItem(smallGroupDataJSONString, "small_group");
            addItemFromSavedData("small_group");
            addItem(ceoGroupDataJSONString, "ceo_group");
            addItemFromSavedData("ceo_group");
            addItem(rentGroupDataJSONString, "rent_group");
            addItemFromSavedData("rent_group");
        }).run();
        Log.d("carList", "isempty :: "+groupDataList.isEmpty());
        adapter.notifyDataSetChanged();
    }
    private void addItemFromSavedData(String groupType){
        //savedInviteGroupData에 저장되어있는 코드를 이용해 아이템 추가
        /*
         * savedInviteGroupData  -   small_group:[{"ic_number": "...."}, ....],
         *                           ceo_group:[{"ic_number": "...."}, ....],
         *                           rent_group:[{"ic_number": "...."}, ....]
         */
        SharedPreferences savedInviteGroupData = getSharedPreferences("savedInviteGroupData", MODE_PRIVATE);
        String savedGroupJSONArrayString = savedInviteGroupData.getString(groupType, "there's no data");
        Log.d("GroupList_addItemFromSaveData", "savedGroupJSONArrayString :: "+savedGroupJSONArrayString);
        if(!savedGroupJSONArrayString.equals("there's no data")){
            try{
                String inviteCode;

                JSONArray inviteCodeJSONArray = new JSONArray(savedGroupJSONArrayString);
                int len = inviteCodeJSONArray.length();
                for(int i = 0; i< len; i++){
                    inviteCode = inviteCodeJSONArray.getJSONObject(i).getString("ic_number");
                    ServerData serverData = new ServerData("POST", "code_car/create", new JSONObject().put("ic_number", inviteCode).put("member", memberID));
                    JSONObject resultJSON = new JSONObject(serverData.get());
                    String message = resultJSON.getString("message");
                    if(message.equals("group invited")){
                        // 성공: {"message":"group invited","mb_id":"29","group_id":"34","status":"ceo_group"}
                        String group_id= resultJSON.getString("group_id");
                        String param="";
                        switch(groupType){
                            case"small_group": param = "sg_id="+group_id;break;
                            case"ceo_group":param = "cg_id="+group_id;break;
                            case"rent_group":param = "rg_id="+group_id;break;
                        }
                        serverData = new ServerData("GET", groupType+"/show", param, null);
                        //성공 : Connected successfully
                        JSONObject groupJSONData = new JSONObject(serverData.get());
                        if(!groupJSONData.get("mb_id").equals("")){
                            //그룹 데이터 사용 가능
                            switch(groupType){
                                case"small_group": groupDataList.add(new SmallGroupData(groupJSONData));;break;
                                case"ceo_group":groupDataList.add(new CEOGroupData(groupJSONData));;break;
                                case"rent_group":groupDataList.add(new RentGroupData(groupJSONData));;break;
                            }
                        }
                        else{
                            //모종의 이유(예: 그룹이 삭제)로 그룹데이터가 사용 불가능
                            Toast.makeText(getApplicationContext(), "존재하지 않는 그룹입니다.", Toast.LENGTH_LONG);
                            Log.d("JoinGroup", "그룹이 존재하지 않습니다.");
                        }
                    }
                    else{// 실패: {"message":"group not invited"}
                        Log.d("GroupList_addItemFromSavedData", message);
                    }
                }
            }
            catch(JSONException e){
                Log.e("groupList_addItemFromSavedData", e.toString());
            }
        }
    }
    private void addItem(String JSONArrayString, String groupType){
        String errorMessage;
        switch(groupType){
            case "sg":
            case "small_group":errorMessage = "No small_group Found";break;
            case "cg":
            case "ceo_group":errorMessage = "No ceo_group Found";break;
            case "rg":
            case "rent_group":errorMessage = "No rent_group Found";break;
            default: errorMessage = "INVALID GROUPTYPE";
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
                Log.e("groupList_addItem", e.toString());
            }
        }
        else Log.e("groupList_addItem", errorMessage);
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
