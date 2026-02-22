package com.example.myapplication.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.myapplication.commands.ItemType
import com.example.myapplication.data.*
import com.example.myapplication.preferences.AppPreferencesRepository
import com.example.myapplication.ui.model.ChartItemId
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.ui.model.FurnitureUiItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalCoroutinesApi::class)
class SeatingChartLayoutToolsTest {

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
    lateinit var appPreferencesRepository: AppPreferencesRepository
    @MockK(relaxed = true)
    lateinit var application: Application

    private lateinit var viewModel: SeatingChartViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        // Mock minimal requirements for SeatingChartViewModel init
        every { appPreferencesRepository.userPreferencesFlow } returns flowOf(mockk(relaxed = true))
        every { repository.allStudents } returns MutableLiveData(emptyList())
        every { repository.getAllFurniture() } returns flowOf(emptyList())
        every { repository.getAllLayoutTemplates() } returns flowOf(emptyList())
        every { repository.getAllQuizMarkTypes() } returns flowOf(emptyList())
        every { appPreferencesRepository.homeworkAssignmentTypesListFlow } returns flowOf(emptySet())
        every { appPreferencesRepository.homeworkStatusesListFlow } returns flowOf(emptySet())

        viewModel = SeatingChartViewModel(
            repository, studentDao, furnitureDao, mockk(relaxed = true),
            mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true),
            mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true),
            mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true),
            mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true),
            appPreferencesRepository, mockk(relaxed = true), application
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `alignSelectedItems center_h calculates correct target and moves items`() = runTest {
        // Prepare items
        val studentUi = mockk<StudentUiItem>(relaxed = true) {
            every { id } returns 1
            every { xPosition } returns mutableStateOf(0f)
            every { yPosition } returns mutableStateOf(0f)
            every { displayWidth } returns mutableStateOf(100.dp)
            every { displayHeight } returns mutableStateOf(100.dp)
        }
        val furnitureUi = FurnitureUiItem(
            id = 2, stringId = "2", name = mutableStateOf("Desk"), type = mutableStateOf("desk"),
            xPosition = mutableStateOf(200f), yPosition = mutableStateOf(0f), displayWidth = mutableStateOf(200.dp), displayHeight = mutableStateOf(100.dp),
            displayBackgroundColor = mutableStateOf(Color.Gray), displayOutlineColor = mutableStateOf(Color.Black),
            displayTextColor = mutableStateOf(Color.White), displayOutlineThickness = mutableStateOf(1.dp)
        )

        viewModel.studentsForDisplay.value = listOf(studentUi)
        viewModel.furnitureForDisplay.value = listOf(furnitureUi)

        viewModel.selectedItemIds.value = setOf(
            ChartItemId(1, ItemType.STUDENT),
            ChartItemId(2, ItemType.FURNITURE)
        )

        // Alignment bounds:
        // Student: x=0, w=100 -> right=100
        // Furniture: x=200, w=200 -> right=400
        // MinX = 0, MaxX = 400. Center = 200.
        // Student newX = 200 - 100/2 = 150.
        // Furniture newX = 200 - 200/2 = 100.

        viewModel.alignSelectedItems("center_h")
        advanceUntilIdle()

        // Check if student position was updated in UI (optimistic)
        assertEquals(150f, studentUi.xPosition.value)
        assertEquals(100f, furnitureUi.xPosition.value)
    }
}
