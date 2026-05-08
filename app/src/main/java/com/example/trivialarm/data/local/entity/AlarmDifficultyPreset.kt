package com.example.trivialarm.data.local.entity

enum class AlarmDifficultyPreset(val apiDifficulty: String, val questionCount: Int) {
    EASY("easy", 5),
    MEDIUM("medium", 3),
    HARD("hard", 2)
}
