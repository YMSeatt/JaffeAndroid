package com.example.myapplication.ui.components

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.myapplication.MainActivity
import com.example.myapplication.SessionType
import com.example.myapplication.data.GuideType
import com.example.myapplication.preferences.AppTheme
import com.example.myapplication.util.captureComposable
import com.example.myapplication.viewmodel.GuideViewModel
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarActions(
    seatingChartViewModel: SeatingChartViewModel,
    settingsViewModel: SettingsViewModel,
    guideViewModel: GuideViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToDataViewer: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onHelpClick: () -> Unit,
    selectMode: Boolean,
    onSelectModeChange: (Boolean) -> Unit,
    showSaveLayoutDialog: Boolean,
    onShowSaveLayoutDialogChange: (Boolean) -> Unit,
    showLoadLayoutDialog: Boolean,
    onShowLoadLayoutDialogChange: (Boolean) -> Unit,
    showExportDialog: Boolean,
    onShowExportDialogChange: (Boolean) -> Unit,
    showChangeBoxSizeDialog: Boolean,
    onShowChangeBoxSizeDialogChange: (Boolean) -> Unit,
    sessionType: SessionType,
    onSessionTypeChange: (SessionType) -> Unit,
    showBehaviorDialog: Boolean,
    onShowBehaviorDialogChange: (Boolean) -> Unit,
    showLogQuizScoreDialog: Boolean,
    onShowLogQuizScoreDialogChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val selectedStudentIds by seatingChartViewModel.selectedStudentIds.observeAsState(initial = emptySet())
    val editModeEnabled by settingsViewModel.editModeEnabled.collectAsState(initial = false)
    val isSessionActive by seatingChartViewModel.isSessionActive.observeAsState(initial = false)
    val lastExportPath by settingsViewModel.lastExportPath.collectAsState(initial = null)
    val students by seatingChartViewModel.studentsForDisplay.observeAsState(initial = emptyList())


    TextButton(
        onClick = {
            onSelectModeChange(!selectMode)
            if (!selectMode) {
                seatingChartViewModel.clearSelection()
            }
        },
        modifier = Modifier.width(100.dp)
    ) {
        Text(
            if (selectMode) "Exit" else "Select",
            maxLines = 1
        )
    }

    IconButton(onClick = { seatingChartViewModel.undo() }) { Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo") }
    IconButton(onClick = { seatingChartViewModel.redo() }) { Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo") }

    var showMoreMenu by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { showMoreMenu = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = showMoreMenu,
            onDismissRequest = { showMoreMenu = false }
        ) {
            if (editModeEnabled) {
                DropdownMenuItem(onClick = { seatingChartViewModel.alignSelectedItems("top") }, text = { Text("Align Top") })
                DropdownMenuItem(onClick = { seatingChartViewModel.alignSelectedItems("bottom") }, text = { Text("Align Bottom") })
                DropdownMenuItem(onClick = { seatingChartViewModel.alignSelectedItems("left") }, text = { Text("Align Left") })
                DropdownMenuItem(onClick = { seatingChartViewModel.alignSelectedItems("right") }, text = { Text("Align Right") })
                DropdownMenuItem(onClick = { seatingChartViewModel.distributeSelectedItems("horizontal") }, text = { Text("Distribute H") })
                DropdownMenuItem(onClick = { seatingChartViewModel.distributeSelectedItems("vertical") }, text = { Text("Distribute V") })
            }
            if (selectMode && selectedStudentIds.isNotEmpty()) {
                DropdownMenuItem(text = { Text("Log Behavior") }, onClick = { onShowBehaviorDialogChange(true); showMoreMenu = false })
                DropdownMenuItem(text = { Text("Log Quiz") }, onClick = { onShowLogQuizScoreDialogChange(true); showMoreMenu = false })
                DropdownMenuItem(text = { Text("Delete") }, onClick = { seatingChartViewModel.deleteStudents(selectedStudentIds); showMoreMenu = false })
                DropdownMenuItem(text = { Text("Change Box Size") }, onClick = { onShowChangeBoxSizeDialogChange(true); showMoreMenu = false })
            }

            DropdownMenuItem(text = { Text("Save Layout") }, onClick = { onShowSaveLayoutDialogChange(true); showMoreMenu = false })
            DropdownMenuItem(text = { Text("Import from JSON") }, onClick = { (context as? MainActivity)?.importJsonLauncher?.launch("application/json"); showMoreMenu = false })
            DropdownMenuItem(text = { Text("Import from Python") }, onClick = { seatingChartViewModel.importFromPythonAssets(context); showMoreMenu = false })
            DropdownMenuItem(text = { Text("Load Layout") }, onClick = { onShowLoadLayoutDialogChange(true); showMoreMenu = false })
            DropdownMenuItem(text = { Text("Import Students from Excel") }, onClick = { (context as? MainActivity)?.importStudentsLauncher?.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); showMoreMenu = false })
            DropdownMenuItem(text = { Text("Export to Excel") }, onClick = {
                if (students.isNotEmpty()) {
                    onShowExportDialogChange(true)
                } else {
                    Toast.makeText(context, "No student data to export", Toast.LENGTH_SHORT).show()
                }
                showMoreMenu = false
            })
            DropdownMenuItem(text = { Text("Open Last Export Folder") }, enabled = lastExportPath?.isNotBlank() == true, onClick = {
                lastExportPath?.let { path ->
                    val uri = path.toUri()
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(uri, "vnd.android.document/directory")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "Could not open folder", Toast.LENGTH_SHORT).show()
                    }
                }
                showMoreMenu = false
            })
            DropdownMenuItem(text = { Text("View Data") }, onClick = { onNavigateToDataViewer(); showMoreMenu = false })
            DropdownMenuItem(text = { Text("Reminders") }, onClick = { onNavigateToReminders(); showMoreMenu = false })
            DropdownMenuItem(text = { Text("Export Database") }, onClick = {
                coroutineScope.launch {
                    val uri = settingsViewModel.shareDatabase()
                    if (uri != null) {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/octet-stream"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Database"))
                    } else {
                        Toast.makeText(context, "Could not export database", Toast.LENGTH_SHORT).show()
                    }
                }
                showMoreMenu = false
            })
            DropdownMenuItem(text = { Text("Open Data Folder") }, onClick = { (context as? MainActivity)?.exportDataFolderLauncher?.launch(null); showMoreMenu = false })
            DropdownMenuItem(text = { Text("Add Vertical Guide") }, onClick = { guideViewModel.addGuide(GuideType.VERTICAL); showMoreMenu = false })
            DropdownMenuItem(text = { Text("Add Horizontal Guide") }, onClick = { guideViewModel.addGuide(GuideType.HORIZONTAL); showMoreMenu = false })
            DropdownMenuItem(text = { Text("Clear Guides") }, onClick = {
                guideViewModel.guides.value.forEach { guideViewModel.deleteGuide(it) }
                showMoreMenu = false
            })
            DropdownMenuItem(text = { Text("Take Screenshot") }, onClick = {
                coroutineScope.launch {
                    val view = (context as Activity).window.decorView
                    val bitmap = captureComposable(view, context.window)
                    if (bitmap != null) {
                        settingsViewModel.saveScreenshot(bitmap)
                        Toast.makeText(context, "Screenshot saved", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to capture screenshot", Toast.LENGTH_SHORT).show()
                    }
                }
                showMoreMenu = false
            })
            AppTheme.entries.forEach { theme ->
                DropdownMenuItem(text = { Text(theme.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }) }, onClick = { settingsViewModel.updateAppTheme(theme); showMoreMenu = false })
            }

            SessionType.entries.forEach { mode ->
                DropdownMenuItem(text = { Text(mode.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }) }, onClick = {
                    if (isSessionActive) {
                        seatingChartViewModel.endSession()
                    }
                    onSessionTypeChange(mode)
                    showMoreMenu = false
                })
            }

            if (sessionType == SessionType.QUIZ || sessionType == SessionType.HOMEWORK) {
                DropdownMenuItem(onClick = {
                    if (isSessionActive) seatingChartViewModel.endSession() else seatingChartViewModel.startSession()
                }, text = { Text(if (isSessionActive) "End Session" else "Start Session") })
            }
        }
    }

    IconButton(onClick = onNavigateToSettings) { Icon(Icons.Filled.Settings, contentDescription = "Settings") }
    IconButton(onClick = onHelpClick) { Icon(Icons.AutoMirrored.Filled.Help, contentDescription = "Help") }
}
