package com.khuong.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AlarmListActivity extends AppCompatActivity {
    RecyclerView recyclerAlarms;
    Button btnAddAlarm;
    AlarmAdapter alarmAdapter;
    SharedPreferences sharedPreferences;
    Gson gson = new Gson();
    final String ALARM_KEY = "alarm_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_list);

        recyclerAlarms = findViewById(R.id.recyclerAlarms);
        recyclerAlarms.setLayoutManager(new LinearLayoutManager(this));

        sharedPreferences = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);

        btnAddAlarm = findViewById(R.id.btnAddAlarm);

        // Load saved alarms
        List<AlarmItem> alarmList = getSavedAlarms();

        // Set up adapter
        alarmAdapter = new AlarmAdapter(new ArrayList<>(alarmList));
        recyclerAlarms.setAdapter(alarmAdapter);

        // Add button click listener
        btnAddAlarm.setOnClickListener(view -> {
            startActivity(new Intent(AlarmListActivity.this, MainActivity.class));
        });
    }

    private List<AlarmItem> getSavedAlarms() {
        String json = sharedPreferences.getString(ALARM_KEY, null);
        if (json != null) {
            Type type = new TypeToken<List<AlarmItem>>() {}.getType();
            return gson.fromJson(json, type);
        } else {
            return new ArrayList<>();
        }
    }
}