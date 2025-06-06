package com.khuong.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TimePicker timePicker;
    Button btnSetAlarm, btnShowAlarms;
    EditText editLabel;
    CheckBox chkMon, chkTue, chkWed, chkThu, chkFri, chkSat, chkSun;

    SharedPreferences sharedPreferences;
    Gson gson = new Gson();
    final String ALARM_KEY = "alarm_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timePicker = findViewById(R.id.timePicker);
        btnSetAlarm = findViewById(R.id.btnSetAlarm);
        btnShowAlarms = findViewById(R.id.btnShowAlarms);
        editLabel = findViewById(R.id.editLabel);

        chkMon = findViewById(R.id.chkMon);
        chkTue = findViewById(R.id.chkTue);
        chkWed = findViewById(R.id.chkWed);
        chkThu = findViewById(R.id.chkThu);
        chkFri = findViewById(R.id.chkFri);
        chkSat = findViewById(R.id.chkSat);
        chkSun = findViewById(R.id.chkSun);

        timePicker.setIs24HourView(true);
        sharedPreferences = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);

        btnSetAlarm.setOnClickListener(view -> {
            int hour = (Build.VERSION.SDK_INT >= 23) ? timePicker.getHour() : timePicker.getCurrentHour();
            int minute = (Build.VERSION.SDK_INT >= 23) ? timePicker.getMinute() : timePicker.getCurrentMinute();

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            String label = editLabel.getText().toString().trim();
            ArrayList<String> days = new ArrayList<>();
            if (chkMon.isChecked()) days.add("Mon");
            if (chkTue.isChecked()) days.add("Tue");
            if (chkWed.isChecked()) days.add("Wed");
            if (chkThu.isChecked()) days.add("Thu");
            if (chkFri.isChecked()) days.add("Fri");
            if (chkSat.isChecked()) days.add("Sat");
            if (chkSun.isChecked()) days.add("Sun");

            // Save to SharedPreferences as JSON
            List<AlarmItem> alarmList = getSavedAlarms();
            alarmList.add(new AlarmItem(hour + ":" + minute, label, String.join(",", days)));

            String json = gson.toJson(alarmList);
            sharedPreferences.edit().putString(ALARM_KEY, json).apply();

            // Set system alarm
            Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
            intent.putExtra("label", label);
            intent.putExtra("days", String.join(",", days));

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    (int) System.currentTimeMillis(), // unique ID for each alarm
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Intent permissionIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    permissionIntent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(permissionIntent);
                    Toast.makeText(this, "Please allow exact alarms in settings", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Toast.makeText(MainActivity.this, "Alarm Set!", Toast.LENGTH_SHORT).show();
        });

        btnShowAlarms.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, AlarmListActivity.class));
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