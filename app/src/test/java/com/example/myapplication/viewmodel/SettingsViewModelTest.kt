package com.example.myapplication.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.exporter.ExportOptions
import com.example.myapplication.preferences.AppPreferencesRepository
import com.example.myapplication.util.EmailWorker
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: SettingsViewModel
    private lateinit var appPreferencesRepository: AppPreferencesRepository
    private lateinit var workManager: WorkManager
    private lateinit var application: Application

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        application = mockk(relaxed = true)
        appPreferencesRepository = mockk(relaxed = true)
        workManager = mockk(relaxed = true)
        val appDatabase: AppDatabase = mockk(relaxed = true)

        // Mock WorkManager
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(application) } returns workManager

        viewModel = SettingsViewModel(application, appPreferencesRepository, appDatabase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `handleOnStop enqueues EmailWorker when auto-send is enabled`() = runTest {
        // Given
        val testEmail = "test@example.com"
        val exportOptions = ExportOptions(separateSheets = true) // Using a non-default value

        every { appPreferencesRepository.autoSendEmailOnCloseFlow } returns flowOf(true)
        every { appPreferencesRepository.defaultEmailAddressFlow } returns flowOf(testEmail)

        val workRequestSlot = slot<OneTimeWorkRequest>()
        every { workManager.enqueue(capture(workRequestSlot)) } returns mockk()

        // When
        viewModel.handleOnStop(exportOptions)
        testDispatcher.scheduler.advanceUntilIdle()


        // Then
        verify { workManager.enqueue(any<OneTimeWorkRequest>()) }
        val capturedWorkRequest = workRequestSlot.captured
        assertEquals(EmailWorker::class.java.name, capturedWorkRequest.workSpec.workerClassName)
        assertEquals(testEmail, capturedWorkRequest.workSpec.input.getString("email_address"))
        val capturedJson = capturedWorkRequest.workSpec.input.getString("export_options")
        val capturedOptions = Json.decodeFromString<ExportOptions>(capturedJson!!)
        assertEquals(exportOptions, capturedOptions)
    }

    @Test
    fun `handleOnStop does nothing when auto-send is disabled`() = runTest {
        // Given
        every { appPreferencesRepository.autoSendEmailOnCloseFlow } returns flowOf(false)

        // When
        viewModel.handleOnStop(ExportOptions())
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(exactly = 0) { workManager.enqueue(any<OneTimeWorkRequest>()) }
    }
}
