package com.example.myapplication.viewmodel

import com.example.myapplication.data.QuizTemplate
import com.example.myapplication.data.QuizTemplateRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class QuizTemplateViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: QuizTemplateViewModel
    private val repository: QuizTemplateRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = QuizTemplateViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `insert should call repository's insert`() = runTest {
        val quizTemplate = QuizTemplate(name = "Test", numQuestions = 10, defaultMarks = emptyMap())
        viewModel.insert(quizTemplate)
        coVerify { repository.insert(quizTemplate) }
    }

    @Test
    fun `update should call repository's update`() = runTest {
        val quizTemplate = QuizTemplate(id = 1, name = "Test", numQuestions = 10, defaultMarks = emptyMap())
        viewModel.update(quizTemplate)
        coVerify { repository.update(quizTemplate) }
    }

    @Test
    fun `delete should call repository's delete`() = runTest {
        val quizTemplate = QuizTemplate(id = 1, name = "Test", numQuestions = 10, defaultMarks = emptyMap())
        viewModel.delete(quizTemplate)
        coVerify { repository.delete(quizTemplate) }
    }
}
