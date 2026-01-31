package com.example.myapplication.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.GuideType
import com.example.myapplication.preferences.AppTheme
import com.example.myapplication.ui.model.SessionType
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.util.toTitleCase
import com.example.myapplication.viewmodel.GuideViewModel
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatingChartTopAppBar(
    sessionType: SessionType,
    isSessionActive: Boolean,
    selectMode: Boolean,
    selectedStudentIds: Set<Int>,
    behaviorTypeNames: List<String>,
    onSessionTypeChange: (SessionType) -> Unit,
    onToggleSession: () -> Unit,
    onToggleSelectMode: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onBehaviorLog: (String) -> Unit,
    onLogQuiz: () -> Unit,
    onDeleteSelected: () -> Unit,
    onChangeBoxSize: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDataViewer: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onHelpClick: () -> Unit,
    onTakeScreenshot: () -> Unit,
    editModeEnabled: Boolean,
    seatingChartViewModel: SeatingChartViewModel,
    settingsViewModel: SettingsViewModel,
    guideViewModel: GuideViewModel,
    onShowSaveLayout: () -> Unit,
    onShowLoadLayout: () -> Unit,
    onShowExport: () -> Unit,
    onImportJson: () -> Unit,
    onImportFromPythonAssets: (Context) -> Unit,
    onImportStudentsFromExcel: () -> Unit,
    onOpenLastExportFolder: (String) -> Unit,
    onOpenAppDataFolder: () -> Unit,
    onShareDatabase: () -> Unit,
    lastExportPath: String?,
    selectedStudentUiItemForAction: StudentUiItem?
) {
    var showMoreMenu by remember { mutableStateOf(false) }
    var showLayoutSubMenu by remember { mutableStateOf(false) }
    var showGuidesSubMenu by remember { mutableStateOf(false) }
    var showAppearanceSubMenu by remember { mutableStateOf(false) }
    var showAlignSubMenu by remember { mutableStateOf(false) }

    var showSessionModeDropdown by remember { mutableStateOf(false) }
    var showDataAndExportDropdown by remember { mutableStateOf(false) }

    val context = LocalContext.current

    TopAppBar(
        title = {
            Column {
                Text("Seating Chart", style = MaterialTheme.typography.titleMedium)
                Text(sessionType.name.toTitleCase(), style = MaterialTheme.typography.bodySmall)
            }
        },
        actions = {
            // Undo/Redo
            IconButton(onClick = onUndo) { Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo") }
            IconButton(onClick = onRedo) { Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo") }

            // Behaviors Dropdown
            val behaviorTargetCount = if (selectMode) selectedStudentIds.size else if (selectedStudentUiItemForAction != null) 1 else 0
            if (behaviorTargetCount > 0 && behaviorTypeNames.isNotEmpty()) {
                var showQuickBehaviorMenu by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { showQuickBehaviorMenu = true }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Log ($behaviorTargetCount)", modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                    DropdownMenu(
                        expanded = showQuickBehaviorMenu,
                        onDismissRequest = { showQuickBehaviorMenu = false }
                    ) {
                        behaviorTypeNames.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    onBehaviorLog(type)
                                    showQuickBehaviorMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Session Mode Dropdown (moved from overflow)
            Box {
                IconButton(onClick = { showSessionModeDropdown = true }) {
                    Icon(Icons.Default.RadioButtonChecked, contentDescription = "Session Mode")
                }
                DropdownMenu(expanded = showSessionModeDropdown, onDismissRequest = { showSessionModeDropdown = false }) {
                    SessionType.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.name.toTitleCase()) },
                            onClick = {
                                if (isSessionActive) onToggleSession()
                                onSessionTypeChange(mode)
                                showSessionModeDropdown = false
                            },
                            trailingIcon = { if (sessionType == mode) Icon(Icons.Default.RadioButtonChecked, null) else Icon(Icons.Default.RadioButtonUnchecked, null) }
                        )
                    }
                }
            }

            // View Data (moved from overflow)
            IconButton(onClick = onNavigateToDataViewer) { Icon(Icons.Default.Analytics, contentDescription = "View Data") }

            // Reminders (moved from overflow)
            IconButton(onClick = onNavigateToReminders) { Icon(Icons.Default.Notifications, contentDescription = "Reminders") }

            // Data & Export Dropdown (moved from overflow)
            Box {
                IconButton(onClick = { showDataAndExportDropdown = true }) {
                    Icon(Icons.Default.Storage, contentDescription = "Data & Export")
                }
                DropdownMenu(expanded = showDataAndExportDropdown, onDismissRequest = { showDataAndExportDropdown = false }) {
                    DropdownMenuItem(text = { Text("Import from JSON") }, onClick = { onImportJson(); showDataAndExportDropdown = false })
                    DropdownMenuItem(text = { Text("Import from Python Assets") }, onClick = { onImportFromPythonAssets(context); showDataAndExportDropdown = false })
                    DropdownMenuItem(text = { Text("Import from Excel") }, onClick = { onImportStudentsFromExcel(); showDataAndExportDropdown = false })
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Export to Excel") },
                        onClick = { onShowExport(); showDataAndExportDropdown = false },
                        leadingIcon = { Icon(Icons.Default.CloudUpload, null) }
                    )
                    DropdownMenuItem(text = { Text("Open Last Export Folder") }, enabled = lastExportPath?.isNotBlank() == true, onClick = {
                        lastExportPath?.let { path ->
                            onOpenLastExportFolder(path)
                        }
                        showDataAndExportDropdown = false
                    })
                    Divider()
                    DropdownMenuItem(text = { Text("Backup Database (Share)") }, onClick = { onShareDatabase(); showDataAndExportDropdown = false })
                    DropdownMenuItem(text = { Text("Open App Data Folder") }, onClick = { onOpenAppDataFolder(); showDataAndExportDropdown = false })
                }
            }

            // Settings (moved from overflow)
            IconButton(onClick = onNavigateToSettings) { Icon(Icons.Default.Settings, contentDescription = "Settings") }

            // Main Overflow Menu (remaining items)
            Box {
                IconButton(onClick = { showMoreMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }

                DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(if (editModeEnabled) "Disable Edit Mode" else "Enable Edit Mode") },
                        onClick = {
                            settingsViewModel.updateEditModeEnabled(!editModeEnabled)
                            showMoreMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        trailingIcon = {
                            Icon(
                                if (editModeEnabled) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                contentDescription = null
                            )
                        }
                    )
                    Divider()
                    if (editModeEnabled) {
                        DropdownMenuItem(text = { Text("Alignment Tools") }, onClick = { showMoreMenu = false; showAlignSubMenu = true }, leadingIcon = { Icon(Icons.Default.AutoFixHigh, null) })
                    }
                    DropdownMenuItem(text = { Text("Layouts") }, onClick = { showMoreMenu = false; showLayoutSubMenu = true }, leadingIcon = { Icon(Icons.Default.Layers, null) })
                    DropdownMenuItem(text = { Text("Guides & Grid") }, onClick = { showMoreMenu = false; showGuidesSubMenu = true }, leadingIcon = { Icon(Icons.Default.GridView, null) })
                    Divider()
                    DropdownMenuItem(text = { Text("Take Screenshot") }, onClick = { onTakeScreenshot(); showMoreMenu = false }, leadingIcon = { Icon(Icons.Default.PhotoCamera, null) })
                    DropdownMenuItem(text = { Text("Appearance") }, onClick = { showMoreMenu = false; showAppearanceSubMenu = true }, leadingIcon = { Icon(Icons.Default.Palette, null) })
                    Divider()
                    DropdownMenuItem(text = { Text("Help") }, onClick = { onHelpClick(); showMoreMenu = false }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.Help, null) })
                }

                // Submenus that remain in the overflow context
                DropdownMenu(expanded = showAlignSubMenu, onDismissRequest = { showAlignSubMenu = false }) {
                    DropdownMenuItem(text = { Text("Align Top") }, onClick = { seatingChartViewModel.alignSelectedItems("top"); showAlignSubMenu = false })
                    DropdownMenuItem(text = { Text("Align Bottom") }, onClick = { seatingChartViewModel.alignSelectedItems("bottom"); showAlignSubMenu = false })
                    DropdownMenuItem(text = { Text("Align Left") }, onClick = { seatingChartViewModel.alignSelectedItems("left"); showAlignSubMenu = false })
                    DropdownMenuItem(text = { Text("Align Right") }, onClick = { seatingChartViewModel.alignSelectedItems("right"); showAlignSubMenu = false })
                    Divider()
                    DropdownMenuItem(text = { Text("Distribute Horizontal") }, onClick = { seatingChartViewModel.distributeSelectedItems("horizontal"); showAlignSubMenu = false })
                    DropdownMenuItem(text = { Text("Distribute Vertical") }, onClick = { seatingChartViewModel.distributeSelectedItems("vertical"); showAlignSubMenu = false })
                }
                DropdownMenu(expanded = showLayoutSubMenu, onDismissRequest = { showLayoutSubMenu = false }) {
                    DropdownMenuItem(text = { Text("Save Current Layout") }, onClick = { onShowSaveLayout(); showLayoutSubMenu = false }, leadingIcon = { Icon(Icons.Default.Add, null) })
                    DropdownMenuItem(text = { Text("Load Saved Layout") }, onClick = { onShowLoadLayout(); showLayoutSubMenu = false }, leadingIcon = { Icon(Icons.Default.CloudDownload, null) })
                }
                DropdownMenu(expanded = showGuidesSubMenu, onDismissRequest = { showGuidesSubMenu = false }) {
                    DropdownMenuItem(text = { Text("Add Vertical Guide") }, onClick = { guideViewModel.addGuide(GuideType.VERTICAL); showGuidesSubMenu = false })
                    DropdownMenuItem(text = { Text("Add Horizontal Guide") }, onClick = { guideViewModel.addGuide(GuideType.HORIZONTAL); showGuidesSubMenu = false })
                    DropdownMenuItem(text = { Text("Clear All Guides") }, onClick = { guideViewModel.guides.value.forEach { guideViewModel.deleteGuide(it) }; showGuidesSubMenu = false })
                }
                DropdownMenu(expanded = showAppearanceSubMenu, onDismissRequest = { showAppearanceSubMenu = false }) {
                    AppTheme.entries.forEach { theme ->
                        DropdownMenuItem(text = { Text(theme.name.toTitleCase()) }, onClick = { settingsViewModel.updateAppTheme(theme); showAppearanceSubMenu = false }, trailingIcon = {
                            val currentTheme by settingsViewModel.appTheme.collectAsState()
                            if (currentTheme == theme) Icon(Icons.Default.RadioButtonChecked, null)
                        })
                    }
                }
            }
        }
    )
}
