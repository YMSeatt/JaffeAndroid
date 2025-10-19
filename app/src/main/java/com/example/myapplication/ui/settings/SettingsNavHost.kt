package com.example.myapplication.ui.settings

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    val noAnimations by settingsViewModel.noAnimations.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "settings",
        enterTransition = { if (noAnimations) EnterTransition.None else fadeIn() },
        exitTransition = { if (noAnimations) ExitTransition.None else fadeOut() }
    ) {
        composable("settings") {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                studentRepository = studentRepository,
                onNavigateToStudentGroups = { navController.navigate("student_groups") },
                onNavigateToConditionalFormatting = { navController.navigate("conditional_formatting") },
                onNavigateToExport = { navController.navigate("export") },
                onNavigateToEmailSchedules = { navController.navigate("email_schedules") },
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
        composable("email_schedules") {
            EmailSchedulesScreen(
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}
