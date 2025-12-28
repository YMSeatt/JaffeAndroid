package com.example.myapplication.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.myapplication.data.QuizTemplate
import com.example.myapplication.data.QuizTemplateRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class QuizTemplateViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: QuizTemplateViewModel
    private lateinit var repository: QuizTemplateRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `quizTemplates flow should emit values from repository`() = runTest {
        // Given
        val templates = listOf(
            QuizTemplate(1, "Template 1", 10, emptyMap()),
            QuizTemplate(2, "Template 2", 20, emptyMap())
        )
        every { repository.getAll() } returns flowOf(templates)

        // When
        viewModel = QuizTemplateViewModel(repository)

        // Then
        val emittedTemplates = viewModel.quizTemplates.drop(1).first() // Drop the initial empty list
        assertEquals(templates, emittedTemplates)
    }

    @Test
    fun `insert should call repository's insert method`() = runTest {
        // Given
        viewModel = QuizTemplateViewModel(repository)
        val template = QuizTemplate(name = "New Template", numQuestions = 15, defaultMarks = emptyMap())

        // When
        viewModel.insert(template)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { repository.insert(template) }
    }

    @Test
    fun `update should call repository's update method`() = runTest {
        // Given
        viewModel = QuizTemplateViewModel(repository)
        val template = QuizTemplate(id = 1, name = "Updated Template", numQuestions = 12, defaultMarks = emptyMap())

        // When
        viewModel.update(template)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { repository.update(template) }
    }

    @Test
    fun `delete should call repository's delete method`() = runTest {
        // Given
        viewModel = QuizTemplateViewModel(repository)
        val template = QuizTemplate(id = 1, name = "Template to Delete", numQuestions = 10, defaultMarks = emptyMap())

        // When
        viewModel.delete(template)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { repository.delete(template) }
    }
}
