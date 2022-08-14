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
public class groupList extends AppCompatActivity {

    private Context context;

    private ArrayList<groupData> groupDataList;
    private groupListAdapter adapter;
    private RecyclerView groupListView;
    private TextView listEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_list);
        context = getApplicationContext();
        groupListView = findViewById(R.id.groupListView);

        //레이아웃메니저: 리사이클러뷰의 항목배치/스크롤 동작을 설정
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        groupListView.setLayoutManager(layoutManager);

        groupDataList = new ArrayList<>();
        adapter = new groupListAdapter(context, groupDataList);
        groupListView.setAdapter(adapter);

        listEmpty = findViewById(R.id.list_empty);
        //회사 데이터 입력단
        String small_groupDataJSONString = getIntent().getStringExtra("small_groupDataJSONString");
        addItem(getIntent().getStringExtra("small_groupDataJSONString"), "sg");
        addItem(getIntent().getStringExtra("ceo_groupDataJSONString"), "cg");
        addItem(getIntent().getStringExtra("rent_groupDataJSONString"), "rg");
        if(groupDataList.isEmpty())listEmpty.setVisibility(View.VISIBLE);
        else listEmpty.setVisibility(View.INVISIBLE);
        /*
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                data -> {
                    Log.d("carList", "data : " + data);
                    if (data.getResultCode() == RESULT_OK)
                    {

                        Intent intent = data.getData();

                        boolean isAvailableChanged = intent.getBooleanExtra("carData_isAvailableChanged", true);
                        int position = Integer.parseInt(intent.getStringExtra("position"));
                        if(isAvailableChanged)nowDriving = DEFAULT;
                        else nowDriving = position;
                        String carStatus = intent.getStringExtra("carStatusChanged");

                        Log.d("carList", "isAvailableChanged : " + isAvailableChanged);
                        Log.d("carList", "position : " + position);
                        Log.d("carList", "carStatusChanged : " + carStatus);

                        carDataList.get(position).setAvailable(isAvailableChanged);
                        carDataList.get(position).setStatus(carStatus);
                        adapter.notifyItemChanged(position);
                    }
                });*/
        adapter.setItemClickListener(new groupListAdapter.itemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent(getApplicationContext(), carList.class);
                ArrayList<carData> carDataList = groupDataList.get(position).getCarDataList();
                intent.putExtra("carDataList",carDataList);
                startActivity(intent);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_group_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int curId = item.getItemId();

        switch(curId){
            case R.id.generateGroup:
                Toast.makeText(this, "그룹 생성", Toast.LENGTH_LONG).show();
                break;
            case R.id.joinGroup:
                Toast.makeText(this, "그룹 가입", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void addItem(String JSONArrayString, String groupType){
        String errorMessage;
        switch(groupType){
            case "sg":errorMessage = "No small_group Found";
            break;
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
                            groupDataList.add(new groupData(JSONArray.getJSONObject(i)));
                            Log.d("groupList.addItem", "GROUP_item "+i+" :: "+jsonObject.getString("sg_title"));
                            break;
                        case"ceo_group":
                            groupDataList.add(new ceoGroupData(JSONArray.getJSONObject(i)));
                            Log.d("groupList.addItem", "GROUP_item "+i+" :: "+jsonObject.getString("cg_title"));
                            break;
                        case"rent_group":
                            groupDataList.add(new rentGroupData(JSONArray.getJSONObject(i)));
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
}
