package com.example.trivialarm.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.trivialarm.data.local.dao.AlarmDao
import com.example.trivialarm.data.local.entity.AlarmEntity
import com.example.trivialarm.data.local.entity.DaysOfWeekConverter

@Database(entities = [AlarmEntity::class], version = 1, exportSchema = false)
@TypeConverters(DaysOfWeekConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
}
