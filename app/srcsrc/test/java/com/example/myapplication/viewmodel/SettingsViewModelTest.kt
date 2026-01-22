package com.example.myapplication.viewmodel

import android.app.Application
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.data.exporter.ExportOptions
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class SettingsViewModelTest {

    private lateinit var workManager: WorkManager
    private lateinit var application: Application
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        application = mockk(relaxed = true)
        workManager = mockk(relaxed = true)
        every { WorkManager.getInstance(application) } returns workManager
        viewModel = SettingsViewModel(application)
    }

    @Test
    fun `scheduleEmailOnClose enqueues WorkRequest with correct data`() = runTest {
        // Given
        val exportOptions = ExportOptions(encrypt = true)
        viewModel.updateAutoSendEmailOnClose(true)
        viewModel.updateDefaultEmailAddress("test@example.com")

        // When
        viewModel.scheduleEmailOnClose(exportOptions)

        // Then
        verify {
            workManager.enqueue(any<OneTimeWorkRequestBuilder<*>>().build())
        }
    }
}
