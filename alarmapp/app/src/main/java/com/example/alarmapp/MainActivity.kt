package com.example.alarmapp

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alarmapp.Alarm
import com.example.alarmapp.databinding.ActivityMainBinding
import com.example.alarmapp.AlarmViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.button.MaterialButton
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var alarmViewModel: AlarmViewModel
    private lateinit var alarmAdapter: AlarmAdapter
    private lateinit var alarmScheduler: AlarmScheduler

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            Toast.makeText(this, "Cần cấp quyền để ứng dụng hoạt động", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tùy chỉnh status bar và navigation bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
            window.navigationBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        }

        setupPermissions()
        setupViewModel()
        setupRecyclerView()
        setupFab()
    }

    private fun setupPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.SCHEDULE_EXACT_ALARM)
            }
        }

        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun setupViewModel() {
        alarmViewModel = ViewModelProvider(this)[AlarmViewModel::class.java]
        alarmScheduler = AlarmScheduler(this)
    }

    private fun setupRecyclerView() {
        alarmAdapter = AlarmAdapter(
            onToggleAlarm = { alarm ->
                val updatedAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
                alarmViewModel.update(updatedAlarm)

                if (updatedAlarm.isEnabled) {
                    alarmScheduler.scheduleRepeatingAlarm(updatedAlarm)
                } else {
                    alarmScheduler.cancelAlarm(updatedAlarm.id)
                }
            },
            onDeleteAlarm = { alarm ->
                showDeleteConfirmation(alarm)
            }
        )

        binding.recyclerViewAlarms.apply {
            adapter = alarmAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(this@MainActivity, LinearLayoutManager.VERTICAL))
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator().apply {
                addDuration = 300
                removeDuration = 300
            }
        }

        alarmViewModel.allAlarms.observe(this) { alarms ->
            alarmAdapter.submitList(alarms) {
                binding.recyclerViewAlarms.startAnimation(
                    AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
                )
            }

            if (alarms.isEmpty()) {
                binding.textViewNoAlarms.visibility = View.VISIBLE
                binding.recyclerViewAlarms.visibility = View.GONE
            } else {
                binding.textViewNoAlarms.visibility = View.GONE
                binding.recyclerViewAlarms.visibility = View.VISIBLE
            }
        }
    }

    private fun setupFab() {
        binding.fabAddAlarm.setOnClickListener {
            showAddAlarmDialog()
        }
    }

    private fun showAddAlarmDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            showLabelAndRepeatDialog(selectedHour, selectedMinute)
        }, hour, minute, true).show()
    }

    private fun showLabelAndRepeatDialog(hour: Int, minute: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_alarm_label, null)
        val editTextLabel = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextLabel)
        val repeatButtons = listOf<MaterialButton>(
            dialogView.findViewById(R.id.buttonSun),
            dialogView.findViewById(R.id.buttonMon),
            dialogView.findViewById(R.id.buttonTue),
            dialogView.findViewById(R.id.buttonWed),
            dialogView.findViewById(R.id.buttonThu),
            dialogView.findViewById(R.id.buttonFri),
            dialogView.findViewById(R.id.buttonSat)
        )

        val selectedDays = BooleanArray(7) { false }

        // Load colors
        val primaryColor = ContextCompat.getColorStateList(this, R.color.primary)
        val textPrimaryColor = ContextCompat.getColorStateList(this, R.color.text_primary)
        val transparentColor = ContextCompat.getColorStateList(this, android.R.color.transparent) // For outlined button background

        repeatButtons.forEachIndexed { index, button ->
            // Initialize button appearance based on initial selectedDays state
            if (selectedDays[index]) {
                button.backgroundTintList = primaryColor
                button.setTextColor(ContextCompat.getColorStateList(this, R.color.on_primary))
            } else {
                button.backgroundTintList = transparentColor
                button.setTextColor(textPrimaryColor)
            }

            button.setOnClickListener {
                selectedDays[index] = !selectedDays[index]
                if (selectedDays[index]) {
                    button.backgroundTintList = primaryColor
                    button.setTextColor(ContextCompat.getColorStateList(this, R.color.on_primary))
                } else {
                    button.backgroundTintList = transparentColor
                    button.setTextColor(textPrimaryColor)
                }
            }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Thêm báo thức")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val label = editTextLabel.text.toString().ifEmpty { "Báo thức" }
                val repeatDays = selectedDays.mapIndexed { index, selected ->
                    if (selected) index.toString() else null
                }.filterNotNull().joinToString(",")
                createAlarm(hour, minute, label, repeatDays)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun createAlarm(hour: Int, minute: Int, label: String, repeatDays: String) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val alarm = Alarm(
            hour = hour,
            minute = minute,
            label = label,
            isEnabled = true,
            repeatDays = repeatDays,
            timeInMillis = calendar.timeInMillis
        )

        alarmViewModel.insert(alarm) { alarmId ->
            val alarmWithId = alarm.copy(id = alarmId.toInt())
            alarmScheduler.scheduleRepeatingAlarm(alarmWithId)

            runOnUiThread {
                Toast.makeText(
                    this,
                    "Đã đặt báo thức lúc ${alarm.getTimeString()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showDeleteConfirmation(alarm: Alarm) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Xóa báo thức")
            .setMessage("Bạn có chắc chắn muốn xóa báo thức này?")
            .setPositiveButton("Xóa") { _, _ ->
                alarmViewModel.delete(alarm)
                alarmScheduler.cancelAlarm(alarm.id)
                Toast.makeText(this, "Đã xóa báo thức", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}