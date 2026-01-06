package com.example.myapplication.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class SettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: SettingsViewModel
    private lateinit var application: Application

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        viewModel = SettingsViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `autoSendEmailOnClose is collected eagerly and updates correctly`() = runTest {
        // Given
        val initialValue = viewModel.autoSendEmailOnClose.value

        // When
        val newValue = !initialValue
        viewModel.updateAutoSendEmailOnClose(newValue)

        // Then
        // Wait for the flow to emit a value that is different from the initial one.
        val updatedValue = viewModel.autoSendEmailOnClose.first { it == newValue }
        assertThat(updatedValue).isEqualTo(newValue)
    }

    @Test
    fun `defaultEmailAddress is collected eagerly and updates correctly`() = runTest {
        // Given
        val initialValue = viewModel.defaultEmailAddress.value

        // When
        val newValue = "new.email.${System.currentTimeMillis()}@example.com"
        viewModel.updateDefaultEmailAddress(newValue)

        // Then
        // Wait for the flow to emit the new value.
        val updatedValue = viewModel.defaultEmailAddress.first { it == newValue }
        assertThat(updatedValue).isEqualTo(newValue)
    }
}
