package com.example.trivialarm.data.repository

import com.example.trivialarm.data.local.dao.CategoryDao
import com.example.trivialarm.data.local.entity.CategoryEntity
import com.example.trivialarm.data.remote.api.TriviaApi
import com.example.trivialarm.data.remote.model.CategoryDto
import com.example.trivialarm.data.remote.model.CategoryResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class CategoryRepositoryTest {

    private val triviaApi: TriviaApi = mockk()
    private val categoryDao: CategoryDao = mockk()
    private lateinit var repository: CategoryRepository

    @Before
    fun setup() {
        every { categoryDao.getAllCategories() } returns emptyFlow()
        repository = CategoryRepository(triviaApi, categoryDao)
    }

    @Test
    fun syncCategories_success_mapsAndCleansNames() = runTest {
        // Arrange
        val apiResponse = CategoryResponse(
            listOf(
                CategoryDto(1, "Science: Computers"),
                CategoryDto(2, "Entertainment: Music"),
                CategoryDto(3, "General Knowledge")
            )
        )
        coEvery { triviaApi.getCategories() } returns apiResponse
        
        val capturedCategories = slot<List<CategoryEntity>>()
        coEvery { categoryDao.insertCategories(capture(capturedCategories)) } returns Unit

        // Act
        repository.syncCategories()

        // Assert
        coVerify { triviaApi.getCategories() }
        coVerify { categoryDao.insertCategories(any()) }
        
        val inserted = capturedCategories.captured
        assertEquals(3, inserted.size)
        assertEquals("Computers", inserted[0].name)
        assertEquals("Music", inserted[1].name)
        assertEquals("General", inserted[2].name)
    }

    @Test
    fun syncCategories_onCancellation_rethrows() = runTest {
        // Arrange
        coEvery { triviaApi.getCategories() } throws CancellationException("Test cancellation")

        // Act & Assert
        try {
            repository.syncCategories()
            fail("Should have rethrown CancellationException")
        } catch (e: CancellationException) {
            assertEquals("Test cancellation", e.message)
        }
        
        coVerify(exactly = 0) { categoryDao.insertCategories(any()) }
    }

    @Test
    fun syncCategories_onException_swallows() = runTest {
        // Arrange
        coEvery { triviaApi.getCategories() } throws RuntimeException("API Error")

        // Act
        // Should not throw because we handle other exceptions
        repository.syncCategories()
        
        // Assert
        coVerify(exactly = 0) { categoryDao.insertCategories(any()) }
    }
}
