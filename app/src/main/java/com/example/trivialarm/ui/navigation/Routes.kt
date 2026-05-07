package com.example.trivialarm.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object AlarmList : Route
    
    @Serializable
    data class AddEditAlarm(val alarmId: Int? = null) : Route

    @Serializable
    data class AlarmActive(val alarmId: Int) : Route
}
