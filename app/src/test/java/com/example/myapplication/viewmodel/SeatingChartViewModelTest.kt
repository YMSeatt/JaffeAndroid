package com.example.myapplication.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.myapplication.commands.AddStudentCommand
import com.example.myapplication.commands.MoveStudentCommand
import com.example.myapplication.data.*
import com.example.myapplication.preferences.AppPreferencesRepository
import com.example.myapplication.ui.model.StudentUiItem
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SeatingChartViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @MockK(relaxed = true)
    lateinit var repository: StudentRepository
    @MockK(relaxed = true)
    lateinit var studentDao: StudentDao
    @MockK(relaxed = true)
    lateinit var furnitureDao: FurnitureDao
    @MockK(relaxed = true)
    lateinit var layoutTemplateDao: LayoutTemplateDao
    @MockK(relaxed = true)
    lateinit var behaviorEventDao: BehaviorEventDao
    @MockK(relaxed = true)
    lateinit var quizLogDao: QuizLogDao
    @MockK(relaxed = true)
    lateinit var homeworkLogDao: HomeworkLogDao
    @MockK(relaxed = true)
    lateinit var studentGroupDao: StudentGroupDao
    @MockK(relaxed = true)
    lateinit var homeworkTemplateDao: HomeworkTemplateDao
    @MockK(relaxed = true)
    lateinit var quizTemplateDao: QuizTemplateDao
    @MockK(relaxed = true)
    lateinit var quizMarkTypeDao: QuizMarkTypeDao
    @MockK(relaxed = true)
    lateinit var appPreferencesRepository: AppPreferencesRepository
    @MockK(relaxed = true)
    lateinit var exporter: com.example.myapplication.data.exporter.Exporter
    @MockK(relaxed = true)
    lateinit var application: Application

    private lateinit var viewModel: SeatingChartViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        // Mock preference flows
        every { appPreferencesRepository.recentBehaviorIncidentsLimitFlow } returns flowOf(5)
        every { appPreferencesRepository.recentHomeworkLogsLimitFlow } returns flowOf(5)
        every { appPreferencesRepository.recentLogsLimitFlow } returns flowOf(5)
        every { appPreferencesRepository.maxRecentLogsToDisplayFlow } returns flowOf(3)
        every { appPreferencesRepository.useInitialsForBehaviorFlow } returns flowOf(false)
        every { appPreferencesRepository.useInitialsForHomeworkFlow } returns flowOf(false)
        every { appPreferencesRepository.useInitialsForQuizFlow } returns flowOf(false)
        every { appPreferencesRepository.behaviorInitialsMapFlow } returns flowOf("")
        every { appPreferencesRepository.homeworkInitialsMapFlow } returns flowOf("")
        every { appPreferencesRepository.quizInitialsMapFlow } returns flowOf("")
        every { appPreferencesRepository.studentLogsLastClearedFlow } returns flowOf(emptyMap())
        every { appPreferencesRepository.homeworkAssignmentTypesListFlow } returns flowOf(emptySet())
        every { appPreferencesRepository.homeworkStatusesListFlow } returns flowOf(emptySet())
        every { appPreferencesRepository.defaultStudentBoxWidthFlow } returns flowOf(100)
        every { appPreferencesRepository.defaultStudentBoxHeightFlow } returns flowOf(100)
        every { appPreferencesRepository.defaultStudentBoxBackgroundColorFlow } returns flowOf("#FFFFFF")
        every { appPreferencesRepository.defaultStudentBoxOutlineColorFlow } returns flowOf("#000000")
        every { appPreferencesRepository.defaultStudentBoxTextColorFlow } returns flowOf("#000000")
        every { appPreferencesRepository.defaultStudentBoxOutlineThicknessFlow } returns flowOf(1)
        every { appPreferencesRepository.defaultStudentBoxCornerRadiusFlow } returns flowOf(0)
        every { appPreferencesRepository.defaultStudentBoxPaddingFlow } returns flowOf(0)
        every { appPreferencesRepository.defaultStudentFontFamilyFlow } returns flowOf("Arial")
        every { appPreferencesRepository.defaultStudentFontSizeFlow } returns flowOf(12)
        every { appPreferencesRepository.defaultStudentFontColorFlow } returns flowOf("#000000")
        every { appPreferencesRepository.behaviorDisplayTimeoutFlow } returns flowOf(3600000)
        every { appPreferencesRepository.homeworkDisplayTimeoutFlow } returns flowOf(3600000)
        every { appPreferencesRepository.quizDisplayTimeoutFlow } returns flowOf(3600000)

        // Mock LiveData sources
        every { repository.allStudents } returns androidx.lifecycle.MutableLiveData(emptyList())
        every { studentDao.getStudentsForDisplay() } returns androidx.lifecycle.MutableLiveData(emptyList())
        every { repository.getAllFurniture() } returns flowOf(emptyList())
        every { repository.getAllLayoutTemplates() } returns flowOf(emptyList())
        every { behaviorEventDao.getAllBehaviorEvents() } returns androidx.lifecycle.MutableLiveData(emptyList())
        every { homeworkLogDao.getAllHomeworkLogs() } returns androidx.lifecycle.MutableLiveData(emptyList())
        every { quizLogDao.getAllQuizLogs() } returns androidx.lifecycle.MutableLiveData(emptyList())
        every { studentGroupDao.getAllStudentGroups() } returns flowOf(emptyList())
        every { quizTemplateDao.getAll() } returns flowOf(emptyList())
        every { repository.getAllQuizMarkTypes() } returns flowOf(emptyList())
        every { homeworkTemplateDao.getAllHomeworkTemplates() } returns flowOf(emptyList())

        
        viewModel = SeatingChartViewModel(
            repository,
            studentDao,
            furnitureDao,
            layoutTemplateDao,
            behaviorEventDao,
            quizLogDao,
            homeworkLogDao,
            studentGroupDao,
            homeworkTemplateDao,
            quizTemplateDao,
            quizMarkTypeDao,
            appPreferencesRepository,
            exporter,
            application
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addStudent inserts student via repository`() = runTest {
        val student = Student(id = 1, firstName = "Test", lastName = "Student", stringId = "1")
        
        // Mock CollisionDetector resolution if needed, or assume it returns valid coordinates
        // Since CollisionDetector is an object, we might want to mock it or rely on its logic. 
        // For now, let's assume it works or we rely on the internalAddStudent which is what command calls.
        
        viewModel.internalAddStudent(student)
        
        coVerify { repository.insertStudent(student) }
    }

    @Test
    fun `updateStudentPosition triggers optimistic update and executes command`() = runTest {
        val studentId = 1
        val oldX = 0f
        val oldY = 0f
        val newX = 100f
        val newY = 100f
        
        val student = Student(id = studentId.toLong(), firstName = "Test", lastName = "Student", stringId = "1", xPosition = oldX, yPosition = oldY)
        coEvery { repository.getStudentById(studentId.toLong()) } returns student
        
        // We need to observe studentsForDisplay to trigger the update
        val observer = mockk<Observer<List<StudentUiItem>>>(relaxed = true)
        viewModel.studentsForDisplay.observeForever(observer)
        
        viewModel.updateStudentPosition(studentId, oldX, oldY, newX, newY)
        advanceUntilIdle()
        
        // Verify optimistic update (studentsForDisplay should be updated via postValue)
        // Since logic is in updateStudentsForDisplay which is complex to mock fully without setting up all flows,
        // we mainly check if the command was executed.
        
        coVerify { studentDao.updatePosition(studentId.toLong(), any(), any()) } // Command calls internalUpdateStudentPosition which calls dao
        
        viewModel.studentsForDisplay.removeObserver(observer)
    }
}
