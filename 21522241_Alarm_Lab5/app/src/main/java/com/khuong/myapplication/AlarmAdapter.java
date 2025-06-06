package com.khuong.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private ArrayList<AlarmItem> alarmList;

    public AlarmAdapter(ArrayList<AlarmItem> alarmList) {
        this.alarmList = alarmList;
    }

    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AlarmViewHolder holder, int position) {
        AlarmItem alarmItem = alarmList.get(position);
        holder.timeTextView.setText(alarmItem.getTime());
        holder.labelTextView.setText(alarmItem.getLabel());
        holder.daysTextView.setText(alarmItem.getDays());
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;
        TextView labelTextView;
        TextView daysTextView;

        public AlarmViewHolder(View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            labelTextView = itemView.findViewById(R.id.labelTextView);
            daysTextView = itemView.findViewById(R.id.daysTextView);
        }
    }
}
