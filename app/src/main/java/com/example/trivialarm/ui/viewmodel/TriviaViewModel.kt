package com.example.trivialarm.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trivialarm.data.local.entity.AlarmDifficultyPreset
import com.example.trivialarm.data.remote.model.TriviaQuestion
import com.example.trivialarm.data.repository.AlarmRepository
import com.example.trivialarm.data.repository.TriviaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AnswerFeedbackState {
    IDLE,
    CORRECT,
    WRONG
}

sealed interface TriviaUiState {
    data object Loading : TriviaUiState
    data class Success(
        val questions: List<TriviaQuestion>,
        val currentQuestionIndex: Int,
        val correctCount: Int,
        val goalCount: Int,
        val shuffledOptions: List<String>,
        val selectedAnswer: String? = null,
        val feedbackState: AnswerFeedbackState = AnswerFeedbackState.IDLE
    ) : TriviaUiState
    data class Error(val message: String) : TriviaUiState
    data object Finished : TriviaUiState
}

@HiltViewModel
class TriviaViewModel @Inject constructor(
    private val triviaRepository: TriviaRepository,
    private val alarmRepository: AlarmRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    var uiState: TriviaUiState by mutableStateOf(TriviaUiState.Loading)
        private set

    private var currentAlarmId: Int? = null
    private var fetchJob: Job? = null

    // We'll initialize this from the screen if Nav3 doesn't auto-fill SavedStateHandle
    fun initialize(alarmId: Int) {
        if (uiState !is TriviaUiState.Loading && currentAlarmId == alarmId) return
        currentAlarmId = alarmId
        fetchQuestions(alarmId)
    }

    fun retry() {
        currentAlarmId?.let { fetchQuestions(it) }
    }

    private fun fetchQuestions(alarmId: Int) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            uiState = TriviaUiState.Loading
            try {
                val alarm = alarmRepository.getAlarmById(alarmId)
                val difficulty = alarm?.difficultyPreset ?: AlarmDifficultyPreset.MEDIUM
                val categoryId = alarm?.categoryId
                val goalCount = difficulty.questionCount

                val questions = triviaRepository.getQuestionsWithFallback(
                    amount = goalCount + 2, // Fetch a few extra
                    categoryId = categoryId,
                    difficulty = difficulty
                )

                if (!isActive) return@launch

                if (questions.isNotEmpty()) {
                    val firstQuestion = questions[0]
                    uiState = TriviaUiState.Success(
                        questions = questions,
                        currentQuestionIndex = 0,
                        correctCount = 0,
                        goalCount = goalCount,
                        shuffledOptions = (firstQuestion.incorrectAnswers + firstQuestion.correctAnswer).shuffled()
                    )
                } else {
                    uiState = TriviaUiState.Error("No questions found")
                }
            } catch (e: Exception) {
                if (isActive) {
                    uiState = TriviaUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun submitAnswer(answer: String) {
        val currentState = uiState as? TriviaUiState.Success ?: return
        if (currentState.feedbackState != AnswerFeedbackState.IDLE) return

        val currentQuestion = currentState.questions[currentState.currentQuestionIndex]
        val isCorrect = answer == currentQuestion.correctAnswer

        viewModelScope.launch {
            uiState = currentState.copy(
                selectedAnswer = answer,
                feedbackState = if (isCorrect) AnswerFeedbackState.CORRECT else AnswerFeedbackState.WRONG
            )

            delay(1200) // Show feedback for a bit

            if (isCorrect) {
                val nextCorrectCount = currentState.correctCount + 1
                if (nextCorrectCount >= currentState.goalCount) {
                    uiState = TriviaUiState.Finished
                } else {
                    val nextIndex = (currentState.currentQuestionIndex + 1) % currentState.questions.size
                    val nextQuestion = currentState.questions[nextIndex]
                    uiState = currentState.copy(
                        currentQuestionIndex = nextIndex,
                        correctCount = nextCorrectCount,
                        shuffledOptions = (nextQuestion.incorrectAnswers + nextQuestion.correctAnswer).shuffled(),
                        selectedAnswer = null,
                        feedbackState = AnswerFeedbackState.IDLE
                    )
                }
            } else {
                // Wrong answer - stay on same question and allow retry
                uiState = currentState.copy(
                    selectedAnswer = null,
                    feedbackState = AnswerFeedbackState.IDLE
                )
            }
        }
    }
}
