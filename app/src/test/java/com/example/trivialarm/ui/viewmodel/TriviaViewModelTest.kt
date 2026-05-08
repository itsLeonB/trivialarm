package com.example.trivialarm.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.trivialarm.data.local.entity.AlarmDifficultyPreset
import com.example.trivialarm.data.local.entity.AlarmEntity
import com.example.trivialarm.data.remote.model.TriviaQuestion
import com.example.trivialarm.data.repository.AlarmRepository
import com.example.trivialarm.data.repository.TriviaRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TriviaViewModelTest {

    private val triviaRepository: TriviaRepository = mockk()
    private val alarmRepository: AlarmRepository = mockk()
    private val savedStateHandle: SavedStateHandle = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: TriviaViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TriviaViewModel(triviaRepository, alarmRepository, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createQuestion(
        question: String = "Q",
        correctAnswer: String = "A",
        incorrectAnswers: List<String> = listOf("B")
    ) = TriviaQuestion(
        type = "multiple",
        difficulty = "easy",
        question = question,
        correctAnswer = correctAnswer,
        incorrectAnswers = incorrectAnswers
    )

    @Test
    fun fetchQuestions_success_updatesUiStateToSuccess() = runTest {
        // Arrange
        val alarmId = 1
        val alarm = AlarmEntity(id = alarmId, hour = 8, minute = 0, difficultyPreset = AlarmDifficultyPreset.EASY, daysOfWeek = emptyList())
        val questions = listOf(createQuestion(question = "Q1", correctAnswer = "A1"))
        coEvery { alarmRepository.getAlarmById(alarmId) } returns alarm
        coEvery { triviaRepository.getQuestionsWithFallback(any(), any(), any()) } returns questions

        // Act
        viewModel.initialize(alarmId)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState
        assertTrue(state is TriviaUiState.Success)
        val successState = state as TriviaUiState.Success
        assertEquals(questions, successState.questions)
        assertEquals(AlarmDifficultyPreset.EASY.questionCount, successState.goalCount)
    }

    @Test
    fun fetchQuestions_empty_updatesUiStateToError() = runTest {
        // Arrange
        val alarmId = 1
        coEvery { alarmRepository.getAlarmById(alarmId) } returns null
        coEvery { triviaRepository.getQuestionsWithFallback(any(), any(), any()) } returns emptyList()

        // Act
        viewModel.initialize(alarmId)
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.uiState is TriviaUiState.Error)
        assertEquals("No questions found", (viewModel.uiState as TriviaUiState.Error).message)
    }

    @Test
    fun fetchQuestions_error_updatesUiStateToError() = runTest {
        // Arrange
        val alarmId = 1
        coEvery { alarmRepository.getAlarmById(alarmId) } throws RuntimeException("Database error")

        // Act
        viewModel.initialize(alarmId)
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.uiState is TriviaUiState.Error)
        assertEquals("Database error", (viewModel.uiState as TriviaUiState.Error).message)
    }

    @Test
    fun concurrentFetch_onlyLatestIsApplied() = runTest {
        // Arrange
        val alarmId1 = 1
        val alarmId2 = 2
        
        val questions1 = listOf(createQuestion(question = "Q1"))
        val questions2 = listOf(createQuestion(question = "Q2"))

        coEvery { alarmRepository.getAlarmById(alarmId1) } coAnswers {
            delay(1000)
            mockk<AlarmEntity>().apply { 
                every { difficultyPreset } returns AlarmDifficultyPreset.EASY
                every { categoryId } returns null
            }
        }
        coEvery { triviaRepository.getQuestionsWithFallback(any(), any(), any()) } returns questions1

        // Start first fetch
        viewModel.initialize(alarmId1)
        
        // Immediately start second fetch (should cancel first)
        coEvery { alarmRepository.getAlarmById(alarmId2) } returns mockk<AlarmEntity>().apply { 
            every { difficultyPreset } returns AlarmDifficultyPreset.MEDIUM
            every { categoryId } returns null
        }
        coEvery { triviaRepository.getQuestionsWithFallback(any(), any(), any()) } returns questions2
        
        viewModel.initialize(alarmId2)
        
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.uiState is TriviaUiState.Success)
        val successState = viewModel.uiState as TriviaUiState.Success
        assertEquals(questions2, successState.questions)
    }

    @Test
    fun submitAnswer_correct_advancesQuestion() = runTest {
        // Arrange
        val q1 = createQuestion(question = "Q1", correctAnswer = "Correct")
        val q2 = createQuestion(question = "Q2", correctAnswer = "Correct")
        val questions = listOf(q1, q2)
        
        coEvery { alarmRepository.getAlarmById(any()) } returns mockk<AlarmEntity>().apply { 
            every { difficultyPreset } returns AlarmDifficultyPreset.MEDIUM
            every { categoryId } returns null
        }
        coEvery { triviaRepository.getQuestionsWithFallback(any(), any(), any()) } returns questions
        
        viewModel.initialize(1)
        advanceUntilIdle()

        // Act
        viewModel.submitAnswer("Correct")

        // Assert
        val state = viewModel.uiState as TriviaUiState.Success
        assertEquals(1, state.currentQuestionIndex)
        assertEquals(1, state.correctCount)
    }

    @Test
    fun submitAnswer_goalReached_finishes() = runTest {
        // Arrange
        val q = createQuestion(correctAnswer = "Correct")
        val questions = listOf(q, q, q) // Enough questions for HARD (2)
        
        val hardAlarm = AlarmEntity(id = 1, hour = 8, minute = 0, difficultyPreset = AlarmDifficultyPreset.HARD, daysOfWeek = emptyList())
        coEvery { alarmRepository.getAlarmById(1) } returns hardAlarm
        coEvery { triviaRepository.getQuestionsWithFallback(any(), any(), any()) } returns questions

        viewModel.initialize(1)
        advanceUntilIdle()

        // Act
        viewModel.submitAnswer("Correct") // count 1
        viewModel.submitAnswer("Correct") // count 2 -> Finished for HARD (goalCount = 2)
        
        // Assert
        assertEquals(TriviaUiState.Finished, viewModel.uiState)
    }
}
