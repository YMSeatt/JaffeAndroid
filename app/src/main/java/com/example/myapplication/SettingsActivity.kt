package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.ui.settings.SettingsNavHost
import com.example.myapplication.viewmodel.ConditionalFormattingRuleViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import com.example.myapplication.viewmodel.StudentGroupsViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme

class SettingsActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    private val studentGroupsViewModelFactory by lazy {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(StudentGroupsViewModel::class.java)) {
                    val studentGroupDao = AppDatabase.getDatabase(applicationContext).studentGroupDao()
                    @Suppress("UNCHECKED_CAST")
                    return StudentGroupsViewModel(studentGroupDao) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private val conditionalFormattingRuleViewModelFactory by lazy {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ConditionalFormattingRuleViewModel::class.java)) {
                    val conditionalFormattingRuleDao = AppDatabase.getDatabase(applicationContext).conditionalFormattingRuleDao()
                    @Suppress("UNCHECKED_CAST")
                    return ConditionalFormattingRuleViewModel(conditionalFormattingRuleDao) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                SettingsNavHost()
            }
        }
    }
}