package com.example.alarmapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("alarm_id", -1)
        val isSnooze = intent.getBooleanExtra("is_snooze", false)
        val label = intent.getStringExtra("alarm_label") ?: "Báo thức"

        if (alarmId != -1) {
            // Khởi động AlarmService để xử lý báo thức
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra("alarm_id", alarmId)
                putExtra("alarm_label", label)
                putExtra("is_snooze", isSnooze)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}