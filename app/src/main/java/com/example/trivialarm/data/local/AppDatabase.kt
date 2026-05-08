package com.example.trivialarm.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.trivialarm.data.local.dao.AlarmDao
import com.example.trivialarm.data.local.dao.CategoryDao
import com.example.trivialarm.data.local.entity.AlarmEntity
import com.example.trivialarm.data.local.entity.AppConverters
import com.example.trivialarm.data.local.entity.CategoryEntity

@Database(
    entities = [AlarmEntity::class, CategoryEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(AppConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add columns to alarms table
                db.execSQL("ALTER TABLE alarms ADD COLUMN categoryId INTEGER")
                db.execSQL("ALTER TABLE alarms ADD COLUMN difficultyPreset TEXT NOT NULL DEFAULT 'MEDIUM'")
                
                // Create categories table
                db.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`id`))")
            }
        }
    }
}
