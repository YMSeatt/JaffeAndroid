package com.example.myapplication.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class QuizMarkTypeRepositoryTest {

    private val dao = mockk<QuizMarkTypeDao>()
    private val repository = QuizMarkTypeRepository(dao)

    @Test
    fun `getAll should return flow from dao`() = runBlocking {
        val markTypes = listOf(QuizMarkType(id = 1, name = "Test", defaultPoints = 1.0, contributesToTotal = true, isExtraCredit = false))
        coEvery { dao.getAllQuizMarkTypes() } returns flowOf(markTypes)

        val result = repository.getAll()

        result.collect {
            assertEquals(markTypes, it)
        }
    }

    @Test
    fun `insert should call dao insert`() = runBlocking {
        val markType = QuizMarkType(name = "Test", defaultPoints = 1.0, contributesToTotal = true, isExtraCredit = false)
        coEvery { dao.insert(markType) } returns Unit

        repository.insert(markType)

        coVerify { dao.insert(markType) }
    }
}
