package com.example.myapplication.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.myapplication.preferences.AppPreferencesRepository
import com.example.myapplication.util.SecurityUtil
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
import org.robolectric.RuntimeEnvironment

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class SettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: SettingsViewModel
    private lateinit var mockPreferencesRepository: AppPreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockPreferencesRepository = mockk(relaxed = true)

        val application = RuntimeEnvironment.getApplication()
        viewModel = SettingsViewModel(application)

        // Use reflection to inject the mock repository
        val repositoryField = viewModel.javaClass.getDeclaredField("preferencesRepository")
        repositoryField.isAccessible = true
        repositoryField.set(viewModel, mockPreferencesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `checkPassword returns true for correct password`() = runTest {
        val password = "password123"
        val hashedPassword = SecurityUtil.hashPassword(password)
        coEvery { mockPreferencesRepository.passwordHashFlow } returns flowOf(hashedPassword)

        val result = viewModel.checkPassword(password)

        assertTrue(result)
    }

    @Test
    fun `checkPassword returns false for incorrect password`() = runTest {
        val password = "password123"
        val wrongPassword = "wrongpassword"
        val hashedPassword = SecurityUtil.hashPassword(password)
        coEvery { mockPreferencesRepository.passwordHashFlow } returns flowOf(hashedPassword)

        val result = viewModel.checkPassword(wrongPassword)

        assertFalse(result)
    }

    @Test
    fun `checkPassword returns true for legacy password`() = runTest {
        val password = "password"
        // This is the SHA-256 hash of "password"
        val legacyHash = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"
        coEvery { mockPreferencesRepository.passwordHashFlow } returns flowOf(legacyHash)

        val result = viewModel.checkPassword(password)

        assertTrue(result)
    }

    @Test
    fun `setPassword updates hash and enables password protection`() = runTest {
        val password = "newPassword"

        viewModel.setPassword(password)
        testDispatcher.scheduler.advanceUntilIdle() // Ensure coroutine launched in viewModelScope completes

        coVerify { mockPreferencesRepository.updatePasswordHash(any()) }
        coVerify { mockPreferencesRepository.updatePasswordEnabled(true) }
    }

    @Test
    fun `setPassword with blank string clears hash and disables password protection`() = runTest {
        viewModel.setPassword("")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockPreferencesRepository.updatePasswordHash("") }
        coVerify { mockPreferencesRepository.updatePasswordEnabled(false) }
    }
}
