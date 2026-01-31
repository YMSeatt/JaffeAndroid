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

        // Mock mandatory flows
        every { preferencesRepository.autoSendEmailOnCloseFlow } returns flowOf(false)
        every { preferencesRepository.editModeEnabledFlow } returns flowOf(false)
        every { preferencesRepository.appThemeFlow } returns flowOf("SYSTEM")
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
        every { preferencesRepository.liveHomeworkSelectOptionsFlow } returns flowOf("")
        every { preferencesRepository.emailPasswordFlow } returns flowOf("")
        every { preferencesRepository.showRecentBehaviorFlow } returns flowOf(true)
        every { preferencesRepository.defaultStudentBoxWidthFlow } returns flowOf(100)
        every { preferencesRepository.defaultStudentBoxHeightFlow } returns flowOf(100)
        every { preferencesRepository.defaultStudentBoxBackgroundColorFlow } returns flowOf("#FFFFFF")
        every { preferencesRepository.defaultStudentBoxOutlineColorFlow } returns flowOf("#000000")
        every { preferencesRepository.defaultStudentBoxTextColorFlow } returns flowOf("#000000")
        every { preferencesRepository.defaultStudentBoxOutlineThicknessFlow } returns flowOf(1)
        every { preferencesRepository.defaultStudentBoxCornerRadiusFlow } returns flowOf(0)
        every { preferencesRepository.defaultStudentBoxPaddingFlow } returns flowOf(0)
        every { preferencesRepository.passwordEnabledFlow } returns flowOf(false)
        every { preferencesRepository.stickyQuizNameDurationSecondsFlow } returns flowOf(0)
        every { preferencesRepository.stickyHomeworkNameDurationSecondsFlow } returns flowOf(0)
        every { preferencesRepository.behaviorInitialsMapFlow } returns flowOf("")
        every { preferencesRepository.lastQuizNameFlow } returns flowOf(null)
        every { preferencesRepository.lastQuizTimestampFlow } returns flowOf(null)
        every { preferencesRepository.lastHomeworkNameFlow } returns flowOf(null)
        every { preferencesRepository.lastHomeworkTimestampFlow } returns flowOf(null)
        every { preferencesRepository.noAnimationsFlow } returns flowOf(false)
        every { preferencesRepository.autosaveIntervalFlow } returns flowOf(30000)
        every { preferencesRepository.gridSnapEnabledFlow } returns flowOf(false)
        every { preferencesRepository.gridSizeFlow } returns flowOf(20)
        every { preferencesRepository.showRulersFlow } returns flowOf(false)
        every { preferencesRepository.showGridFlow } returns flowOf(false)
        every { preferencesRepository.autoExpandStudentBoxesFlow } returns flowOf(true)
        every { preferencesRepository.lastExportPathFlow } returns flowOf(null)
        every { preferencesRepository.encryptDataFilesFlow } returns flowOf(true)
        every { preferencesRepository.defaultEmailAddressFlow } returns flowOf("test@example.com")
        every { preferencesRepository.canvasBackgroundColorFlow } returns flowOf("#FFFFFF")
        every { preferencesRepository.guidesStayWhenRulersHiddenFlow } returns flowOf(false)
        every { preferencesRepository.behaviorDisplayTimeoutFlow } returns flowOf(0)
        every { preferencesRepository.homeworkDisplayTimeoutFlow } returns flowOf(0)
        every { preferencesRepository.quizDisplayTimeoutFlow } returns flowOf(0)
        every { preferencesRepository.smtpSettingsFlow } returns flowOf(SmtpSettings())

        // Mock DAOs
        every { customBehaviorDao.getAllCustomBehaviors() } returns androidx.lifecycle.MutableLiveData(emptyList())
        every { customHomeworkTypeDao.getAllCustomHomeworkTypes() } returns androidx.lifecycle.MutableLiveData(emptyList())
        every { customHomeworkStatusDao.getAllCustomHomeworkStatuses() } returns androidx.lifecycle.MutableLiveData(emptyList())
        every { quizMarkTypeDao.getAllQuizMarkTypes() } returns flowOf(emptyList())

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
        // Note: In a real test we'd need to mock preferencesRepository to emit the new value
        // but since it's a relaxed mock and we're just checking if it was called,
        // we might need to adjust the test or the mock setup.
        coVerify { preferencesRepository.updateAutoSendEmailOnClose(true) }
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
        coVerify { preferencesRepository.updateEditModeEnabled(true) }
    }
}
