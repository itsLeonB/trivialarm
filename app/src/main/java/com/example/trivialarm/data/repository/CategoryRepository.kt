package com.example.trivialarm.data.repository

import com.example.trivialarm.data.local.dao.CategoryDao
import com.example.trivialarm.data.local.entity.CategoryEntity
import com.example.trivialarm.data.remote.api.TriviaApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val triviaApi: TriviaApi,
    private val categoryDao: CategoryDao
) {
    val categories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun syncCategories() {
        try {
            val response = triviaApi.getCategories()
            val entities = response.triviaCategories.map { dto ->
                CategoryEntity(
                    id = dto.id,
                    name = cleanCategoryName(dto.name)
                )
            }
            categoryDao.insertCategories(entities)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cleanCategoryName(name: String): String {
        return name
            .replace("Science: ", "")
            .replace("Entertainment: ", "")
            .replace("General Knowledge", "General")
    }
}
