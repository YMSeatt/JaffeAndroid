package com.example.myapplication.viewmodel

import android.app.Application
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.myapplication.data.exporter.ExportOptions
import com.example.myapplication.preferences.AppPreferencesRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class SettingsViewModelTest {

    private lateinit var application: Application
    private lateinit var workManager: WorkManager
    private lateinit var preferencesRepository: AppPreferencesRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        application = RuntimeEnvironment.getApplication()
        workManager = mockk(relaxed = true)
        preferencesRepository = mockk(relaxed = true)
        viewModel = SettingsViewModel(application, workManager, preferencesRepository)
    }

    @Test
    fun `handleOnStop enqueues work with default options when autoSendEmailOnClose is true and options are null`() = runTest {
        // Given
        val email = "test@example.com"
        coEvery { preferencesRepository.autoSendEmailOnCloseFlow } returns flowOf(true)
        coEvery { preferencesRepository.defaultEmailAddressFlow } returns flowOf(email)
        val requestSlot = slot<OneTimeWorkRequest>()

        // When
        viewModel.handleOnStop(null)

        // Then
        verify(exactly = 1) { workManager.enqueue(capture(requestSlot)) }
        val capturedRequest = requestSlot.captured
        val workData = capturedRequest.workSpec.input
        val resultOptions = Json.decodeFromString(ExportOptions.serializer(), workData.getString("export_options")!!)
        assertThat(workData.getString("email_address")).isEqualTo(email)
        assertThat(resultOptions).isEqualTo(ExportOptions())
    }

    @Test
    fun `handleOnStop enqueues work with provided options when autoSendEmailOnClose is true`() = runTest {
        // Given
        val email = "test@example.com"
        val customOptions = ExportOptions(includeBehaviorLogs = false, includeQuizLogs = false)
        coEvery { preferencesRepository.autoSendEmailOnCloseFlow } returns flowOf(true)
        coEvery { preferencesRepository.defaultEmailAddressFlow } returns flowOf(email)
        val requestSlot = slot<OneTimeWorkRequest>()

        // When
        viewModel.handleOnStop(customOptions)

        // Then
        verify(exactly = 1) { workManager.enqueue(capture(requestSlot)) }
        val capturedRequest = requestSlot.captured
        val workData = capturedRequest.workSpec.input
        val resultOptions = Json.decodeFromString(ExportOptions.serializer(), workData.getString("export_options")!!)
        assertThat(workData.getString("email_address")).isEqualTo(email)
        assertThat(resultOptions).isEqualTo(customOptions)
    }


    @Test
    fun `handleOnStop does not enqueue work when autoSendEmailOnClose is false`() = runTest {
        // Given
        coEvery { preferencesRepository.autoSendEmailOnCloseFlow } returns flowOf(false)

        // When
        viewModel.handleOnStop(null)

        // Then
        verify(exactly = 0) { workManager.enqueue(any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `handleOnStop does not enqueue work when email is blank`() = runTest {
        // Given
        coEvery { preferencesRepository.autoSendEmailOnCloseFlow } returns flowOf(true)
        coEvery { preferencesRepository.defaultEmailAddressFlow } returns flowOf("") // Blank email

        // When
        viewModel.handleOnStop(null)

        // Then
        verify(exactly = 0) { workManager.enqueue(any<OneTimeWorkRequest>()) }
    }
}
