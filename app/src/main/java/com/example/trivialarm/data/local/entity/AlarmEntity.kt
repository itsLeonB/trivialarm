package com.example.trivialarm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val daysOfWeek: List<Int>, // 1 for Monday, 7 for Sunday etc.
    val categoryId: Int? = null,
    val difficultyPreset: AlarmDifficultyPreset = AlarmDifficultyPreset.MEDIUM
)

class AppConverters {
    @TypeConverter
    fun fromDaysOfWeekList(value: List<Int>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toDaysOfWeekList(value: String): List<Int> {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun fromDifficultyPreset(value: AlarmDifficultyPreset): String {
        return value.name
    }

    @TypeConverter
    fun toDifficultyPreset(value: String): AlarmDifficultyPreset {
        return AlarmDifficultyPreset.entries.firstOrNull { it.name == value } ?: AlarmDifficultyPreset.MEDIUM
    }
}
