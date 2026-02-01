package com.example.myapplication.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.example.myapplication.preferences.AppPreferencesRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private lateinit var viewModel: SettingsViewModel
    private val preferencesRepository = mockk<AppPreferencesRepository>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val application = ApplicationProvider.getApplicationContext<Application>()

        // Mock StateFlows for constructor initialization
        every { preferencesRepository.autoSendEmailOnCloseFlow } returns kotlinx.coroutines.flow.flowOf(false)
        every { preferencesRepository.editModeEnabledFlow } returns kotlinx.coroutines.flow.flowOf(false)
        every { preferencesRepository.appThemeFlow } returns kotlinx.coroutines.flow.flowOf("SYSTEM")
        every { preferencesRepository.emailPasswordFlow } returns kotlinx.coroutines.flow.flowOf(null)
        every { preferencesRepository.defaultEmailAddressFlow } returns kotlinx.coroutines.flow.flowOf("behaviorlogger@gmail.com")

        viewModel = SettingsViewModel(
            application,
            preferencesRepository,
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true)
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
