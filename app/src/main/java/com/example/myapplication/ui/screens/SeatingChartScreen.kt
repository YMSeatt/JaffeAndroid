package com.example.myapplication.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Chair
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.MainActivity
import com.example.myapplication.labs.ghost.GhostConfig
import com.example.myapplication.labs.ghost.GhostInsight
import com.example.myapplication.labs.ghost.GhostInsightDialog
import com.example.myapplication.labs.ghost.GhostInsightEngine
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.GuideType
import com.example.myapplication.preferences.AppTheme
import com.example.myapplication.ui.components.FurnitureDraggableIcon
import com.example.myapplication.ui.components.GridAndRulers
import com.example.myapplication.ui.components.StudentDraggableIcon
import com.example.myapplication.ui.dialogs.AddEditFurnitureDialog
import com.example.myapplication.ui.dialogs.AddEditStudentDialog
import com.example.myapplication.ui.dialogs.AdvancedHomeworkLogDialog
import com.example.myapplication.ui.dialogs.AssignTaskDialog
import com.example.myapplication.ui.dialogs.BehaviorDialog
import com.example.myapplication.ui.dialogs.BehaviorLogViewerDialog
import com.example.myapplication.ui.dialogs.ChangeBoxSizeDialog
import com.example.myapplication.ui.dialogs.EmailDialog
import com.example.myapplication.ui.dialogs.ExportDialog
import com.example.myapplication.ui.dialogs.LiveHomeworkMarkDialog
import com.example.myapplication.ui.dialogs.LiveQuizMarkDialog
import com.example.myapplication.ui.dialogs.LoadLayoutDialog
import com.example.myapplication.ui.dialogs.LogQuizScoreDialog
import com.example.myapplication.ui.dialogs.SaveLayoutDialog
import com.example.myapplication.ui.dialogs.StudentStyleScreen
import com.example.myapplication.ui.dialogs.UndoHistoryDialog
import com.example.myapplication.ui.model.SessionType
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.util.EmailException
import com.example.myapplication.util.EmailUtil
import com.example.myapplication.util.captureComposable
import com.example.myapplication.util.toTitleCase
import com.example.myapplication.viewmodel.GuideViewModel
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import com.example.myapplication.viewmodel.StudentGroupsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SeatingChartScreen(
    seatingChartViewModel: SeatingChartViewModel,
    settingsViewModel: SettingsViewModel,
    studentGroupsViewModel: StudentGroupsViewModel,
    guideViewModel: GuideViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToDataViewer: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onHelpClick: () -> Unit,
    onImportJson: () -> Unit,
    onImportStudentsFromExcel: () -> Unit,
    onOpenAppDataFolder: () -> Unit,
    createDocumentLauncher: ActivityResultLauncher<String>,
    showEmailDialog: Boolean,
    onShowEmailDialogChange: (Boolean) -> Unit
) {
    val students by seatingChartViewModel.studentsForDisplay.observeAsState(initial = emptyList())
    val furniture by seatingChartViewModel.furnitureForDisplay.observeAsState(initial = emptyList())
    val layouts by seatingChartViewModel.allLayoutTemplates.observeAsState(initial = emptyList())
    val selectedStudentIds by seatingChartViewModel.selectedStudentIds.observeAsState(initial = emptySet())
    val allBehaviorEvents by seatingChartViewModel.allBehaviorEvents.observeAsState(initial = emptyList())
    val allQuizLogs by seatingChartViewModel.allQuizLogs.observeAsState(initial = emptyList())
    val allHomeworkLogs by seatingChartViewModel.allHomeworkLogs.observeAsState(initial = emptyList())

    var showGhostInsightDialog by remember { mutableStateOf(false) }
    var currentGhostInsight by remember { mutableStateOf<GhostInsight?>(null) }

    var showBehaviorDialog by remember { mutableStateOf(false) }
    var showLogQuizScoreDialog by remember { mutableStateOf(false) }
    var showLiveQuizMarkDialog by remember { mutableStateOf(false) }
    var showAdvancedHomeworkLogDialog by remember { mutableStateOf(false) }
    var showLiveHomeworkMarkDialog by remember { mutableStateOf(false) }
    var showStudentActionMenu by remember { mutableStateOf(false) }
    var showSaveLayoutDialog by remember { mutableStateOf(false) }
    var showLoadLayoutDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showUndoHistoryDialog by remember { mutableStateOf(false) }

    // Submenu States
    var showAlignSubMenu by remember { mutableStateOf(false) }

    var selectMode by remember { mutableStateOf(false) }
    var showChangeBoxSizeDialog by remember { mutableStateOf(false) }
    var showStudentStyleDialog by remember { mutableStateOf(false) }
    var showAssignTaskDialog by remember { mutableStateOf(false) }

    var selectedStudentUiItemForAction by remember { mutableStateOf<StudentUiItem?>(null) }

    var showAddEditStudentDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<com.example.myapplication.data.Student?>(null) }
    var showAddEditFurnitureDialog by remember { mutableStateOf(false) }
    var editingFurniture by remember { mutableStateOf<com.example.myapplication.data.Furniture?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val behaviorTypes by settingsViewModel.customBehaviors.observeAsState(initial = emptyList())
    val behaviorTypeNames = remember(behaviorTypes) { behaviorTypes.map { it.name } }
    val showRecentBehavior by settingsViewModel.showRecentBehavior.collectAsState(initial = false)
    var sessionType by remember { mutableStateOf(SessionType.BEHAVIOR) }
    val editModeEnabled by settingsViewModel.editModeEnabled.collectAsState(initial = false)
    var longPressPosition by remember { mutableStateOf(Offset.Zero) }

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(canvasSize.height) {
        seatingChartViewModel.canvasHeight = canvasSize.height
    }

    var isFabMenuOpen by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val isSessionActive by seatingChartViewModel.isSessionActive.observeAsState(initial = false)
    val lastExportPath by settingsViewModel.lastExportPath.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            SeatingChartTopAppBar(
                sessionType = sessionType,
                isSessionActive = isSessionActive,
                selectMode = selectMode,
                selectedStudentIds = selectedStudentIds,
                behaviorTypeNames = behaviorTypeNames,
                onSessionTypeChange = { sessionType = it },
                onToggleSession = { if (isSessionActive) seatingChartViewModel.endSession() else seatingChartViewModel.startSession() },
                onToggleSelectMode = {
                    selectMode = !selectMode
                    if (!selectMode) seatingChartViewModel.clearSelection()
                },
                onUndo = { seatingChartViewModel.undo() },
                onRedo = { seatingChartViewModel.redo() },
                onShowUndoHistory = { showUndoHistoryDialog = true },
                onBehaviorLog = { type ->
                    val targets = if (selectMode) selectedStudentIds.map { it.toLong() } else listOfNotNull(selectedStudentUiItemForAction?.id?.toLong())
                    targets.forEach { id ->
                        seatingChartViewModel.addBehaviorEvent(BehaviorEvent(studentId = id, type = type, timestamp = System.currentTimeMillis(), comment = null))
                    }
                    if (!selectMode) selectedStudentUiItemForAction = null
                    coroutineScope.launch { snackbarHostState.showSnackbar("Logged $type for ${targets.size} student(s)") }
                },
                onLogQuiz = { showLogQuizScoreDialog = true },
                onDeleteSelected = { seatingChartViewModel.deleteStudents(selectedStudentIds) },
                onChangeBoxSize = { showChangeBoxSizeDialog = true },
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToDataViewer = onNavigateToDataViewer,
                onNavigateToReminders = onNavigateToReminders,
                onHelpClick = onHelpClick,
                onTakeScreenshot = {
                    coroutineScope.launch {
                        val view = (context as Activity).window.decorView
                        val bitmap = captureComposable(view, (context as Activity).window)
                        if (bitmap != null) {
                            settingsViewModel.saveScreenshot(bitmap)
                            Toast.makeText(context, "Screenshot saved", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                editModeEnabled = editModeEnabled,
                seatingChartViewModel = seatingChartViewModel,
                settingsViewModel = settingsViewModel,
                guideViewModel = guideViewModel,
                onShowSaveLayout = { showSaveLayoutDialog = true },
                onShowLoadLayout = { showLoadLayoutDialog = true },
                onShowExport = { showExportDialog = true },
                onImportJson = onImportJson,
                onImportFromPythonAssets = { seatingChartViewModel.importFromPythonAssets(context) },
                onImportStudentsFromExcel = onImportStudentsFromExcel,
                onOpenLastExportFolder = { path ->
                    lastExportPath?.let { uriPath ->
                        val uri = uriPath.toUri()
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "vnd.android.document/directory")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent)
                    }
                },
                onOpenAppDataFolder = onOpenAppDataFolder,
                onShareDatabase = {
                    coroutineScope.launch {
                        settingsViewModel.shareDatabase()?.let { uri ->
                            val intent = Intent(Intent.ACTION_SEND).apply { type = "application/octet-stream"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                            context.startActivity(Intent.createChooser(intent, "Share Database"))
                        }
                    }
                },
                lastExportPath = lastExportPath,
                selectedStudentUiItemForAction = selectedStudentUiItemForAction
            )
        },
        floatingActionButton = {
            if (editModeEnabled) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AnimatedVisibility(visible = isFabMenuOpen) {
                        FloatingActionButton(onClick = { editingStudent = null; showAddEditStudentDialog = true; isFabMenuOpen = false }) {
                            Icon(Icons.Default.Person, contentDescription = "Add Student")
                        }
                    }
                    AnimatedVisibility(visible = isFabMenuOpen) {
                        FloatingActionButton(onClick = { editingFurniture = null; showAddEditFurnitureDialog = true; isFabMenuOpen = false }) {
                            Icon(Icons.Default.Chair, contentDescription = "Add Furniture")
                        }
                    }
                    FloatingActionButton(onClick = { isFabMenuOpen = !isFabMenuOpen }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .onSizeChanged { canvasSize = it }

        ) {
            GridAndRulers(
                settingsViewModel = settingsViewModel,
                guideViewModel = guideViewModel,
                scale = scale,
                offset = offset,
                canvasSize = androidx.compose.ui.geometry.Size(canvasSize.width.toFloat(), canvasSize.height.toFloat())
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            val oldScale = scale
                            val newScale = (scale * zoom).coerceIn(0.5f, 5f)
                            offset = (offset - centroid) * (newScale / oldScale) + centroid + pan
                            scale = newScale
                        }
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            ) {
                Box(modifier = Modifier.size(4000.dp)) {
                    students.forEach { studentItem ->
                        val noAnimations by settingsViewModel.noAnimations.collectAsState()
                        StudentDraggableIcon(
                            studentUiItem = studentItem,
                            viewModel = seatingChartViewModel,
                            settingsViewModel = settingsViewModel,
                            showBehavior = showRecentBehavior,
                            isSelected = selectedStudentIds.contains(studentItem.id),
                            onClick = {
                                if (selectMode) {
                                    val currentSelected = selectedStudentIds.toMutableSet()
                                    if (currentSelected.contains(studentItem.id)) {
                                        currentSelected.remove(studentItem.id)
                                    } else {
                                        currentSelected.add(studentItem.id)
                                    }
                                    seatingChartViewModel.selectedStudentIds.value = currentSelected
                                } else {
                                    selectedStudentUiItemForAction = studentItem
                                    when (sessionType) {
                                        SessionType.BEHAVIOR -> showBehaviorDialog = true
                                        SessionType.QUIZ -> if (seatingChartViewModel.isSessionActive.value == true) showLiveQuizMarkDialog = true else showLogQuizScoreDialog = true
                                        SessionType.HOMEWORK -> if (seatingChartViewModel.isSessionActive.value == true) showLiveHomeworkMarkDialog = true else showAdvancedHomeworkLogDialog = true
                                    }
                                 }
                            },
                            onLongClick = {
                                selectedStudentUiItemForAction = studentItem
                                showStudentActionMenu = true
                            },
                            onResize = { width, height -> seatingChartViewModel.changeBoxSize(setOf(studentItem.id), width.toInt(), height.toInt()) },
                            noAnimations = noAnimations,
                            canvasSize = canvasSize,
                            canvasScale = scale,
                            canvasOffset = offset
                        )
                    }
                    furniture.forEach { furnitureItem ->
                        val noAnimations by settingsViewModel.noAnimations.collectAsState()
                        FurnitureDraggableIcon(
                            furnitureUiItem = furnitureItem,
                            viewModel = seatingChartViewModel,
                            settingsViewModel = settingsViewModel,
                            scale = scale,
                            canvasOffset = offset,
                            onLongClick = {
                                coroutineScope.launch {
                                    editingFurniture = seatingChartViewModel.getFurnitureById(furnitureItem.id)
                                    showAddEditFurnitureDialog = true
                                }
                            },
                            onResize = { width, height -> seatingChartViewModel.changeFurnitureSize(furnitureItem.id, width.toInt(), height.toInt()) },
                            noAnimations = noAnimations
                        )
                    }
                }
            }

            if (showSaveLayoutDialog) {
                SaveLayoutDialog(onDismiss = { showSaveLayoutDialog = false }, onSave = { name -> seatingChartViewModel.saveLayout(name); showSaveLayoutDialog = false })
            }

            if (showLoadLayoutDialog) {
                LoadLayoutDialog(layouts = layouts, onDismiss = { showLoadLayoutDialog = false }, onLoad = { layout -> seatingChartViewModel.loadLayout(layout); showLoadLayoutDialog = false }, onDelete = { layout -> seatingChartViewModel.deleteLayoutTemplate(layout) })
            }

            if (showStudentActionMenu) {
                selectedStudentUiItemForAction?.let { student ->
                    val groups by studentGroupsViewModel.allStudentGroups.collectAsState(initial = emptyList())
                    var showGroupMenu by remember { mutableStateOf(false) }
                    var showBehaviorLogViewer by remember { mutableStateOf(false) }

                    DropdownMenu(
                        expanded = true,
                        onDismissRequest = { showStudentActionMenu = false },
                        offset = DpOffset(longPressPosition.x.dp, longPressPosition.y.dp)
                    ) {
                        if (GhostConfig.GHOST_MODE_ENABLED) {
                            DropdownMenuItem(
                                text = { Text("Neural Insight 👻") },
                                onClick = {
                                    val behavior = allBehaviorEvents.filter { it.studentId == student.id.toLong() }
                                    val quiz = allQuizLogs.filter { it.studentId == student.id.toLong() }
                                    val homework = allHomeworkLogs.filter { it.studentId == student.id.toLong() }

                                    currentGhostInsight = GhostInsightEngine.generateInsight(
                                        student.fullName, behavior, quiz, homework
                                    )
                                    showGhostInsightDialog = true
                                    showStudentActionMenu = false
                                }
                            )
                        }
                        DropdownMenuItem(text = { Text("Edit Student") }, onClick = {
                            coroutineScope.launch {
                                editingStudent =
                                    seatingChartViewModel.getStudentForEditing(student.id.toLong())
                                showAddEditStudentDialog = true
                            }
                            showStudentActionMenu = false
                        })
                        DropdownMenuItem(
                            text = { Text("Delete Student") },
                            onClick = {
                                seatingChartViewModel.deleteStudents(setOf(student.id))
                                showStudentActionMenu = false
                            })
                        DropdownMenuItem(
                            text = { Text("Log Behavior") },
                            onClick = {
                                showBehaviorDialog = true
                                showStudentActionMenu = false
                            })
                        DropdownMenuItem(
                            text = { Text("View Behavior Log") },
                            onClick = {
                                showBehaviorLogViewer = true
                                showStudentActionMenu = false
                            })
                        DropdownMenuItem(
                            text = { Text("Log Homework") },
                            onClick = {
                                showAdvancedHomeworkLogDialog = true
                                showStudentActionMenu = false
                            })
                        DropdownMenuItem(
                            text = { Text("Log Quiz Score") },
                            onClick = {
                                showLogQuizScoreDialog = true
                                showStudentActionMenu = false
                            })
                        DropdownMenuItem(
                            text = { Text("Assign Task") },
                            onClick = {
                                showAssignTaskDialog = true
                                showStudentActionMenu = false
                            })
                        DropdownMenuItem(
                            text = { Text("Change Student Box Style") },
                            onClick = {
                                showStudentStyleDialog = true
                                showStudentActionMenu = false
                            })
                        DropdownMenuItem(
                            text = { Text("Clear Recent Logs") },
                            onClick = {
                                seatingChartViewModel.clearRecentLogsForStudent(student.id.toLong())
                                showStudentActionMenu = false
                            })
                        DropdownMenuItem(
                            text = { Text("Show Recent Logs") },
                            onClick = {
                                seatingChartViewModel.showRecentLogsForStudent(student.id.toLong())
                                showStudentActionMenu = false
                            })
                        DropdownMenuItem(
                            text = { Text("Assign to Group") },
                            onClick = { showGroupMenu = true })
                        if (student.groupId != null) {
                            DropdownMenuItem(text = { Text("Remove from Group") }, onClick = {
                                seatingChartViewModel.removeStudentFromGroup(student.id.toLong())
                                showStudentActionMenu = false
                            })
                        }
                    }
                    if (showBehaviorLogViewer) {
                        BehaviorLogViewerDialog(
                            studentId = student.id.toLong(),
                            viewModel = seatingChartViewModel,
                            onDismiss = { showBehaviorLogViewer = false }
                        )
                    }
                    DropdownMenu(
                        expanded = showGroupMenu,
                        onDismissRequest = { showGroupMenu = false }) {
                        groups.forEach { group ->
                            DropdownMenuItem(text = { Text(group.name) }, onClick = {
                                seatingChartViewModel.assignStudentToGroup(
                                    student.id.toLong(),
                                    group.id
                                )
                                showGroupMenu = false
                                showStudentActionMenu = false
                            })
                        }
                    }
                }
            }

            if (showBehaviorDialog) {
                val studentIds = if (selectMode) selectedStudentIds.map { it.toLong() } else listOfNotNull(selectedStudentUiItemForAction?.id?.toLong())
                BehaviorDialog(
                    studentIds = studentIds,
                    viewModel = seatingChartViewModel,
                    behaviorTypes = behaviorTypeNames,
                    onDismiss = { showBehaviorDialog = false; selectedStudentUiItemForAction = null },
                    onBehaviorLogged = { count ->
                        coroutineScope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Logged behavior for $count student(s)",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                repeat(count) { seatingChartViewModel.undo() }
                            }
                        }
                    }
                )
            }

            if (showLogQuizScoreDialog) {
                val studentIds = if (selectMode) selectedStudentIds.map { it.toLong() } else listOfNotNull(selectedStudentUiItemForAction?.id?.toLong())
                LogQuizScoreDialog(studentIds = studentIds, viewModel = seatingChartViewModel, settingsViewModel = settingsViewModel, onDismissRequest = { showLogQuizScoreDialog = false; selectedStudentUiItemForAction = null }, onSave = { quizLogs ->
                    if (sessionType == SessionType.QUIZ) quizLogs.forEach { seatingChartViewModel.addQuizLogToSession(it) } else quizLogs.forEach { seatingChartViewModel.saveQuizLog(it) }
                })
            }

            if (showAdvancedHomeworkLogDialog) {
                val studentIds = if (selectMode) selectedStudentIds.map { it.toLong() } else listOfNotNull(selectedStudentUiItemForAction?.id?.toLong())
                AdvancedHomeworkLogDialog(studentIds = studentIds, viewModel = seatingChartViewModel, settingsViewModel = settingsViewModel, onDismissRequest = { showAdvancedHomeworkLogDialog = false; selectedStudentUiItemForAction = null }, onSave = { homeworkLogs ->
                    if (sessionType == SessionType.HOMEWORK) homeworkLogs.forEach { seatingChartViewModel.addHomeworkLogToSession(it) } else homeworkLogs.forEach { seatingChartViewModel.addHomeworkLog(it) }
                })
            }

            if (showLiveQuizMarkDialog) {
                selectedStudentUiItemForAction?.let { student ->
                    LiveQuizMarkDialog(
                        studentId = student.id.toLong(),
                        viewModel = seatingChartViewModel,
                        onDismissRequest = { showLiveQuizMarkDialog = false; selectedStudentUiItemForAction = null },
                        onSave = { quizLog -> seatingChartViewModel.addQuizLogToSession(quizLog) }
                    )
                }
            }

            if (showLiveHomeworkMarkDialog) {
                selectedStudentUiItemForAction?.let { student ->
                    LiveHomeworkMarkDialog(
                        studentId = student.id.toLong(),
                        viewModel = seatingChartViewModel,
                        settingsViewModel = settingsViewModel,
                        onDismissRequest = { showLiveHomeworkMarkDialog = false; selectedStudentUiItemForAction = null },
                        onSave = { homeworkLog -> seatingChartViewModel.addHomeworkLogToSession(homeworkLog) }
                    )
                }
            }

            if (showAddEditStudentDialog) {
                AddEditStudentDialog(
                    studentToEdit = editingStudent,
                    viewModel = seatingChartViewModel,
                    studentGroupsViewModel = studentGroupsViewModel,
                    settingsViewModel = settingsViewModel,
                    onDismiss = { showAddEditStudentDialog = false; editingStudent = null },
                    onEditStyle = {
                        showAddEditStudentDialog = false
                        showStudentStyleDialog = true
                    }
                )
            }

            if (showAddEditFurnitureDialog) {
                AddEditFurnitureDialog(furnitureToEdit = editingFurniture, viewModel = seatingChartViewModel, settingsViewModel = settingsViewModel, onDismiss = { showAddEditFurnitureDialog = false; editingFurniture = null })
            }

            if (showExportDialog) {
                ExportDialog(viewModel = seatingChartViewModel, onDismissRequest = { showExportDialog = false }, onExport = { options, share ->
                    seatingChartViewModel.pendingExportOptions = options
                    if (share) {
                        onShowEmailDialogChange(true)
                    } else {
                        createDocumentLauncher.launch("seating_chart_export.xlsx")
                    }
                    showExportDialog = false
                })
            }

            if (showEmailDialog) {
                val activity = (context as? MainActivity)
                val from by settingsViewModel.defaultEmailAddress.collectAsState()
                val emailPassword by settingsViewModel.emailPassword.collectAsState()
                val smtpSettings by settingsViewModel.smtpSettings.collectAsState()
                EmailDialog(
                    fromAddress = from,
                    onDismissRequest = { onShowEmailDialogChange(false) },
                    onSend = { to, subject, body ->
                        activity?.lifecycleScope?.launch {
                            val emailUtil = EmailUtil(activity)
                            // Create a temporary file for the attachment
                            val file = kotlin.io.path.createTempFile("export", ".xlsx").toFile()
                            val uri = Uri.fromFile(file)
                            seatingChartViewModel.pendingExportOptions?.let { options ->
                                val result = seatingChartViewModel.exportData(
                                    context = activity,
                                    uri = uri,
                                    options = options
                                )
                                if (result.isSuccess) {
                                    try {
                                        emailUtil.sendEmailWithRetry(
                                            from = from,
                                            password = emailPassword,
                                            to = to,
                                            subject = subject,
                                            body = body,
                                            attachmentPath = file.absolutePath,
                                            smtpSettings = smtpSettings
                                        )
                                        Toast.makeText(activity, "Email sent!", Toast.LENGTH_SHORT).show()
                                    } catch (e: EmailException) {
                                        Toast.makeText(activity, "Email failed to send: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(activity, "Export failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                            onShowEmailDialogChange(false)
                        }
                    }
                )
            }

            if (showChangeBoxSizeDialog) {
                ChangeBoxSizeDialog(onDismissRequest = { showChangeBoxSizeDialog = false }, onSave = { width, height -> seatingChartViewModel.changeBoxSize(selectedStudentIds, width, height); showChangeBoxSizeDialog = false })
            }

            if (showStudentStyleDialog) {
                val studentId = selectedStudentUiItemForAction?.id?.toLong() ?: editingStudent?.id
                if (studentId != null) {
                    StudentStyleScreen(studentId = studentId, seatingChartViewModel = seatingChartViewModel, onDismiss = { showStudentStyleDialog = false; if (editingStudent != null) editingStudent = null })
                }
            }

            if (showAssignTaskDialog) {
                selectedStudentUiItemForAction?.let { student ->
                    val systemBehaviors by seatingChartViewModel.allSystemBehaviors.observeAsState(initial = emptyList())
                    AssignTaskDialog(
                        studentId = student.id.toLong(),
                        viewModel = seatingChartViewModel,
                        systemBehaviors = systemBehaviors,
                        onDismissRequest = { showAssignTaskDialog = false }
                    )
                }
            }

            if (showUndoHistoryDialog) {
                UndoHistoryDialog(
                    viewModel = seatingChartViewModel,
                    onDismissRequest = { showUndoHistoryDialog = false }
                )
            }

            if (showGhostInsightDialog) {
                currentGhostInsight?.let { insight ->
                    GhostInsightDialog(
                        insight = insight,
                        onDismiss = { showGhostInsightDialog = false }
                    )
                }
            }
        }
    }
}


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
    onShowUndoHistory: () -> Unit,
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
                    HorizontalDivider()
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
                    HorizontalDivider()
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
                        text = { Text("Undo History") },
                        onClick = {
                            onShowUndoHistory()
                            showMoreMenu = false
                        },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Undo, null) }
                    )
                    HorizontalDivider()
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
                    HorizontalDivider()
                    if (editModeEnabled) {
                        DropdownMenuItem(text = { Text("Alignment Tools") }, onClick = { showMoreMenu = false; showAlignSubMenu = true }, leadingIcon = { Icon(Icons.Default.AutoFixHigh, null) })
                    }
                    DropdownMenuItem(text = { Text("Layouts") }, onClick = { showMoreMenu = false; showLayoutSubMenu = true }, leadingIcon = { Icon(Icons.Default.Layers, null) })
                    DropdownMenuItem(text = { Text("Guides & Grid") }, onClick = { showMoreMenu = false; showGuidesSubMenu = true }, leadingIcon = { Icon(Icons.Default.GridView, null) })
                    HorizontalDivider()
                    DropdownMenuItem(text = { Text("Take Screenshot") }, onClick = { onTakeScreenshot(); showMoreMenu = false }, leadingIcon = { Icon(Icons.Default.PhotoCamera, null) })
                    DropdownMenuItem(text = { Text("Appearance") }, onClick = { showMoreMenu = false; showAppearanceSubMenu = true }, leadingIcon = { Icon(Icons.Default.Palette, null) })
                    HorizontalDivider()
                    DropdownMenuItem(text = { Text("Help") }, onClick = { onHelpClick(); showMoreMenu = false }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.Help, null) })
                }

                // Submenus that remain in the overflow context
                DropdownMenu(expanded = showAlignSubMenu, onDismissRequest = { showAlignSubMenu = false }) {
                    DropdownMenuItem(text = { Text("Align Top") }, onClick = { seatingChartViewModel.alignSelectedItems("top"); showAlignSubMenu = false })
                    DropdownMenuItem(text = { Text("Align Bottom") }, onClick = { seatingChartViewModel.alignSelectedItems("bottom"); showAlignSubMenu = false })
                    DropdownMenuItem(text = { Text("Align Left") }, onClick = { seatingChartViewModel.alignSelectedItems("left"); showAlignSubMenu = false })
                    DropdownMenuItem(text = { Text("Align Right") }, onClick = { seatingChartViewModel.alignSelectedItems("right"); showAlignSubMenu = false })
                    HorizontalDivider()
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
