package com.example.trivialarm.data.repository

import com.example.trivialarm.data.local.entity.AlarmDifficultyPreset
import com.example.trivialarm.data.remote.api.TriviaApi
import com.example.trivialarm.data.remote.model.TriviaQuestion
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TriviaRepository @Inject constructor(
    private val triviaApi: TriviaApi
) {
    suspend fun getQuestionsWithFallback(
        amount: Int,
        categoryId: Int?,
        difficulty: AlarmDifficultyPreset
    ): List<TriviaQuestion> {
        return try {
            // Attempt with specific category
            val response = triviaApi.getQuestions(
                amount = amount,
                category = categoryId,
                difficulty = difficulty.apiDifficulty
            )
            if (response.results.isNotEmpty()) {
                response.results
            } else {
                // Fallback: Fetch without category
                fetchFallbackQuestions(amount, difficulty)
            }
        } catch (e: Exception) {
            // Fallback: Fetch without category on error
            fetchFallbackQuestions(amount, difficulty)
        }
    }

    private suspend fun fetchFallbackQuestions(
        amount: Int,
        difficulty: AlarmDifficultyPreset
    ): List<TriviaQuestion> {
        return try {
            val response = triviaApi.getQuestions(
                amount = amount,
                category = null,
                difficulty = difficulty.apiDifficulty
            )
            response.results
        } catch (e: Exception) {
            emptyList()
        }
    }
}
