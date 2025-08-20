package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.StudentRepository
import com.example.myapplication.ui.settings.SettingsNavHost
import com.example.myapplication.viewmodel.ConditionalFormattingRuleViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.myapplication.preferences.AppTheme
import com.example.myapplication.viewmodel.StudentGroupsViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme

class SettingsActivity : ComponentActivity() {

    private val db by lazy { AppDatabase.getDatabase(applicationContext) }
    private val studentRepository by lazy {
        StudentRepository(
            studentDao = db.studentDao(),
            behaviorEventDao = db.behaviorEventDao(),
            homeworkLogDao = db.homeworkLogDao(),
            furnitureDao = db.furnitureDao(),
            quizLogDao = db.quizLogDao(),
            studentGroupDao = db.studentGroupDao(),
            layoutTemplateDao = db.layoutTemplateDao(),
            conditionalFormattingRuleDao = db.conditionalFormattingRuleDao(),
            quizMarkTypeDao = db.quizMarkTypeDao()
        )
    }

    private val settingsViewModel: SettingsViewModel by viewModels()

    private val studentGroupsViewModel: StudentGroupsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(StudentGroupsViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return StudentGroupsViewModel(db.studentGroupDao()) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private val conditionalFormattingRuleViewModel: ConditionalFormattingRuleViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ConditionalFormattingRuleViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ConditionalFormattingRuleViewModel(db.conditionalFormattingRuleDao()) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val appTheme by settingsViewModel.appTheme.collectAsState()
            val noAnimations by settingsViewModel.noAnimations.collectAsState()

            MyApplicationTheme(
                darkTheme = when (appTheme) {
                    AppTheme.LIGHT -> false
                    AppTheme.DARK -> true
                    AppTheme.SYSTEM -> isSystemInDarkTheme()
                    AppTheme.DYNAMIC -> isSystemInDarkTheme()
                },
                dynamicColor = appTheme == AppTheme.DYNAMIC,
                disableAnimations = noAnimations
            ) {
                SettingsNavHost(
                    settingsViewModel = settingsViewModel,
                    studentRepository = studentRepository,
                    studentGroupsViewModel = studentGroupsViewModel,
                    conditionalFormattingRuleViewModel = conditionalFormattingRuleViewModel,
                    onDismiss = { finish() }
                )
            }
        }
    }
}