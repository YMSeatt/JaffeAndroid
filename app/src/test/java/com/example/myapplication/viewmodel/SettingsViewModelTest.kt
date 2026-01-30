package com.example.myapplication.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.example.myapplication.data.*
import com.example.myapplication.data.importer.JsonImporter
import com.example.myapplication.preferences.AppPreferencesRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
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

    @MockK(relaxed = true)
    lateinit var preferencesRepository: AppPreferencesRepository
    @MockK(relaxed = true)
    lateinit var jsonImporter: JsonImporter
    @MockK(relaxed = true)
    lateinit var studentDao: StudentDao
    @MockK(relaxed = true)
    lateinit var furnitureDao: FurnitureDao
    @MockK(relaxed = true)
    lateinit var studentGroupDao: StudentGroupDao
    @MockK(relaxed = true)
    lateinit var layoutTemplateDao: LayoutTemplateDao
    @MockK(relaxed = true)
    lateinit var conditionalFormattingRuleDao: ConditionalFormattingRuleDao
    @MockK(relaxed = true)
    lateinit var customBehaviorDao: CustomBehaviorDao
    @MockK(relaxed = true)
    lateinit var customHomeworkTypeDao: CustomHomeworkTypeDao
    @MockK(relaxed = true)
    lateinit var customHomeworkStatusDao: CustomHomeworkStatusDao
    @MockK(relaxed = true)
    lateinit var quizMarkTypeDao: QuizMarkTypeDao
    @MockK(relaxed = true)
    lateinit var quizTemplateDao: QuizTemplateDao
    @MockK(relaxed = true)
    lateinit var homeworkTemplateDao: HomeworkTemplateDao
    @MockK(relaxed = true)
    lateinit var behaviorEventDao: BehaviorEventDao
    @MockK(relaxed = true)
    lateinit var homeworkLogDao: HomeworkLogDao

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        val application = ApplicationProvider.getApplicationContext<Application>()

        every { preferencesRepository.autoSendEmailOnCloseFlow } returns MutableStateFlow(false)
        every { preferencesRepository.editModeEnabledFlow } returns MutableStateFlow(false)
        every { preferencesRepository.appThemeFlow } returns MutableStateFlow("SYSTEM")
        every { preferencesRepository.emailPasswordFlow } returns MutableStateFlow("")

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
