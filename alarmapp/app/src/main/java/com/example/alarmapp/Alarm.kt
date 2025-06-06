package com.example.alarmapp

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val isEnabled: Boolean = true,
    val repeatDays: String = "", // "1,2,3,4,5" for Mon-Fri
    val timeInMillis: Long = 0L
) {
    fun getTimeString(): String {
        return String.format("%02d:%02d", hour, minute)
    }

    fun getRepeatDaysString(): String {
        if (repeatDays.isEmpty()) return "Một lần"

        val days = repeatDays.split(",").map { it.toInt() }
        val dayNames = listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
        return days.map { dayNames[it] }.joinToString(", ")
    }
}