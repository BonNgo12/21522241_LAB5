package com.example.alarmapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmapp.databinding.ItemAlarmBinding
import com.google.android.material.materialswitch.MaterialSwitch

class AlarmAdapter(
    private val onToggleAlarm: (Alarm) -> Unit,
    private val onDeleteAlarm: (Alarm) -> Unit
) : ListAdapter<Alarm, AlarmAdapter.AlarmViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Alarm>() {
            override fun areItemsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
                return oldItem == newItem
            }
        }
    }

    class AlarmViewHolder(private val binding: ItemAlarmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(alarm: Alarm, onToggleAlarm: (Alarm) -> Unit, onDeleteAlarm: (Alarm) -> Unit) {
            binding.textViewTime.text = alarm.getTimeString()
            binding.textViewLabel.text = alarm.label
            binding.textViewRepeatDays.text = if (alarm.repeatDays.isEmpty()) "Một lần" else alarm.repeatDays
            binding.switchAlarm.isChecked = alarm.isEnabled
            binding.buttonDelete.setOnClickListener { onDeleteAlarm(alarm) }

            binding.switchAlarm.setOnCheckedChangeListener { _, isChecked ->
                val updatedAlarm = alarm.copy(isEnabled = isChecked)
                onToggleAlarm(updatedAlarm)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = getItem(position)
        holder.bind(alarm, onToggleAlarm, onDeleteAlarm)
    }
}