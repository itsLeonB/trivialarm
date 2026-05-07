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
    val daysOfWeek: List<Int> // 1 for Monday, 7 for Sunday etc.
)

class DaysOfWeekConverter {
    @TypeConverter
    fun fromList(value: List<Int>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toList(value: String): List<Int> {
        return Json.decodeFromString(value)
    }
}
