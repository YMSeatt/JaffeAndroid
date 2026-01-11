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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: SettingsViewModel
    private lateinit var preferencesRepository: AppPreferencesRepository
    private val application: Application = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        preferencesRepository = mockk(relaxed = true)
        // Mock the constructor of SettingsViewModel to inject the mocked repository
        viewModel = SettingsViewModel(application)
        // Manually set the mocked repository
        viewModel.javaClass.getDeclaredField("preferencesRepository").apply {
            isAccessible = true
            set(viewModel, preferencesRepository)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `checkPassword with old SHA-256 hash migrates to SHA-512`() = runTest {
        val password = "password"
        val oldHash = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"
        val newHash = SecurityUtil.hashPassword(password, "SHA-512")

        coEvery { preferencesRepository.passwordHashFlow } returns flowOf(oldHash)

        val result = viewModel.checkPassword(password)

        assertTrue(result)
        coVerify { preferencesRepository.updatePasswordHash(newHash) }
    }
}
