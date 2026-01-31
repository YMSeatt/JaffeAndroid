package com.example.myapplication.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.example.myapplication.preferences.AppPreferencesRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
    lateinit var jsonImporter: com.example.myapplication.data.importer.JsonImporter
    @MockK(relaxed = true)
    lateinit var studentDao: com.example.myapplication.data.StudentDao
    @MockK(relaxed = true)
    lateinit var furnitureDao: com.example.myapplication.data.FurnitureDao
    @MockK(relaxed = true)
    lateinit var studentGroupDao: com.example.myapplication.data.StudentGroupDao
    @MockK(relaxed = true)
    lateinit var layoutTemplateDao: com.example.myapplication.data.LayoutTemplateDao
    @MockK(relaxed = true)
    lateinit var conditionalFormattingRuleDao: com.example.myapplication.data.ConditionalFormattingRuleDao
    @MockK(relaxed = true)
    lateinit var customBehaviorDao: com.example.myapplication.data.CustomBehaviorDao
    @MockK(relaxed = true)
    lateinit var customHomeworkTypeDao: com.example.myapplication.data.CustomHomeworkTypeDao
    @MockK(relaxed = true)
    lateinit var customHomeworkStatusDao: com.example.myapplication.data.CustomHomeworkStatusDao
    @MockK(relaxed = true)
    lateinit var quizMarkTypeDao: com.example.myapplication.data.QuizMarkTypeDao
    @MockK(relaxed = true)
    lateinit var quizTemplateDao: com.example.myapplication.data.QuizTemplateDao
    @MockK(relaxed = true)
    lateinit var homeworkTemplateDao: com.example.myapplication.data.HomeworkTemplateDao
    @MockK(relaxed = true)
    lateinit var behaviorEventDao: com.example.myapplication.data.BehaviorEventDao
    @MockK(relaxed = true)
    lateinit var homeworkLogDao: com.example.myapplication.data.HomeworkLogDao

    private lateinit var viewModel: SettingsViewModel

    private val autoSendEmailOnCloseFlow = kotlinx.coroutines.flow.MutableStateFlow(false)
    private val editModeEnabledFlow = kotlinx.coroutines.flow.MutableStateFlow(false)

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        val application = ApplicationProvider.getApplicationContext<Application>()

        // Mock default flows
        every { preferencesRepository.autoSendEmailOnCloseFlow } returns autoSendEmailOnCloseFlow
        io.mockk.coEvery { preferencesRepository.updateAutoSendEmailOnClose(any()) } answers {
            autoSendEmailOnCloseFlow.value = it.invocation.args[0] as Boolean
        }
        every { preferencesRepository.editModeEnabledFlow } returns editModeEnabledFlow
        io.mockk.coEvery { preferencesRepository.updateEditModeEnabled(any()) } answers {
            editModeEnabledFlow.value = it.invocation.args[0] as Boolean
        }
        every { preferencesRepository.recentLogsLimitFlow } returns flowOf(3)
        every { preferencesRepository.recentHomeworkLogsLimitFlow } returns flowOf(3)
        every { preferencesRepository.recentBehaviorIncidentsLimitFlow } returns flowOf(3)
        every { preferencesRepository.useInitialsForBehaviorFlow } returns flowOf(false)
        every { preferencesRepository.useInitialsForHomeworkFlow } returns flowOf(false)
        every { preferencesRepository.useInitialsForQuizFlow } returns flowOf(false)
        every { preferencesRepository.homeworkInitialsMapFlow } returns flowOf("")
        every { preferencesRepository.quizInitialsMapFlow } returns flowOf("")
        every { preferencesRepository.useFullNameForStudentFlow } returns flowOf(false)
        every { preferencesRepository.useBoldFontFlow } returns flowOf(false)
        every { preferencesRepository.liveHomeworkSessionModeFlow } returns flowOf("Yes/No")
        every { preferencesRepository.liveHomeworkSelectOptionsFlow } returns flowOf("Done,Not Done,Signed,Returned")
        every { preferencesRepository.appThemeFlow } returns flowOf("SYSTEM")
        every { preferencesRepository.emailPasswordFlow } returns flowOf("")
        every { preferencesRepository.showRecentBehaviorFlow } returns flowOf(true)
        every { preferencesRepository.defaultStudentBoxWidthFlow } returns flowOf(120)
        every { preferencesRepository.defaultStudentBoxHeightFlow } returns flowOf(80)
        every { preferencesRepository.defaultStudentBoxBackgroundColorFlow } returns flowOf("#FFFFFF")
        every { preferencesRepository.defaultStudentBoxOutlineColorFlow } returns flowOf("#000000")
        every { preferencesRepository.defaultStudentBoxTextColorFlow } returns flowOf("#000000")
        every { preferencesRepository.defaultStudentBoxOutlineThicknessFlow } returns flowOf(1)
        every { preferencesRepository.defaultStudentBoxCornerRadiusFlow } returns flowOf(4)
        every { preferencesRepository.defaultStudentBoxPaddingFlow } returns flowOf(4)
        every { preferencesRepository.passwordEnabledFlow } returns flowOf(false)
        every { preferencesRepository.stickyQuizNameDurationSecondsFlow } returns flowOf(0)
        every { preferencesRepository.stickyHomeworkNameDurationSecondsFlow } returns flowOf(0)
        every { preferencesRepository.behaviorInitialsMapFlow } returns flowOf("")
        every { preferencesRepository.lastQuizNameFlow } returns flowOf("")
        every { preferencesRepository.lastQuizTimestampFlow } returns flowOf(0L)
        every { preferencesRepository.lastHomeworkNameFlow } returns flowOf("")
        every { preferencesRepository.lastHomeworkTimestampFlow } returns flowOf(0L)
        every { preferencesRepository.noAnimationsFlow } returns flowOf(false)
        every { preferencesRepository.autosaveIntervalFlow } returns flowOf(30000)
        every { preferencesRepository.gridSnapEnabledFlow } returns flowOf(false)
        every { preferencesRepository.gridSizeFlow } returns flowOf(20)
        every { preferencesRepository.showRulersFlow } returns flowOf(false)
        every { preferencesRepository.showGridFlow } returns flowOf(false)
        every { preferencesRepository.autoExpandStudentBoxesFlow } returns flowOf(true)
        every { preferencesRepository.lastExportPathFlow } returns flowOf("")
        every { preferencesRepository.encryptDataFilesFlow } returns flowOf(true)
        every { preferencesRepository.defaultEmailAddressFlow } returns flowOf("behaviorlogger@gmail.com")
        every { preferencesRepository.canvasBackgroundColorFlow } returns flowOf("#FFFFFF")
        every { preferencesRepository.guidesStayWhenRulersHiddenFlow } returns flowOf(false)
        every { preferencesRepository.behaviorDisplayTimeoutFlow } returns flowOf(0)
        every { preferencesRepository.homeworkDisplayTimeoutFlow } returns flowOf(0)
        every { preferencesRepository.quizDisplayTimeoutFlow } returns flowOf(0)
        every { preferencesRepository.smtpSettingsFlow } returns flowOf(com.example.myapplication.data.SmtpSettings())

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
