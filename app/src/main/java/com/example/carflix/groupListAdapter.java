package com.example.carflix;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class groupListAdapter extends RecyclerView.Adapter<groupListAdapter.ViewHolder>{

    private String TAG = "groupListAdapter";
    private Context context;
    private ArrayList dataList;//데이터를 담을 리스트

    public groupListAdapter(Context context, ArrayList dataList){
        this.context = context;
        this.dataList = dataList;
    }
    //클릭 리스너 인터페이스
    public interface itemClickListener{
        void onItemClick(View v, int position);

    }
    //리스너 객체 참조 변수
    private itemClickListener listener = null;
    //리스너 객체 참조를 어댑터에 전달
    public void setItemClickListener(itemClickListener listener){
        this.listener = listener;
    }
    //리스트의 각 항목을 이루는 디자인을 적용
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.group_list_item, parent, false);
        return new ViewHolder(view);
    }
    //리스트의 각 항목에 들어갈 데이터를 지정
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position){

        Log.d("groupListAdpater_onBindViewHolder", "POSITION :: "+ position);
        String dataType = dataList.get(position).getClass().toString();
        dataType = dataType.substring(dataType.lastIndexOf(".")+1);
        Log.d("groupListAdpater_onBindViewHolder", "DATATYPE :: "+ dataType);
        switch(dataType){
            case "groupData":
                groupData groupData = (groupData)dataList.get(position);
                holder.groupName.setText(groupData.getGroupName());
                holder.groupDescription.setText(groupData.getGroupDescription());
                holder.groupType.setText(groupData.getStatus());
                holder.groupType.setBackgroundColor(ContextCompat.getColor(context, R.color.sg_color));

                break;
            case "ceoGroupData":
                ceoGroupData ceoGroupData = (ceoGroupData) dataList.get(position);
                holder.groupName.setText(ceoGroupData.getGroupName());
                holder.groupDescription.setText(ceoGroupData.getGroupDescription());
                holder.groupType.setText(ceoGroupData.getStatus());
                holder.groupType.setBackgroundColor(ContextCompat.getColor(context, R.color.cg_color));
                break;

            case "rentGroupData":
                rentGroupData rentGroupData = (rentGroupData) dataList.get(position);
                holder.groupName.setText(rentGroupData.getGroupName());
                holder.groupDescription.setText(rentGroupData.getGroupDescription());
                holder.groupType.setText(rentGroupData.getStatus());
                holder.groupType.setBackgroundColor(ContextCompat.getColor(context, R.color.rg_color));break;
        }
    }
    //화면에 보여줄 데이터의 갯수를 반환
    @Override
    public int getItemCount(){
        return dataList.size();
    }
    //ViewHolder 객체에 저장되어 화면에 표시되고, 필요에 따라 생성 또는 재활용 된다.
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView groupName;
        TextView groupDescription;
        Button groupType;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            groupName = itemView.findViewById(R.id.groupName);
            groupDescription = itemView.findViewById(R.id.groupDescription);
            groupType = itemView.findViewById(R.id.groupType);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (listener != null) {
                            listener.onItemClick(view, position);
                        }
                    }
                }
            });
        }
    }
}
