package com.example.myapplication.viewmodel

import app.cash.turbine.test
import com.example.myapplication.data.QuizTemplate
import com.example.myapplication.data.QuizTemplateRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class QuizTemplateViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: QuizTemplateViewModel
    private val repository: QuizTemplateRepository = mockk(relaxed = true)

    private val existingTemplates = listOf(
        QuizTemplate(id = 1, name = "History", numQuestions = 10, defaultMarks = emptyMap()),
        QuizTemplate(id = 2, name = "Math", numQuestions = 20, defaultMarks = emptyMap())
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getAll() } returns flowOf(existingTemplates)
        viewModel = QuizTemplateViewModel(repository)
        // Allow the StateFlow to initialize
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addTemplate should call repository's insert for new name`() = runTest {
        val newTemplate = QuizTemplate(name = "Science", numQuestions = 15, defaultMarks = emptyMap())
        val result = viewModel.addTemplate(newTemplate)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result)
        coVerify { repository.insert(newTemplate) }
        viewModel.errorFlow.test {
            assertEquals(null, awaitItem())
        }
    }

    @Test
    fun `addTemplate should not call repository and post error for duplicate name`() = runTest {
        val duplicateTemplate = QuizTemplate(name = "History", numQuestions = 5, defaultMarks = emptyMap())
        val result = viewModel.addTemplate(duplicateTemplate)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(result)
        coVerify(exactly = 0) { repository.insert(any()) }
        viewModel.errorFlow.test {
            assertEquals("A quiz template named 'History' already exists.", awaitItem())
        }
    }

    @Test
    fun `addTemplate should not call repository and post error for duplicate name case-insensitive`() = runTest {
        val duplicateTemplate = QuizTemplate(name = "history", numQuestions = 5, defaultMarks = emptyMap())
        val result = viewModel.addTemplate(duplicateTemplate)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(result)
        coVerify(exactly = 0) { repository.insert(any()) }
        viewModel.errorFlow.test {
            assertEquals("A quiz template named 'history' already exists.", awaitItem())
        }
    }

    @Test
    fun `updateTemplate should call repository's update for valid change`() = runTest {
        val updatedTemplate = QuizTemplate(id = 1, name = "US History", numQuestions = 12, defaultMarks = emptyMap())
        val result = viewModel.updateTemplate(updatedTemplate)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result)
        coVerify { repository.update(updatedTemplate) }
        viewModel.errorFlow.test {
            assertEquals(null, awaitItem())
        }
    }

    @Test
    fun `updateTemplate should not call repository and post error for duplicate name`() = runTest {
        val updatedTemplate = QuizTemplate(id = 1, name = "Math", numQuestions = 10, defaultMarks = emptyMap())
        val result = viewModel.updateTemplate(updatedTemplate)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(result)
        coVerify(exactly = 0) { repository.update(any()) }
        viewModel.errorFlow.test {
            assertEquals("Another quiz template named 'Math' already exists.", awaitItem())
        }
    }

    @Test
    fun `updateTemplate should not call repository and post error for duplicate name case-insensitive`() = runTest {
        val updatedTemplate = QuizTemplate(id = 1, name = "math", numQuestions = 10, defaultMarks = emptyMap())
        val result = viewModel.updateTemplate(updatedTemplate)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(result)
        coVerify(exactly = 0) { repository.update(any()) }
        viewModel.errorFlow.test {
            assertEquals("Another quiz template named 'math' already exists.", awaitItem())
        }
    }

    @Test
    fun `updateTemplate should succeed when only changing case of the same item`() = runTest {
        val updatedTemplate = QuizTemplate(id = 1, name = "history", numQuestions = 10, defaultMarks = emptyMap())
        val result = viewModel.updateTemplate(updatedTemplate)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result)
        coVerify { repository.update(updatedTemplate) }
        viewModel.errorFlow.test {
            assertEquals(null, awaitItem())
        }
    }

    @Test
    fun `delete should call repository's delete`() = runTest {
        val templateToDelete = existingTemplates.first()
        viewModel.delete(templateToDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.delete(templateToDelete) }
    }
}
