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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.stringResource
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
import com.example.myapplication.ui.dialogs.AssignTaskDialog
import com.example.myapplication.ui.dialogs.BehaviorDialog
import com.example.myapplication.ui.dialogs.BehaviorLogViewerDialog
import com.example.myapplication.ui.dialogs.ChangeBoxSizeDialog
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

    var pendingExportOptions: com.example.myapplication.data.exporter.ExportOptions? by mutableStateOf(null)

    val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
        uri?.let {
            pendingExportOptions?.let { options ->
                lifecycleScope.launch {
                    val result = seatingChartViewModel.exportData(
                        context = this@MainActivity,
                        uri = it,
                        options = options
                    )
                    if (result.isSuccess) {
                        Toast.makeText(this@MainActivity, getString(R.string.data_exported_successfully), Toast.LENGTH_LONG).show()
                        settingsViewModel.updateLastExportPath(it.toString())
                    } else {
                        Toast.makeText(this@MainActivity, getString(R.string.export_failed, result.exceptionOrNull()?.message), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        pendingExportOptions = null
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
                Toast.makeText(this@MainActivity, R.string.data_imported_successfully, Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@MainActivity, getString(R.string.students_imported_successfully, count), Toast.LENGTH_SHORT).show()
                }.onFailure { error ->
                    Toast.makeText(this@MainActivity, getString(R.string.error_importing_students, error.message), Toast.LENGTH_LONG).show()
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
                Toast.makeText(this@MainActivity, R.string.data_exported_successfully, Toast.LENGTH_SHORT).show()
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
        lifecycleScope.launch {
            val autoSendOnClose: Boolean = settingsViewModel.autoSendEmailOnClose.first()
            if (autoSendOnClose) {
                val email: String = settingsViewModel.defaultEmailAddress.first()
                if (email.isNotBlank()) {
                    val exportOptions = pendingExportOptions ?: com.example.myapplication.data.exporter.ExportOptions()
                    val workRequest = OneTimeWorkRequestBuilder<EmailWorker>()
                        .setInputData(workDataOf(
                            "email_address" to email,
                            "export_options" to exportOptions.toString()
                        ))
                        .build()
                    WorkManager.getInstance(applicationContext).enqueue(workRequest)
                }
            }
        }
    }
}

@Composable
fun EmailDialog(
    onDismissRequest: () -> Unit,
    onSend: (String, String, String) -> Unit,
    settingsViewModel: SettingsViewModel,
    fromAddress: String
) {
    var to by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(id = R.string.send_email)) },
        text = {
            Column {
                TextField(
                    value = fromAddress,
                    onValueChange = { },
                    label = { Text(stringResource(id = R.string.from)) },
                    readOnly = true
                )
                TextField(
                    value = to,
                    onValueChange = { to = it },
                    label = { Text(stringResource(id = R.string.to)) }
                )
                TextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text(stringResource(id = R.string.subject)) }
                )
                TextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text(stringResource(id = R.string.body)) }
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
                Text(stringResource(id = R.string.send))
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text(stringResource(id = R.string.color_picker_dialog_cancel))
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
                title = { Text(stringResource(id = R.string.seating_chart)) },
                actions = {
                    var showFileMenu by remember { mutableStateOf(false) }
                    var showViewMenu by remember { mutableStateOf(false) }

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
                            if (selectMode) stringResource(id = R.string.exit) else stringResource(id = R.string.select),
                            maxLines = 1
                        )
                    }

                    if (editModeEnabled) {
                        Row {
                            TextButton(onClick = { seatingChartViewModel.alignSelectedItems("top") }) { Text(stringResource(id = R.string.align_top)) }
                            TextButton(onClick = { seatingChartViewModel.alignSelectedItems("bottom") }) { Text(stringResource(id = R.string.align_bottom)) }
                            TextButton(onClick = { seatingChartViewModel.alignSelectedItems("left") }) { Text(stringResource(id = R.string.align_left)) }
                            TextButton(onClick = { seatingChartViewModel.alignSelectedItems("right") }) { Text(stringResource(id = R.string.align_right)) }
                            TextButton(onClick = { seatingChartViewModel.distributeSelectedItems("horizontal") }) { Text(stringResource(id = R.string.distribute_h)) }
                            TextButton(onClick = { seatingChartViewModel.distributeSelectedItems("vertical") }) { Text(stringResource(id = R.string.distribute_v)) }
                        }
                    }

                    if (selectMode && selectedStudentIds.isNotEmpty()) {
                        var showActionsMenu by remember { mutableStateOf(false) }
                        TextButton(onClick = { showActionsMenu = true }) {
                            Text(stringResource(id = R.string.actions))
                        }
                        DropdownMenu(
                            expanded = showActionsMenu,
                            onDismissRequest = { showActionsMenu = false }
                        ) {
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.log_behavior)) }, onClick = { showBehaviorDialog = true; showActionsMenu = false })
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.log_quiz)) }, onClick = { showLogQuizScoreDialog = true; showActionsMenu = false })
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.delete)) }, onClick = { seatingChartViewModel.deleteStudents(selectedStudentIds); showActionsMenu = false })
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.change_box_size)) }, onClick = { showChangeBoxSizeDialog = true; showActionsMenu = false })
                        }
                    }

                    IconButton(onClick = { seatingChartViewModel.undo() }) { Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = stringResource(id = R.string.undo)) }
                    IconButton(onClick = { seatingChartViewModel.redo() }) { Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = stringResource(id = R.string.redo)) }
                    Box {
                        IconButton(onClick = { showFileMenu = true }) { Text(stringResource(id = R.string.file)) }
                        val lastExportPath by settingsViewModel.lastExportPath.collectAsState()
                        DropdownMenu(expanded = showFileMenu, onDismissRequest = { showFileMenu = false }, offset = DpOffset(x = 0.dp, y = 0.dp)) {
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.save_layout)) }, onClick = { showSaveLayoutDialog = true; showFileMenu = false })
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.import_from_json)) }, onClick = { (context as? MainActivity)?.importJsonLauncher?.launch("application/json"); showFileMenu = false })
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.import_from_python)) }, onClick = { seatingChartViewModel.importFromPythonAssets(context); showFileMenu = false })
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.load_layout)) }, onClick = { showLoadLayoutDialog = true; showFileMenu = false })
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.import_students_from_excel)) }, onClick = { (context as? MainActivity)?.importStudentsLauncher?.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); showFileMenu = false })
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.export_to_excel)) }, onClick = {
                                if (seatingChartViewModel.studentsForDisplay.value?.isNotEmpty() == true) {
                                    showExportDialog = true
                                } else {
                                    Toast.makeText(context, R.string.no_student_data_to_export, Toast.LENGTH_SHORT).show()
                                }
                                showFileMenu = false
                            })
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.open_last_export_folder)) }, enabled = lastExportPath?.isNotBlank() == true, onClick = {
                                lastExportPath?.let { path ->
                                    val uri = path.toUri()
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.setDataAndType(uri, "vnd.android.document/directory")
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    if (intent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(context, R.string.could_not_open_folder, Toast.LENGTH_SHORT).show()
                                    }
                                }
                                showFileMenu = false
                            })
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.view_data)) }, onClick = { onNavigateToDataViewer(); showFileMenu = false })
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.reminders)) }, onClick = { onNavigateToReminders(); showFileMenu = false })
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.export_database)) }, onClick = {
                                coroutineScope.launch {
                                    val uri = settingsViewModel.shareDatabase()
                                    if (uri != null) {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/octet-stream"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_database)))
                                    } else {
                                        Toast.makeText(context, R.string.could_not_export_database, Toast.LENGTH_SHORT).show()
                                    }
                                }
                                showFileMenu = false
                            })
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.open_data_folder)) }, onClick = { (context as? MainActivity)?.exportDataFolderLauncher?.launch(null); showFileMenu = false })
                        }
                    }
                    Box {
                        IconButton(onClick = { showViewMenu = true }) { Text(stringResource(id = R.string.view)) }
                        DropdownMenu(expanded = showViewMenu, onDismissRequest = { showViewMenu = false }, offset = DpOffset(x = 0.dp, y = 0.dp)) {
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.add_vertical_guide)) }, onClick = { guideViewModel.addGuide(GuideType.VERTICAL); showViewMenu = false })
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.add_horizontal_guide)) }, onClick = { guideViewModel.addGuide(GuideType.HORIZONTAL); showViewMenu = false })
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.clear_guides)) }, onClick = {
                                guideViewModel.guides.value.forEach { guideViewModel.deleteGuide(it) }
                                showViewMenu = false
                            })
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.take_screenshot)) }, onClick = {
                                coroutineScope.launch {
                                    val view = (context as Activity).window.decorView
                                    val bitmap = captureComposable(view, context.window)
                                    if (bitmap != null) {
                                        settingsViewModel.saveScreenshot(bitmap)
                                        Toast.makeText(context, R.string.screenshot_saved, Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, R.string.failed_to_capture_screenshot, Toast.LENGTH_SHORT).show()
                                    }
                                }
                                showViewMenu = false
                            })
                            AppTheme.entries.forEach { theme ->
                                DropdownMenuItem(text = { Text(theme.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }) }, onClick = { settingsViewModel.updateAppTheme(theme); showViewMenu = false })
                            }
                        }
                    }

                    var showModeMenu by remember { mutableStateOf(false) }
                    val isSessionActive by seatingChartViewModel.isSessionActive.observeAsState(initial = false)

                    Box {
                        TextButton(onClick = { showModeMenu = true }) { Text(sessionType.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }) }
                        DropdownMenu(expanded = showModeMenu, onDismissRequest = { showModeMenu = false }, offset = DpOffset(x = 0.dp, y = 0.dp)) {
                            SessionType.entries.forEach { mode ->
                                DropdownMenuItem(text = { Text(mode.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }) }, onClick = {
                                    if (isSessionActive) {
                                        seatingChartViewModel.endSession()
                                    }
                                    sessionType = mode
                                    showModeMenu = false
                                })
                            }
                        }
                    }

                    if (sessionType == SessionType.QUIZ || sessionType == SessionType.HOMEWORK) {
                        TextButton(onClick = {
                            if (isSessionActive) seatingChartViewModel.endSession() else seatingChartViewModel.startSession()
                        }) {
                            Text(if (isSessionActive) stringResource(id = R.string.end_session) else stringResource(id = R.string.start_session))
                        }
                    }

                    IconButton(onClick = onNavigateToSettings) { Icon(Icons.Filled.Settings, contentDescription = stringResource(id = R.string.settings)) }
                    IconButton(onClick = onHelpClick) { Icon(Icons.AutoMirrored.Filled.Help, contentDescription = stringResource(id = R.string.help)) }
                }
            )
        },
        floatingActionButton = {
            if (editModeEnabled) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AnimatedVisibility(visible = isFabMenuOpen) {
                        FloatingActionButton(onClick = { editingStudent = null; showAddEditStudentDialog = true; isFabMenuOpen = false }) {
                            Icon(Icons.Default.Person, contentDescription = stringResource(id = R.string.add_student))
                        }
                    }
                    AnimatedVisibility(visible = isFabMenuOpen) {
                        FloatingActionButton(onClick = { editingFurniture = null; showAddEditFurnitureDialog = true; isFabMenuOpen = false }) {
                            Icon(Icons.Default.Chair, contentDescription = stringResource(id = R.string.add_furniture))
                        }
                    }
                    FloatingActionButton(onClick = { isFabMenuOpen = !isFabMenuOpen }) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(id = R.string.add))
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

            if (showStudentActionMenu && selectedStudentUiItemForAction != null) {
                val student = selectedStudentUiItemForAction!!
                val groups by studentGroupsViewModel.allStudentGroups.collectAsState(initial = emptyList())
                var showGroupMenu by remember { mutableStateOf(false) }

                var showBehaviorLogViewer by remember { mutableStateOf(false) }

                DropdownMenu(expanded = true, onDismissRequest = { showStudentActionMenu = false }, offset = DpOffset(longPressPosition.x.dp, longPressPosition.y.dp)) {
                    DropdownMenuItem(text = { Text(stringResource(id = R.string.edit_student)) }, onClick = {
                        coroutineScope.launch { editingStudent = seatingChartViewModel.getStudentForEditing(student.id.toLong()); showAddEditStudentDialog = true }
                        showStudentActionMenu = false
                    })
                    DropdownMenuItem(text = { Text(stringResource(id = R.string.delete_student)) }, onClick = { seatingChartViewModel.deleteStudents(setOf(student.id)); showStudentActionMenu = false })
                    DropdownMenuItem(text = { Text(stringResource(id = R.string.log_behavior)) }, onClick = { showBehaviorDialog = true; showStudentActionMenu = false })
                    DropdownMenuItem(text = { Text(stringResource(id = R.string.view_behavior_log)) }, onClick = { showBehaviorLogViewer = true; showStudentActionMenu = false })
                    DropdownMenuItem(text = { Text(stringResource(id = R.string.log_homework)) }, onClick = { showAdvancedHomeworkLogDialog = true; showStudentActionMenu = false })
                    DropdownMenuItem(text = { Text(stringResource(id = R.string.log_quiz_score)) }, onClick = { showLogQuizScoreDialog = true; showStudentActionMenu = false })
                    DropdownMenuItem(text = { Text(stringResource(id = R.string.assign_task)) }, onClick = { showAssignTaskDialog = true; showStudentActionMenu = false })
                    DropdownMenuItem(text = { Text(stringResource(id = R.string.change_student_box_style)) }, onClick = { showStudentStyleDialog = true; showStudentActionMenu = false })
                    DropdownMenuItem(text = { Text(stringResource(id = R.string.clear_recent_logs)) }, onClick = { seatingChartViewModel.clearRecentLogsForStudent(student.id.toLong()); showStudentActionMenu = false })
                    DropdownMenuItem(text = { Text(stringResource(id = R.string.show_recent_logs)) }, onClick = { seatingChartViewModel.showRecentLogsForStudent(student.id.toLong()); showStudentActionMenu = false })
                    DropdownMenuItem(text = { Text(stringResource(id = R.string.assign_to_group)) }, onClick = { showGroupMenu = true })
                    if (student.groupId != null) {
                        DropdownMenuItem(text = { Text(stringResource(id = R.string.remove_from_group)) }, onClick = { seatingChartViewModel.removeStudentFromGroup(student.id.toLong()); showStudentActionMenu = false })
                    }
                }
                if (showBehaviorLogViewer) {
                    BehaviorLogViewerDialog(
                        studentId = student.id.toLong(),
                        viewModel = seatingChartViewModel,
                        onDismiss = { showBehaviorLogViewer = false }
                    )
                }
                DropdownMenu(expanded = showGroupMenu, onDismissRequest = { showGroupMenu = false }) {
                    groups.forEach { group ->
                        DropdownMenuItem(text = { Text(group.name) }, onClick = { seatingChartViewModel.assignStudentToGroup(student.id.toLong(), group.id); showGroupMenu = false; showStudentActionMenu = false })
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
                                message = context.getString(R.string.logged_behavior_for_students, count),
                                actionLabel = context.getString(R.string.undo),
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
                    (context as? MainActivity)?.pendingExportOptions = options
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
                EmailDialog(
                    fromAddress = from,
                    onDismissRequest = { onShowEmailDialogChange(false) },
                    onSend = { to, subject, body ->
                        activity?.lifecycleScope?.launch {
                            val emailUtil = EmailUtil(activity)
                            // Create a temporary file for the attachment
                            val file = kotlin.io.path.createTempFile("export", ".xlsx").toFile()
                            val uri = Uri.fromFile(file)
                            activity.pendingExportOptions?.let { options ->
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
                                            attachmentPath = file.absolutePath
                                        )
                                        Toast.makeText(activity, R.string.email_sent, Toast.LENGTH_SHORT).show()
                                    } catch (e: EmailException) {
                                        Toast.makeText(activity, context.getString(R.string.email_failed_to_send, e.message), Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(activity, context.getString(R.string.export_failed, result.exceptionOrNull()?.message), Toast.LENGTH_LONG).show()
                                }
                            }
                            onShowEmailDialogChange(false)
                        }
                    },
                    settingsViewModel = settingsViewModel
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