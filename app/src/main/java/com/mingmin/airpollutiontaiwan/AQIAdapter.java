package com.mingmin.airpollutiontaiwan;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AQIAdapter extends RecyclerView.Adapter<AQIAdapter.ViewHolder>{

    private List<AQI> aqiList;

    public AQIAdapter(List<AQI> aqiList) {
        this.aqiList = aqiList;
    }

    public void updateAQIList(List<AQI> newList) {
        aqiList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_aqi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AQI aqi = aqiList.get(position);
        holder.tvCounty.setText(aqi.getCounty());
        holder.tvSiteName.setText(aqi.getSiteName());
        holder.tvAQIValue.setText(String.valueOf(aqi.getAQI()));
        holder.tvStatus.setText(aqi.getStatus());
        choiceAQIBarLevel(holder, aqi.getAQILevel());
    }

    @Override
    public int getItemCount() {
        if (aqiList != null) {
            return aqiList.size();
        } else {
            return 0;
        }
    }

    private void choiceAQIBarLevel(ViewHolder holder, int level) {
        holder.ivBarLevel1.setVisibility(View.INVISIBLE);
        holder.ivBarLevel2.setVisibility(View.INVISIBLE);
        holder.ivBarLevel3.setVisibility(View.INVISIBLE);
        holder.ivBarLevel4.setVisibility(View.INVISIBLE);
        holder.ivBarLevel5.setVisibility(View.INVISIBLE);
        holder.ivBarLevel6.setVisibility(View.INVISIBLE);
        switch (level) {
            case 1:
                holder.ivBarLevel1.setVisibility(View.VISIBLE);
                break;
            case 2:
                holder.ivBarLevel2.setVisibility(View.VISIBLE);
                break;
            case 3:
                holder.ivBarLevel3.setVisibility(View.VISIBLE);
                break;
            case 4:
                holder.ivBarLevel4.setVisibility(View.VISIBLE);
                break;
            case 5:
                holder.ivBarLevel5.setVisibility(View.VISIBLE);
                break;
            case 6:
                holder.ivBarLevel6.setVisibility(View.VISIBLE);
                break;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvCounty;
        private TextView tvSiteName;
        private TextView tvAQIValue;
        private TextView tvStatus;
        private ImageView ivBarLevel1;
        private ImageView ivBarLevel2;
        private ImageView ivBarLevel3;
        private ImageView ivBarLevel4;
        private ImageView ivBarLevel5;
        private ImageView ivBarLevel6;

        public ViewHolder(View itemView) {
            super(itemView);
            tvCounty = itemView.findViewById(R.id.item_aqi_county);
            tvSiteName = itemView.findViewById(R.id.item_aqi_siteName);
            tvAQIValue = itemView.findViewById(R.id.item_aqi_value);
            tvStatus = itemView.findViewById(R.id.item_aqi_status);
            ivBarLevel1 = itemView.findViewById(R.id.bar_aqi_level_arrow1);
            ivBarLevel2 = itemView.findViewById(R.id.bar_aqi_level_arrow2);
            ivBarLevel3 = itemView.findViewById(R.id.bar_aqi_level_arrow3);
            ivBarLevel4 = itemView.findViewById(R.id.bar_aqi_level_arrow4);
            ivBarLevel5 = itemView.findViewById(R.id.bar_aqi_level_arrow5);
            ivBarLevel6 = itemView.findViewById(R.id.bar_aqi_level_arrow6);
        }
    }
}
