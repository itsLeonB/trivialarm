package com.example.trivialarm.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trivialarm.data.remote.api.TriviaApi
import com.example.trivialarm.data.remote.model.TriviaQuestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TriviaUiState {
    data object Loading : TriviaUiState
    data class Success(
        val questions: List<TriviaQuestion>,
        val currentQuestionIndex: Int,
        val correctCount: Int,
        val shuffledOptions: List<String>
    ) : TriviaUiState
    data class Error(val message: String) : TriviaUiState
    data object Finished : TriviaUiState
}

@HiltViewModel
class TriviaViewModel @Inject constructor(
    private val triviaApi: TriviaApi
) : ViewModel() {

    var uiState: TriviaUiState by mutableStateOf(TriviaUiState.Loading)
        private set

    init {
        fetchQuestions()
    }

    fun fetchQuestions() {
        viewModelScope.launch {
            uiState = TriviaUiState.Loading
            try {
                val response = triviaApi.getQuestions(amount = 5) // Fetch a few extra just in case
                if (response.results.isNotEmpty()) {
                    val firstQuestion = response.results[0]
                    uiState = TriviaUiState.Success(
                        questions = response.results,
                        currentQuestionIndex = 0,
                        correctCount = 0,
                        shuffledOptions = (firstQuestion.incorrectAnswers + firstQuestion.correctAnswer).shuffled()
                    )
                } else {
                    uiState = TriviaUiState.Error("No questions found")
                }
            } catch (e: Exception) {
                uiState = TriviaUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun submitAnswer(answer: String) {
        val currentState = uiState as? TriviaUiState.Success ?: return
        val currentQuestion = currentState.questions[currentState.currentQuestionIndex]

        if (answer == currentQuestion.correctAnswer) {
            val nextCorrectCount = currentState.correctCount + 1
            if (nextCorrectCount >= 3) {
                uiState = TriviaUiState.Finished
            } else {
                val nextIndex = (currentState.currentQuestionIndex + 1) % currentState.questions.size
                val nextQuestion = currentState.questions[nextIndex]
                uiState = currentState.copy(
                    currentQuestionIndex = nextIndex,
                    correctCount = nextCorrectCount,
                    shuffledOptions = (nextQuestion.incorrectAnswers + nextQuestion.correctAnswer).shuffled()
                )
            }
        } else {
            // Wrong answer - just shuffle again or stay
            val nextIndex = (currentState.currentQuestionIndex + 1) % currentState.questions.size
            val nextQuestion = currentState.questions[nextIndex]
            uiState = currentState.copy(
                currentQuestionIndex = nextIndex,
                shuffledOptions = (nextQuestion.incorrectAnswers + nextQuestion.correctAnswer).shuffled()
            )
        }
    }
}
