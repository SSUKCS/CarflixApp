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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
public class CarList extends AppCompatActivity {
    private final static int DEFAULT = -1;

    private Context context;

    private ArrayList<CarData> carDataList;
    private CarListAdapter adapter;
    private RecyclerView carListView;
    private TextView listEmpty;


    private String memberID;
    private String groupID;
    private String groupName;
    private String status;

    private int nowDriving = -1;

    int carImg_default = R.drawable.carimage_default;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_list);
        context = getApplicationContext();
        carListView = findViewById(R.id.carListView);
        //레이아웃메니저: 리사이클러뷰의 항목배치/스크롤 동작을 설정
        carListView.setLayoutManager(new LinearLayoutManager(this));

        memberID = getIntent().getStringExtra("memberID");
        groupID = getIntent().getStringExtra("groupID");
        groupName = getIntent().getStringExtra("groupName");
        status = getIntent().getStringExtra("status");

        carDataList = new ArrayList<CarData>();
        updateListfromServer();

        adapter = new CarListAdapter(context, carDataList);
        carListView.setAdapter(adapter);

        listEmpty = findViewById(R.id.list_empty);
        if(carDataList.isEmpty())listEmpty.setVisibility(View.VISIBLE);
        else listEmpty.setVisibility(View.INVISIBLE);

        getSupportActionBar().setTitle(groupName);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                data -> {
                    Log.d("carList", "data : " + data);
                    switch (data.getResultCode())
                    {
                        //carInterface->carList
                        case 9001:
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
                        adapter.notifyItemChanged(position);break;

                        //carLookupInfo->carList
                        case 9002: break;

                        //addCar -> break;
                        case 9003: break;
                        default:break;
                    }
                });
        adapter.setItemClickListener(new CarListAdapter.itemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                //carInterface로 이동
                if(nowDriving == DEFAULT || nowDriving==position){
                    Intent intent = new Intent(getApplicationContext(), CarInterface.class);
                    CarData carData = carDataList.get(position);
                    intent.putExtra("carData", carData);
                    intent.putExtra("position", position);
                    launcher.launch(intent);
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
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_car_list, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int curId = item.getItemId();
        Intent intent;
        switch(curId){
            case R.id.addCar:
                Toast.makeText(this, "차량 추가", Toast.LENGTH_LONG).show();
                intent = new Intent(getApplicationContext(), AddCar.class);
                intent.putExtra("memberID", memberID);
                intent.putExtra("groupID", groupID);
                intent.putExtra("status", status);
                startActivity(intent);
                break;
            case R.id.generateCode:
                Toast.makeText(this, "코드 생성", Toast.LENGTH_LONG).show();
                intent = new Intent(getApplicationContext(), GenerateCode.class);
                intent.putExtra("memberID", memberID);
                intent.putExtra("groupID", groupID);
                intent.putExtra("groupName", groupName);
                intent.putExtra("status", status);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed(){
        /*
        if(carData_isAvailable_initialState != carData.isAvailable())
        {
            Intent intent = new Intent();
            intent.putExtra("position", Integer.toString(position));
            intent.putExtra("carData_isAvailableChanged", carData.isAvailable());
            intent.putExtra("carStatusChanged", isAvailable.getText());
            setResult(RESULT_OK, intent);
        }*/
        finish();
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
                    Log.d("carListList_addItem", jsonObject.getString("status"));
                    carDataList.add(new CarData(JSONArray.getJSONObject(i)));
                    Log.d("carList.addItem", "GROUP_item "+i+" :: "+jsonObject.getString("cr_carname"));
                }
            }
            catch(JSONException e){
                Log.e("groupList.addItem", e.toString());
            }
        }

    }
}
