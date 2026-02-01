package com.example.myapplication.viewmodel

import com.example.myapplication.data.QuizMarkType
import com.example.myapplication.data.QuizMarkTypeRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizMarkTypeViewModelTest {

    private val repository = mockk<QuizMarkTypeRepository>(relaxed = true)
    private lateinit var viewModel: QuizMarkTypeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getAll() } returns flowOf(emptyList())
        viewModel = QuizMarkTypeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `insert should call repository insert`() = runTest {
        val markType = QuizMarkType(name = "Test", defaultPoints = 1.0, contributesToTotal = true, isExtraCredit = false)
        viewModel.insert(markType)
        advanceUntilIdle()
        coVerify { repository.insert(markType) }
    }

    @Test
    fun `resetToDefaults should call repository deleteAll and then insertAll with defaults`() = runTest {
        viewModel.resetToDefaults()
        advanceUntilIdle()
        coVerify { repository.deleteAll() }
        coVerify { repository.insertAll(any()) }
    }
}
