package com.example.trivialarm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trivialarm.data.local.entity.AlarmEntity
import com.example.trivialarm.data.local.entity.CategoryEntity
import com.example.trivialarm.data.repository.AlarmRepository
import com.example.trivialarm.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val repository: AlarmRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val alarms: StateFlow<List<AlarmEntity>> = repository.getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            categoryRepository.syncCategories()
        }
    }

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

    fun saveAlarm(
        hour: Int,
        minute: Int,
        daysOfWeek: List<Int>,
        categoryId: Int?,
        difficultyPreset: com.example.trivialarm.data.local.entity.AlarmDifficultyPreset,
        alarmId: Int? = null
    ) {
        viewModelScope.launch {
            val alarm = AlarmEntity(
                id = alarmId ?: 0,
                hour = hour,
                minute = minute,
                isEnabled = true,
                daysOfWeek = daysOfWeek,
                categoryId = categoryId,
                difficultyPreset = difficultyPreset
            )
            if (alarmId == null) {
                repository.insertAlarm(alarm)
            } else {
                repository.updateAlarm(alarm)
            }
        }
    }
}
