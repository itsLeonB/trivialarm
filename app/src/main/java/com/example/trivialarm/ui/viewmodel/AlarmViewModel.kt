package com.example.trivialarm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trivialarm.data.local.entity.AlarmEntity
import com.example.trivialarm.data.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val repository: AlarmRepository
) : ViewModel() {

    val alarms: StateFlow<List<AlarmEntity>> = repository.getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            repository.updateAlarm(alarm.copy(isEnabled = !alarm.isEnabled))
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
        }
    }

    suspend fun getAlarmById(id: Int): AlarmEntity? {
        // Since we don't have a direct DAO access here, let's use the flow
        return alarms.value.find { it.id == id }
    }

    fun saveAlarm(hour: Int, minute: Int, daysOfWeek: List<Int>, alarmId: Int? = null) {
        viewModelScope.launch {
            val alarm = AlarmEntity(
                id = alarmId ?: 0,
                hour = hour,
                minute = minute,
                isEnabled = true,
                daysOfWeek = daysOfWeek
            )
            if (alarmId == null) {
                repository.insertAlarm(alarm)
            } else {
                repository.updateAlarm(alarm)
            }
        }
    }
}
