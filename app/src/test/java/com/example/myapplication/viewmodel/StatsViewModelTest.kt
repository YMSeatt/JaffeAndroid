package com.example.myapplication.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.data.*
import com.example.myapplication.data.exporter.ExportOptions
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.google.common.truth.Truth.assertThat
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val studentDao: StudentDao = mockk()
    private val behaviorEventDao: BehaviorEventDao = mockk()
    private val homeworkLogDao: HomeworkLogDao = mockk()
    private val quizLogDao: QuizLogDao = mockk()
    private val quizMarkTypeDao: QuizMarkTypeDao = mockk()
    private val customBehaviorDao: CustomBehaviorDao = mockk()
    private val customHomeworkTypeDao: CustomHomeworkTypeDao = mockk()

    private lateinit var viewModel: StatsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { studentDao.getAllStudentsNonLiveData() } returns emptyList()
        coEvery { behaviorEventDao.getAllBehaviorEventsList() } returns emptyList()
        coEvery { homeworkLogDao.getAllHomeworkLogsList() } returns emptyList()
        coEvery { quizLogDao.getAllQuizLogsList() } returns emptyList()
        coEvery { quizMarkTypeDao.getAllQuizMarkTypesList() } returns emptyList()
        every { studentDao.getAllStudents() } returns MutableLiveData(emptyList())
        every { customBehaviorDao.getAllCustomBehaviors() } returns MutableLiveData(emptyList())
        every { customHomeworkTypeDao.getAllCustomHomeworkTypes() } returns MutableLiveData(emptyList())

        viewModel = StatsViewModel(
            studentDao,
            behaviorEventDao,
            homeworkLogDao,
            quizLogDao,
            quizMarkTypeDao,
            customBehaviorDao,
            customHomeworkTypeDao
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `calculateAttendanceSummary correctly counts present days`() {
        val cal = Calendar.getInstance()
        cal.set(2024, Calendar.JANUARY, 1, 10, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val day1 = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val day2 = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val day3 = cal.timeInMillis

        val student = Student(id = 1, firstName = "John", lastName = "Doe")
        val behaviorEvent = BehaviorEvent(studentId = 1, type = "Talking", timestamp = day1, comment = null)
        val homeworkLog = HomeworkLog(studentId = 1, assignmentName = "Math", status = "Done", loggedAt = day3)

        val options = ExportOptions(startDate = day1, endDate = day3)

        val (summaryList, totalDays) = viewModel.calculateAttendanceSummary(
            options,
            listOf(student),
            listOf(behaviorEvent),
            listOf(homeworkLog),
            emptyList()
        )

        assertThat(totalDays).isEqualTo(3)
        assertThat(summaryList).hasSize(1)

        val summary = summaryList.first()
        assertThat(summary.studentName).isEqualTo("John Doe")
        assertThat(summary.daysPresent).isEqualTo(2) // day1 and day3
        assertThat(summary.daysAbsent).isEqualTo(1)  // day2
        assertThat(summary.attendancePercentage).isWithin(0.1).of(66.6)
    }
}
