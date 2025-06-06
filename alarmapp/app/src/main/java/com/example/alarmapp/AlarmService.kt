package com.example.alarmapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.os.Handler
import android.os.Looper

class AlarmService : Service() {

    private var ringtone: Ringtone? = null
    private val stopRingtoneHandler = Handler(Looper.getMainLooper())
    private val stopRingtoneRunnable = Runnable {
        stopRingtone()
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getIntExtra("alarm_id", -1) ?: -1
        val isSnooze = intent?.getBooleanExtra("is_snooze", false) ?: false
        val label = intent?.getStringExtra("alarm_label") ?: "Báo thức"

        if (alarmId != -1) {
            // Tạo thông báo foreground
            startForeground(alarmId, createNotification(alarmId, label))

            // Phát âm thanh báo thức
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(this, uri)
            ringtone?.play()

            // Dừng âm thanh sau 30 giây nếu không có hành động
            stopRingtoneHandler.postDelayed(stopRingtoneRunnable, 30000)

            // Khởi động AlarmRingActivity
            val activityIntent = Intent(this, AlarmRingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("alarm_id", alarmId)
                putExtra("alarm_label", label)
                putExtra("is_snooze", isSnooze)
            }
            startActivity(activityIntent)

            // Hiển thị thông báo bổ sung nếu không phải snooze
            if (!isSnooze) {
                showNotification(alarmId, label)
            }
        }

        return START_NOT_STICKY
    }

    private fun createNotification(alarmId: Int, label: String): android.app.Notification {
        val channelId = "alarm_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarm Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for alarm service notifications"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val dismissIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = "DISMISS_ALARM"
            putExtra("alarm_id", alarmId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this,
            alarmId,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Báo thức")
            .setContentText("Báo thức $label đang kêu")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, "Tắt", dismissPendingIntent)
            .build()
    }

    private fun showNotification(alarmId: Int, label: String) {
        val notificationManager = NotificationManagerCompat.from(this)
        val channelId = "alarm_channel"
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Báo thức")
            .setContentText("Báo thức $label đã kích hoạt")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(alarmId, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun stopRingtone() {
        ringtone?.stop()
        ringtone = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingtoneHandler.removeCallbacks(stopRingtoneRunnable)
        stopRingtone()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}