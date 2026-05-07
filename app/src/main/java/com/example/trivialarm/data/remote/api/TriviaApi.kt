package com.example.trivialarm.data.remote.api

import com.example.trivialarm.data.remote.model.CategoryResponse
import com.example.trivialarm.data.remote.model.TriviaResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TriviaApi {
    @GET("api.php")
    suspend fun getQuestions(
        @Query("amount") amount: Int? = 3,
        @Query("category") category: Int? = null,
        @Query("difficulty") difficulty: String? = null
    ): TriviaResponse

    @GET("api_category.php")
    suspend fun getCategories(): CategoryResponse
}
