package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.example.myapplication.data.importer.JsonImporter
import com.example.myapplication.preferences.AppPreferencesRepository

class SettingsViewModelFactory(
    private val application: Application,
    private val workManager: WorkManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(application, workManager, AppPreferencesRepository(application)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
