package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.StudentRepository
import com.example.myapplication.data.importer.JsonImporter
import com.example.myapplication.preferences.AppTheme
import com.example.myapplication.ui.settings.SettingsNavHost
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.ConditionalFormattingRuleViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import com.example.myapplication.viewmodel.StudentGroupsViewModel

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    @Inject lateinit var studentRepository: StudentRepository
    @Inject lateinit var jsonImporter: JsonImporter

    private val settingsViewModel: SettingsViewModel by viewModels()
    private val studentGroupsViewModel: StudentGroupsViewModel by viewModels()
    private val conditionalFormattingRuleViewModel: ConditionalFormattingRuleViewModel by viewModels()

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
