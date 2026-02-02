package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.preferences.AppTheme
import com.example.myapplication.ui.DataViewerScreen
import com.example.myapplication.ui.PasswordScreen
import com.example.myapplication.ui.model.SessionType
import com.example.myapplication.ui.screens.RemindersScreen
import com.example.myapplication.ui.screens.SeatingChartScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.GuideViewModel
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import com.example.myapplication.viewmodel.StatsViewModel
import com.example.myapplication.viewmodel.StudentGroupsViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.content.Context
import androidx.activity.result.ActivityResultLauncher

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val seatingChartViewModel: SeatingChartViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val guideViewModel: GuideViewModel by viewModels()
    private val studentGroupsViewModel: StudentGroupsViewModel by viewModels()
    private val statsViewModel: StatsViewModel by viewModels()

    val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
        uri?.let {
            seatingChartViewModel.pendingExportOptions?.let { options ->
                lifecycleScope.launch {
                    val result = seatingChartViewModel.exportData(
                        context = this@MainActivity,
                        uri = it,
                        options = options
                    )
                    if (result.isSuccess) {
                        Toast.makeText(this@MainActivity, "Data exported successfully!", Toast.LENGTH_LONG).show()
                        settingsViewModel.updateLastExportPath(it.toString())
                    } else {
                        Toast.makeText(this@MainActivity, "Export failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        seatingChartViewModel.pendingExportOptions = null
    }

    var showEmailDialog by mutableStateOf(false)
    var emailUri by mutableStateOf<Uri?>(null)

    val importJsonLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch {
                seatingChartViewModel.importData(this@MainActivity, it)
                Toast.makeText(this@MainActivity, "Data imported successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importStudentsLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch {
                val result = seatingChartViewModel.importStudentsFromExcel(this@MainActivity, it)
                result.onSuccess { count ->
                    Toast.makeText(this@MainActivity, "$count students imported successfully", Toast.LENGTH_SHORT).show()
                }.onFailure { error ->
                    Toast.makeText(this@MainActivity, "Error importing students: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val exportDataFolderLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch {
                settingsViewModel.exportDataFolder(it)
                Toast.makeText(this@MainActivity, "Data exported successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val currentAppThemeState by settingsViewModel.appTheme.collectAsState()
            val passwordEnabled by settingsViewModel.passwordEnabled.collectAsState()
            var unlocked by remember { mutableStateOf(!passwordEnabled) }
            var showDataViewer by remember { mutableStateOf(false) }
            var showReminders by remember { mutableStateOf(false) }

            val noAnimations by settingsViewModel.noAnimations.collectAsState()
            val useBoldFont by settingsViewModel.useBoldFont.collectAsState()

            MyApplicationTheme(
                darkTheme = when (currentAppThemeState) {
                    AppTheme.LIGHT -> false
                    AppTheme.DARK -> true
                    AppTheme.SYSTEM -> isSystemInDarkTheme()
                    AppTheme.DYNAMIC -> isSystemInDarkTheme()
                },
                dynamicColor = currentAppThemeState == AppTheme.DYNAMIC,
                disableAnimations = noAnimations,
                useBoldFont = useBoldFont
            ) {
                if (unlocked) {
                    if (showDataViewer) {
                        DataViewerScreen(
                            seatingChartViewModel = seatingChartViewModel,
                            statsViewModel = statsViewModel,
                            settingsViewModel = settingsViewModel,
                            onDismiss = { showDataViewer = false }
                        )
                        BackHandler {
                            showDataViewer = false
                        }
                    } else if (showReminders) {
                        RemindersScreen(
                            viewModel = hiltViewModel(),
                            onDismiss = { showReminders = false }
                        )
                        BackHandler {
                            showReminders = false
                        }
                    } else {
                        var showEmailDialogState by remember { mutableStateOf(false) }
                        SeatingChartScreen(
                            seatingChartViewModel = seatingChartViewModel,
                            settingsViewModel = settingsViewModel,
                            studentGroupsViewModel = studentGroupsViewModel,
                            guideViewModel = guideViewModel,
                            onNavigateToSettings = {
                                startActivity(Intent(this, SettingsActivity::class.java))
                            },
                            onNavigateToDataViewer = { showDataViewer = true },
                            onNavigateToReminders = { showReminders = true },
                            onHelpClick = {
                                startActivity(Intent(this, HelpActivity::class.java))
                            },
                            onImportJson = { importJsonLauncher.launch("application/json") },
                            onImportStudentsFromExcel = { importStudentsLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") },
                            onOpenAppDataFolder = { exportDataFolderLauncher.launch(null) },
                            createDocumentLauncher = createDocumentLauncher,
                            onShowEmailDialogChange = { showEmailDialogState = it },
                            showEmailDialog = showEmailDialogState,
                        )
                    }
                } else {
                    PasswordScreen(settingsViewModel = settingsViewModel) {
                        unlocked = true
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        seatingChartViewModel.handleOnStop(this)
    }
}
