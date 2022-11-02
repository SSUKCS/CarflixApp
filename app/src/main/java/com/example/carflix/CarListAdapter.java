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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CarListAdapter extends RecyclerView.Adapter<CarListAdapter.ViewHolder>{

    private String TAG = "carListAdapter";
    private Context context;
    private ArrayList<CarData> dataList;//데이터를 담을 리스트
    private boolean isDeleteMode;

    public CarListAdapter(Context context, ArrayList<CarData> dataList){
        this.context = context;
        this.dataList = dataList;
        isDeleteMode = false;
    }
    //클릭 리스너 인터페이스
    public interface itemClickListener{
        void onItemClick(View v, int position);
        void onDeleteCarButtonClick(View v, int position);
        void onLookupInfoClick(View v, int position);
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
        View view = inflater.inflate(R.layout.car_list_item, parent, false);
        return new ViewHolder(view);
    }
    //리스트의 각 항목에 들어갈 데이터를 지정
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position){
        CarData carData = dataList.get(position);
        holder.carImg.setImageResource(carData.getcarImg());
        holder.carName.setText(carData.getCarName());
        holder.isAvailable.setText(carData.getCarStatus());
        if(isDeleteMode){
            holder.deleteCarButton.setVisibility(View.VISIBLE);
        }
        else{
            holder.deleteCarButton.setVisibility(View.INVISIBLE);
        }
        switch(carData.getCarStatus())
        {
            case"운전 가능":holder.isAvailable.setTextColor(Color.parseColor("#4488FF"));break;
            case"다른 사람이 운전중":holder.isAvailable.setTextColor(Color.parseColor("#FF5544"));break;
            case"직접 운전중":holder.isAvailable.setTextColor(Color.parseColor("#9911BB"));break;
        }
    }
    public void setDeleteMode(boolean mode){
        isDeleteMode = mode;
    };
    //화면에 보여줄 데이터의 갯수를 반환
    @Override
    public int getItemCount(){
        return dataList.size();
    }
    //ViewHolder 객체에 저장되어 화면에 표시되고, 필요에 따라 생성 또는 재활용 된다.
    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView carImg;
        TextView carName;
        TextView isAvailable;
        View deleteCarButton;
        Button btn_LookupInfo;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            carImg = itemView.findViewById(R.id.carImg);
            carName = itemView.findViewById(R.id.carName);
            isAvailable = itemView.findViewById(R.id.isAvailable);
            deleteCarButton = itemView.findViewById(R.id.deleteCarButton);
            btn_LookupInfo = itemView.findViewById(R.id.lookupInfo);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("itemview", "itemview"+getAdapterPosition());
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (listener != null) {
                            listener.onItemClick(view, position);
                        }
                    }
                }
            });
            deleteCarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (listener != null) {
                            listener.onDeleteCarButtonClick(view, position);
                        }
                    }
                }
            });

            btn_LookupInfo.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view){
                   int position = getAdapterPosition();
                   if(position != RecyclerView.NO_POSITION){
                       if(listener!=null){
                           listener.onLookupInfoClick(view, position);
                       }
                   }
               }
            });
        }
    }
}
