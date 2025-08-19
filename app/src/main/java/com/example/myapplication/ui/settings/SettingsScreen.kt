package com.example.myapplication.ui.settings

import android.app.Activity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.data.StudentRepository
import com.example.myapplication.ui.dialogs.ChangePasswordDialog
import com.example.myapplication.ui.dialogs.SetPasswordDialog
import com.example.myapplication.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    studentRepository: StudentRepository,
    onNavigateToStudentGroups: () -> Unit,
    onNavigateToConditionalFormatting: () -> Unit,
    onNavigateToExport: () -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var showSetPasswordDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    val restoreComplete by settingsViewModel.restoreComplete.observeAsState(false)

    if (restoreComplete) {
        AlertDialog(
            onDismissRequest = { /* Don't allow dismissal */ },
            title = { Text("Restore Complete") },
            text = { Text("The database has been restored. Please restart the app for the changes to take effect.") },
            confirmButton = {
                TextButton(onClick = {
                    (context as? Activity)?.finish()
                }) {
                    Text("OK")
                }
            }
        )
    }

    if (showSetPasswordDialog) {
        SetPasswordDialog(
            onDismiss = { showSetPasswordDialog = false },
            onSave = { password ->
                settingsViewModel.setPassword(password)
                settingsViewModel.updatePasswordEnabled(true)
            }
        )
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onSave = { oldPassword, newPassword ->
                coroutineScope.launch {
                    if (settingsViewModel.checkPassword(oldPassword)) {
                        settingsViewModel.setPassword(newPassword)
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        val tabs = listOf("General", "Display", "Data", "Advanced")
        val pagerState = rememberPagerState(pageCount = { tabs.size })
        val coroutineScope = rememberCoroutineScope()

        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text("General") }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("Display") }
                )
                Tab(
                    selected = pagerState.currentPage == 2,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(2) } },
                    text = { Text("Data") }
                )
                Tab(
                    selected = pagerState.currentPage == 3,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(3) } },
                    text = { Text("Advanced") }
                )
            }
            HorizontalPager(
                state = pagerState,
            ) { page ->
                when (page) {
                    0 -> GeneralSettingsTab(
                        settingsViewModel = settingsViewModel,
                        onShowSetPasswordDialog = { showSetPasswordDialog = true },
                        onShowChangePasswordDialog = { showChangePasswordDialog = true }
                    )
                    1 -> DisplaySettingsTab(settingsViewModel = settingsViewModel)
                    2 -> DataSettingsTab(settingsViewModel = settingsViewModel, studentRepository = studentRepository)
                    3 -> AdvancedSettingsTab(
                        onNavigateToStudentGroups = onNavigateToStudentGroups,
                        onNavigateToConditionalFormatting = onNavigateToConditionalFormatting,
                        onNavigateToExport = onNavigateToExport
                    )
                }
            }
        }
    }
}
