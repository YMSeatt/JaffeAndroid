package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Furniture
import com.example.myapplication.data.GuideType
import com.example.myapplication.data.exporter.ExportOptions
import com.example.myapplication.data.Student
import com.example.myapplication.preferences.AppTheme
import com.example.myapplication.ui.DataViewerScreen
import com.example.myapplication.ui.PasswordScreen
import com.example.myapplication.ui.components.FurnitureDraggableIcon
import com.example.myapplication.ui.components.GridAndRulers
import com.example.myapplication.ui.components.StudentDraggableIcon
import com.example.myapplication.ui.dialogs.AddEditFurnitureDialog
import com.example.myapplication.ui.dialogs.AddEditStudentDialog
import com.example.myapplication.ui.dialogs.AdvancedHomeworkLogDialog
import com.example.myapplication.ui.dialogs.EmailDialog
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
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.ui.screens.RemindersScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.util.EmailException
import com.example.myapplication.util.EmailUtil
import com.example.myapplication.util.EmailWorker
import com.example.myapplication.util.captureComposable
import com.example.myapplication.util.toTitleCase
import com.example.myapplication.viewmodel.GuideViewModel
import com.example.myapplication.viewmodel.ReminderViewModel
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import com.example.myapplication.viewmodel.SettingsViewModelFactory
import com.example.myapplication.viewmodel.StatsViewModel
import com.example.myapplication.viewmodel.StudentGroupsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class SessionType {
    BEHAVIOR,
    QUIZ,
    HOMEWORK
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val seatingChartViewModel: SeatingChartViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(application)
    }
    private val guideViewModel: GuideViewModel by viewModels()

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

    val studentGroupsViewModel: StudentGroupsViewModel by viewModels()

    val statsViewModel: StatsViewModel by viewModels()

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
                            viewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : ViewModelProvider.Factory {
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return ReminderViewModel(application) as T
                                }
                            }),
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
                            showEmailDialog = showEmailDialogState,
                            onShowEmailDialogChange = { showEmailDialogState = it }
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
        val autoSendOnClose = settingsViewModel.autoSendEmailOnClose.value
        if (autoSendOnClose) {
            val email = settingsViewModel.defaultEmailAddress.value
            if (email.isNotBlank()) {
                val exportOptions =
                    pendingExportOptions ?: com.example.myapplication.data.exporter.ExportOptions()
                val workRequest = OneTimeWorkRequestBuilder<EmailWorker>()
                    .setInputData(
                        workDataOf(
                            "email_address" to email,
                            "export_options" to exportOptions.toString()
                        )
                    )
                    .build()
                WorkManager.getInstance(applicationContext).enqueue(workRequest)
            }
        }
    }
}

@Composable
fun EmailDialog(
    onDismissRequest: () -> Unit,
    onSend: (String, String, String) -> Unit,
    fromAddress: String
) {
    var to by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Send Email") },
        text = {
            Column {
                TextField(
                    value = fromAddress,
                    onValueChange = { },
                    label = { Text("From") },
                    readOnly = true
                )
                TextField(
                    value = to,
                    onValueChange = { to = it },
                    label = { Text("To") }
                )
                TextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") }
                )
                TextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Body") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSend(to, subject, body)
                    onDismissRequest()
                }
            ) {
                Text("Send")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

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
    showEmailDialog: Boolean,
    onShowEmailDialogChange: (Boolean) -> Unit
) {
    val students by seatingChartViewModel.studentsForDisplay.observeAsState(initial = emptyList())
    val furniture by seatingChartViewModel.furnitureForDisplay.observeAsState(initial = emptyList())
    val layouts by seatingChartViewModel.allLayoutTemplates.observeAsState(initial = emptyList())
    val selectedStudentIds by seatingChartViewModel.selectedStudentIds.observeAsState(initial = emptySet())

    var showBehaviorDialog by remember { mutableStateOf(false) }
    var showLogQuizScoreDialog by remember { mutableStateOf(false) }
    var showLiveQuizMarkDialog by remember { mutableStateOf(false) }
    var showAdvancedHomeworkLogDialog by remember { mutableStateOf(false) }
    var showLiveHomeworkMarkDialog by remember { mutableStateOf(false) }
    var showStudentActionMenu by remember { mutableStateOf(false) }
    var showSaveLayoutDialog by remember { mutableStateOf(false) }
    var showLoadLayoutDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showChangeBoxSizeDialog by remember { mutableStateOf(false) }
    var showStudentStyleDialog by remember { mutableStateOf(false) }
    var showAssignTaskDialog by remember { mutableStateOf(false) }
    var selectMode by remember { mutableStateOf(false) }

    var selectedStudentUiItemForAction by remember { mutableStateOf<StudentUiItem?>(null) }

    var showAddEditStudentDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var showAddEditFurnitureDialog by remember { mutableStateOf(false) }
    var editingFurniture by remember { mutableStateOf<Furniture?>(null) }
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

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Seating Chart") },
                actions = {
                    TextButton(
                        onClick = {
                            selectMode = !selectMode
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

                    if (editModeEnabled) {
                        Row {
                            TextButton(onClick = { seatingChartViewModel.alignSelectedItems("top") }) { Text("Align Top") }
                            TextButton(onClick = { seatingChartViewModel.alignSelectedItems("bottom") }) { Text("Align Bottom") }
                            TextButton(onClick = { seatingChartViewModel.alignSelectedItems("left") }) { Text("Align Left") }
                            TextButton(onClick = { seatingChartViewModel.alignSelectedItems("right") }) { Text("Align Right") }
                            TextButton(onClick = { seatingChartViewModel.distributeSelectedItems("horizontal") }) { Text("Distribute H") }
                            TextButton(onClick = { seatingChartViewModel.distributeSelectedItems("vertical") }) { Text("Distribute V") }
                        }
                    }

                    if (selectMode && selectedStudentIds.isNotEmpty()) {
                        var showActionsMenu by remember { mutableStateOf(false) }
                        TextButton(onClick = { showActionsMenu = true }) {
                            Text("Actions")
                        }
                        DropdownMenu(
                            expanded = showActionsMenu,
                            onDismissRequest = { showActionsMenu = false }
                        ) {
                            DropdownMenuItem(text = { Text("Log Behavior") }, onClick = { showBehaviorDialog = true; showActionsMenu = false })
                            DropdownMenuItem(text = { Text("Log Quiz") }, onClick = { showLogQuizScoreDialog = true; showActionsMenu = false })
                            DropdownMenuItem(text = { Text("Delete") }, onClick = { seatingChartViewModel.deleteStudents(selectedStudentIds); showActionsMenu = false })
                            DropdownMenuItem(text = { Text("Change Box Size") }, onClick = { showChangeBoxSizeDialog = true; showActionsMenu = false })
                        }
                    }

                    IconButton(onClick = { seatingChartViewModel.undo() }) { Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo") }
                    IconButton(onClick = { seatingChartViewModel.redo() }) { Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo") }

                    val isSessionActive by seatingChartViewModel.isSessionActive.observeAsState(initial = false)
                    if (sessionType == SessionType.QUIZ || sessionType == SessionType.HOMEWORK) {
                        TextButton(onClick = {
                            if (isSessionActive) seatingChartViewModel.endSession() else seatingChartViewModel.startSession()
                        }) {
                            Text(if (isSessionActive) "End Session" else "Start Session")
                        }
                    }

                    var showMoreMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        val lastExportPath by settingsViewModel.lastExportPath.collectAsState()
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(text = { Text("Save Layout") }, onClick = { showSaveLayoutDialog = true; showMoreMenu = false })
                            DropdownMenuItem(text = { Text("Import from JSON") }, onClick = { (context as? MainActivity)?.importJsonLauncher?.launch("application/json"); showMoreMenu = false })
                            DropdownMenuItem(text = { Text("Import from Python") }, onClick = { seatingChartViewModel.importFromPythonAssets(context); showMoreMenu = false })
                            DropdownMenuItem(text = { Text("Load Layout") }, onClick = { showLoadLayoutDialog = true; showMoreMenu = false })
                            DropdownMenuItem(text = { Text("Import Students from Excel") }, onClick = { (context as? MainActivity)?.importStudentsLauncher?.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); showMoreMenu = false })
                            DropdownMenuItem(text = { Text("Export to Excel") }, onClick = {
                                if (seatingChartViewModel.studentsForDisplay.value?.isNotEmpty() == true) {
                                    showExportDialog = true
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
                            Divider()
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
                            Divider()
                            Text("Theme", modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp))
                            AppTheme.entries.forEach { theme ->
                                DropdownMenuItem(text = { Text(theme.name.toTitleCase()) }, onClick = { settingsViewModel.updateAppTheme(theme); showViewMenu = false })
                            }
                        }
                    }

                    var showModeMenu by remember { mutableStateOf(false) }
                    val isSessionActive by seatingChartViewModel.isSessionActive.observeAsState(initial = false)

                    Box {
                        TextButton(onClick = { showModeMenu = true }) { Text(sessionType.name.toTitleCase()) }
                        DropdownMenu(expanded = showModeMenu, onDismissRequest = { showModeMenu = false }, offset = DpOffset(x = 0.dp, y = 0.dp)) {
                            SessionType.entries.forEach { mode ->
                                DropdownMenuItem(text = { Text(mode.name.toTitleCase()) }, onClick = {
                                    if (isSessionActive) {
                                        seatingChartViewModel.endSession()
                                    }
                                    sessionType = mode
                                    showMoreMenu = false
                                })
                            }
                            Divider()
                            DropdownMenuItem(text = { Text("Settings") }, onClick = { onNavigateToSettings(); showMoreMenu = false })
                            DropdownMenuItem(text = { Text("Help") }, onClick = { onHelpClick(); showMoreMenu = false })
                        }
                    }
                }
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
                                duration = androidx.compose.material3.SnackbarDuration.Short
                            )
                            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
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
                        (context as? MainActivity)?.createDocumentLauncher?.launch("seating_chart_export.xlsx")
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
        }
    }
}