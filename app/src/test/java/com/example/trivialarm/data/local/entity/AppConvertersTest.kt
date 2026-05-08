package com.example.trivialarm.data.local.entity

import org.junit.Assert.assertEquals
import org.junit.Test

class AppConvertersTest {
    private val converters = AppConverters()

    @Test
    fun toDifficultyPreset_withValidValue_returnsCorrectEnum() {
        assertEquals(AlarmDifficultyPreset.EASY, converters.toDifficultyPreset("EASY"))
        assertEquals(AlarmDifficultyPreset.MEDIUM, converters.toDifficultyPreset("MEDIUM"))
        assertEquals(AlarmDifficultyPreset.HARD, converters.toDifficultyPreset("HARD"))
    }

    @Test
    fun toDifficultyPreset_withInvalidValue_returnsMedium() {
        assertEquals(AlarmDifficultyPreset.MEDIUM, converters.toDifficultyPreset("INVALID"))
    }
}
