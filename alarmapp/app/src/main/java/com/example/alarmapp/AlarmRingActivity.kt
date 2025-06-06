package com.example.alarmapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.alarmapp.databinding.ActivityAlarmRingBinding

class AlarmRingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmRingBinding
    private lateinit var alarmScheduler: AlarmScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmRingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alarmScheduler = AlarmScheduler(this)

        val alarmId = intent.getIntExtra("alarm_id", -1)
        if (alarmId != -1) {
            binding.textViewAlarmTime.text = getAlarmTime(alarmId)
            binding.textViewAlarmLabel.text = getAlarmLabel(alarmId) ?: "Báo thức"
        }

        binding.buttonSnooze.setOnClickListener {
            snoozeAlarm(alarmId)
            stopService(Intent(this, AlarmService::class.java))
            finish()
        }

        binding.buttonDismiss.setOnClickListener {
            dismissAlarm(alarmId)
            stopService(Intent(this, AlarmService::class.java))
            finish()
        }

        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun getAlarmTime(alarmId: Int): String {
        return "12:51" // Giả lập
    }

    private fun getAlarmLabel(alarmId: Int): String? {
        return intent.getStringExtra("alarm_label") ?: "Báo thức"
    }

    private fun snoozeAlarm(alarmId: Int) {
        if (alarmId != -1) {
            alarmScheduler.scheduleSnoozeAlarm(alarmId)
            Toast.makeText(this, "Báo lại sau 10 phút", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy báo thức", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dismissAlarm(alarmId: Int) {
        if (alarmId != -1) {
            alarmScheduler.cancelAlarm(alarmId)
            Toast.makeText(this, "Đã tắt báo thức", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy báo thức", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}