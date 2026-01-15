package com.example.myapplication.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.myapplication.preferences.AppPreferencesRepository
import com.example.myapplication.util.SecurityUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

import com.example.myapplication.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class SettingsViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: SettingsViewModel
    private lateinit var preferencesRepository: AppPreferencesRepository

    @Before
    fun setup() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        preferencesRepository = mock(AppPreferencesRepository::class.java)
        viewModel = SettingsViewModel(application, preferencesRepository)
    }

    @Test
    fun `checkPassword should return true for correct SHA-512 password`() = runBlocking {
        val password = "password123"
        val hash = SecurityUtil.hashPassword(password, "SHA-512")
        `when`(preferencesRepository.passwordHashFlow).thenReturn(flowOf(hash))

        val result = viewModel.checkPassword(password)

        assert(result)
    }

    @Test
    fun `checkPassword should return true for correct SHA-256 password and upgrade hash`() = runBlocking {
        val password = "password123"
        val oldHash = SecurityUtil.hashPassword(password, "SHA-256")
        val newHash = SecurityUtil.hashPassword(password, "SHA-512")
        `when`(preferencesRepository.passwordHashFlow).thenReturn(flowOf(oldHash))

        val result = viewModel.checkPassword(password)

        assert(result)
        verify(preferencesRepository).updatePasswordHash(newHash)
    }

    @Test
    fun `setPassword should save SHA-512 hash`() = runBlocking {
        val password = "password123"
        val expectedHash = SecurityUtil.hashPassword(password, "SHA-512")

        viewModel.setPassword(password)

        verify(preferencesRepository).updatePasswordHash(expectedHash)
        verify(preferencesRepository).updatePasswordEnabled(true)
    }
}
