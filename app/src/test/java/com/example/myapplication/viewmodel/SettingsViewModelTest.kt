package com.example.myapplication.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.example.myapplication.preferences.AppPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import com.example.myapplication.data.BehaviorEventDao
import com.example.myapplication.data.ConditionalFormattingRuleDao
import com.example.myapplication.data.CustomBehaviorDao
import com.example.myapplication.data.CustomHomeworkStatusDao
import com.example.myapplication.data.CustomHomeworkTypeDao
import com.example.myapplication.data.FurnitureDao
import com.example.myapplication.data.HomeworkLogDao
import com.example.myapplication.data.HomeworkTemplateDao
import com.example.myapplication.data.LayoutTemplateDao
import com.example.myapplication.data.QuizMarkTypeDao
import com.example.myapplication.data.QuizTemplateDao
import com.example.myapplication.data.StudentDao
import com.example.myapplication.data.StudentGroupDao
import com.example.myapplication.data.importer.JsonImporter
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class SettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: SettingsViewModel

    @MockK(relaxed = true)
    private lateinit var application: Application
    @MockK(relaxed = true)
    private lateinit var preferencesRepository: AppPreferencesRepository
    @MockK(relaxed = true)
    private lateinit var jsonImporter: JsonImporter
    @MockK(relaxed = true)
    private lateinit var studentDao: StudentDao
    @MockK(relaxed = true)
    private lateinit var furnitureDao: FurnitureDao
    @MockK(relaxed = true)
    private lateinit var studentGroupDao: StudentGroupDao
    @MockK(relaxed = true)
    private lateinit var layoutTemplateDao: LayoutTemplateDao
    @MockK(relaxed = true)
    private lateinit var conditionalFormattingRuleDao: ConditionalFormattingRuleDao
    @MockK(relaxed = true)
    private lateinit var customBehaviorDao: CustomBehaviorDao
    @MockK(relaxed = true)
    private lateinit var customHomeworkTypeDao: CustomHomeworkTypeDao
    @MockK(relaxed = true)
    private lateinit var customHomeworkStatusDao: CustomHomeworkStatusDao
    @MockK(relaxed = true)
    private lateinit var quizMarkTypeDao: QuizMarkTypeDao
    @MockK(relaxed = true)
    private lateinit var quizTemplateDao: QuizTemplateDao
    @MockK(relaxed = true)
    private lateinit var homeworkTemplateDao: HomeworkTemplateDao
    @MockK(relaxed = true)
    private lateinit var behaviorEventDao: BehaviorEventDao
    @MockK(relaxed = true)
    private lateinit var homeworkLogDao: HomeworkLogDao


    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = SettingsViewModel(
            application,
            preferencesRepository,
            jsonImporter,
            studentDao,
            furnitureDao,
            studentGroupDao,
            layoutTemplateDao,
            conditionalFormattingRuleDao,
            customBehaviorDao,
            customHomeworkTypeDao,
            customHomeworkStatusDao,
            quizMarkTypeDao,
            quizTemplateDao,
            homeworkTemplateDao,
            behaviorEventDao,
            homeworkLogDao
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `autoSendEmailOnClose StateFlow should reflect updated value`() = runTest {
        // Given
        val initialValue = viewModel.autoSendEmailOnClose.value
        assertFalse("Initial value should be false", initialValue)

        // When
        viewModel.updateAutoSendEmailOnClose(true)
        testDispatcher.scheduler.advanceUntilIdle() // Ensure the update coroutine completes

        // Then
        val updatedValue = viewModel.autoSendEmailOnClose.value
        assertTrue("Value should be true after update", updatedValue)
    }

    @Test
    fun `editModeEnabled StateFlow should reflect updated value`() = runTest {
        // Given
        val initialValue = viewModel.editModeEnabled.value
        assertFalse("Initial value should be false", initialValue)

        // When
        viewModel.updateEditModeEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val updatedValue = viewModel.editModeEnabled.value
        assertTrue("Value should be true after update", updatedValue)
    }
}
