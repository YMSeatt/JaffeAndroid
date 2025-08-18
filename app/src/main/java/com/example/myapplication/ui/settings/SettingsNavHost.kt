package com.example.myapplication.ui.settings

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.StudentRepository
import com.example.myapplication.viewmodel.ConditionalFormattingRuleViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import com.example.myapplication.viewmodel.StudentGroupsViewModel

@Composable
fun SettingsNavHost(
    settingsViewModel: SettingsViewModel,
    studentRepository: StudentRepository,
    studentGroupsViewModel: StudentGroupsViewModel,
    conditionalFormattingRuleViewModel: ConditionalFormattingRuleViewModel,
    onDismiss: () -> Unit
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "settings") {
        composable("settings") {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                studentRepository = studentRepository,
                onNavigateToStudentGroups = { navController.navigate("student_groups") },
                onNavigateToConditionalFormatting = { navController.navigate("conditional_formatting") },
                onNavigateToExport = { navController.navigate("export") },
                onDismiss = onDismiss
            )
        }
        composable("student_groups") {
            StudentGroupsScreen(
                viewModel = studentGroupsViewModel,
                onDismiss = { navController.popBackStack() }
            )
        }
        composable("conditional_formatting") {
            ConditionalFormattingScreen(
                viewModel = conditionalFormattingRuleViewModel,
                onDismiss = { navController.popBackStack() }
            )
        }
        composable("export") {
            ExportScreen(
                studentRepository = studentRepository,
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}