package com.example.carflix;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class InviteCodeListAdapter extends RecyclerView.Adapter<InviteCodeListAdapter.ViewHolder>{

    private Context context;
    private ArrayList<InviteCode> dataList;//데이터를 담을 리스트
    private String groupID;
    private String groupName;
    private String status;

    public InviteCodeListAdapter(Context context, ArrayList<InviteCode> dataList){
        this.context = context;
        this.dataList = dataList;
    }
    public interface itemClickListener{
        void onItemClick(View v, int position);

    }
    //리스너 객체 참조 변수
    private InviteCodeListAdapter.itemClickListener listener = null;
    //리스너 객체 참조를 어댑터에 전달
    public void setItemClickListener(InviteCodeListAdapter.itemClickListener listener){
        this.listener = listener;
    }

    //리스트의 각 항목을 이루는 디자인을 적용
    @NonNull
    @Override
    public InviteCodeListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.invite_code, parent, false);
        return new InviteCodeListAdapter.ViewHolder(view);
    }
    //리스트의 각 항목에 들어갈 데이터를 지정
    @Override
    public void onBindViewHolder(@NonNull final InviteCodeListAdapter.ViewHolder holder, int position){
        InviteCode inviteCode = dataList.get(position);

        holder.groupTitle.setText(inviteCode.getGroupName());
        String status = inviteCode.getStatus();
        switch(status){
            case"small_group": holder.groupStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.sg_color));break;
            case"ceo_group":holder.groupStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.cg_color));break;
            case"rent_group":holder.groupStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.rg_color));break;
        }
        holder.groupStatus.setText(status);
        holder.inviteCodeText.setText(inviteCode.getCode());
    }
    //화면에 보여줄 데이터의 갯수를 반환
    @Override
    public int getItemCount(){
        return dataList.size();
    }
    //ViewHolder 객체에 저장되어 화면에 표시되고, 필요에 따라 생성 또는 재활용 된다.
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView groupTitle;
        TextView inviteCodeText;
        Button groupStatus;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            groupTitle = itemView.findViewById(R.id.groupTitle);
            inviteCodeText = itemView.findViewById(R.id.inviteCode);
        }
    }
}
