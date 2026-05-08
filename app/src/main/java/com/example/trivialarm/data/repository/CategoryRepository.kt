package com.example.trivialarm.data.repository

import android.util.Log
import com.example.trivialarm.data.local.dao.CategoryDao
import com.example.trivialarm.data.local.entity.CategoryEntity
import com.example.trivialarm.data.remote.api.TriviaApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val triviaApi: TriviaApi,
    private val categoryDao: CategoryDao
) {
    private val tag = "CategoryRepository"
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(tag, "Error syncing categories", e)
        }
    }

    private fun cleanCategoryName(name: String): String {
        return name
            .replace("Science: ", "")
            .replace("Entertainment: ", "")
            .replace("General Knowledge", "General")
    }
}
