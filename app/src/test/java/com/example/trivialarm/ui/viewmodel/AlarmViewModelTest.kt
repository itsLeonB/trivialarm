package com.example.trivialarm.ui.viewmodel

import com.example.trivialarm.data.local.entity.AlarmDifficultyPreset
import com.example.trivialarm.data.local.entity.AlarmEntity
import com.example.trivialarm.data.repository.AlarmRepository
import com.example.trivialarm.data.repository.CategoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlarmViewModelTest {

    private val alarmRepository: AlarmRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { alarmRepository.getAllAlarms() } returns emptyFlow()
        every { categoryRepository.categories } returns emptyFlow()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_syncsCategories() = runTest {
        // Arrange
        coEvery { categoryRepository.syncCategories() } returns Unit

        // Act
        AlarmViewModel(alarmRepository, categoryRepository)

        // Assert
        coVerify { categoryRepository.syncCategories() }
    }

    @Test
    fun init_syncCategoriesFails_doesNotCrash() = runTest {
        // Arrange
        coEvery { categoryRepository.syncCategories() } throws RuntimeException("Network Error")

        // Act
        AlarmViewModel(alarmRepository, categoryRepository)

        // Assert
        coVerify { categoryRepository.syncCategories() }
        // If we reach here, it didn't crash
    }

    @Test
    fun toggleAlarm_updatesRepository() = runTest {
        // Arrange
        coEvery { categoryRepository.syncCategories() } returns Unit
        val alarm = AlarmEntity(id = 1, hour = 8, minute = 0, isEnabled = true, daysOfWeek = emptyList())
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        val viewModel = AlarmViewModel(alarmRepository, categoryRepository)

        // Act
        viewModel.toggleAlarm(alarm)

        // Assert
        coVerify { alarmRepository.updateAlarm(match { !it.isEnabled && it.id == 1 }) }
    }

    @Test
    fun deleteAlarm_updatesRepository() = runTest {
        // Arrange
        coEvery { categoryRepository.syncCategories() } returns Unit
        val alarm = AlarmEntity(id = 1, hour = 8, minute = 0, isEnabled = true, daysOfWeek = emptyList())
        coEvery { alarmRepository.deleteAlarm(any()) } returns Unit
        val viewModel = AlarmViewModel(alarmRepository, categoryRepository)

        // Act
        viewModel.deleteAlarm(alarm)

        // Assert
        coVerify { alarmRepository.deleteAlarm(alarm) }
    }

    @Test
    fun saveAlarm_newAlarm_insertsToRepository() = runTest {
        // Arrange
        coEvery { categoryRepository.syncCategories() } returns Unit
        coEvery { alarmRepository.insertAlarm(any()) } returns Unit
        val viewModel = AlarmViewModel(alarmRepository, categoryRepository)

        // Act
        viewModel.saveAlarm(
            hour = 7,
            minute = 30,
            daysOfWeek = listOf(1, 2),
            categoryId = null,
            difficultyPreset = AlarmDifficultyPreset.EASY
        )

        // Assert
        coVerify { alarmRepository.insertAlarm(match { it.hour == 7 && it.minute == 30 && it.difficultyPreset == AlarmDifficultyPreset.EASY }) }
    }

    @Test
    fun saveAlarm_existingAlarm_updatesRepository() = runTest {
        // Arrange
        coEvery { categoryRepository.syncCategories() } returns Unit
        coEvery { alarmRepository.updateAlarm(any()) } returns Unit
        val viewModel = AlarmViewModel(alarmRepository, categoryRepository)

        // Act
        viewModel.saveAlarm(
            hour = 9,
            minute = 15,
            daysOfWeek = listOf(6, 7),
            categoryId = 10,
            difficultyPreset = AlarmDifficultyPreset.HARD,
            alarmId = 5
        )

        // Assert
        coVerify { alarmRepository.updateAlarm(match { it.id == 5 && it.hour == 9 && it.difficultyPreset == AlarmDifficultyPreset.HARD }) }
    }
}
