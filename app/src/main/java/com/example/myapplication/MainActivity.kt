package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Undo
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Furniture
import com.example.myapplication.data.Student
import com.example.myapplication.preferences.AppTheme
import com.example.myapplication.ui.DataViewerScreen
import com.example.myapplication.ui.PasswordScreen
import com.example.myapplication.ui.components.FurnitureDraggableIcon
import com.example.myapplication.ui.components.StudentDraggableIcon
import com.example.myapplication.ui.dialogs.AddEditFurnitureDialog
import com.example.myapplication.ui.dialogs.AddEditStudentDialog
import com.example.myapplication.ui.dialogs.AdvancedHomeworkLogDialog
import com.example.myapplication.ui.dialogs.BehaviorDialog
import com.example.myapplication.ui.dialogs.ChangeBoxSizeDialog
import com.example.myapplication.ui.dialogs.ExportFilterDialog
import com.example.myapplication.ui.dialogs.ExportFilterOptions
import com.example.myapplication.ui.dialogs.ExportType
import com.example.myapplication.ui.dialogs.LoadLayoutDialog
import com.example.myapplication.ui.dialogs.LogQuizScoreDialog
import com.example.myapplication.ui.dialogs.SaveLayoutDialog
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.ExcelImportUtil
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import com.example.myapplication.viewmodel.StudentGroupsViewModel
import kotlinx.coroutines.launch
import java.util.Locale

enum class SessionType {
    BEHAVIOR,
    QUIZ,
    HOMEWORK
}

class MainActivity : ComponentActivity() {
    private val seatingChartViewModel: SeatingChartViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private var pendingExportFilterOptions by mutableStateOf<ExportFilterOptions?>(null)

    private val filteredExportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
        pendingExportFilterOptions?.let { filterOptions ->
            uri?.let {
                lifecycleScope.launch {
                                        val result = seatingChartViewModel.exportFilteredData(
                        context = this@MainActivity,
                        uri = it,
                        filterOptions = filterOptions
                    )

                    if (result.isSuccess) {
                        Toast.makeText(this@MainActivity, "Data exported successfully!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Export failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        pendingExportFilterOptions = null // Clear pending options
    }

    private val studentGroupsViewModelFactory by lazy {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(StudentGroupsViewModel::class.java)) {
                    val studentGroupDao = AppDatabase.getDatabase(applicationContext).studentGroupDao()
                    @Suppress("UNCHECKED_CAST")
                    return StudentGroupsViewModel(studentGroupDao) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private val studentGroupsViewModel: StudentGroupsViewModel by viewModels { studentGroupsViewModelFactory }

    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // This part needs to be handled by the specific action that requested the permission
            // For example, if export requested it, then proceed with export.
            // For now, just a toast.
            Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch {
                val exportData = seatingChartViewModel.getExportData()
                if (exportData.students.isEmpty()) {
                    Toast.makeText(this@MainActivity, "No student data to export", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val result = ExcelImportUtil.exportData(
                    context = this@MainActivity,
                    uri = it,
                    allStudents = exportData.students,
                    behaviorLogs = exportData.behaviorEvents,
                    homeworkLogs = exportData.homeworkLogs,
                    quizLogs = exportData.quizLogs,
                    filterOptions = ExportFilterOptions(
                        startDate = null,
                        endDate = null,
                        exportBehaviorLogs = true,
                        exportHomeworkLogs = true,
                        exportQuizLogs = true,
                        selectedStudentIds = emptyList(),
                        exportType = ExportType.MASTER_LOG
                    )
                )
                if (result.isSuccess) {
                    Toast.makeText(this@MainActivity, "Data exported successfully", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@MainActivity, "Failed to export data: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val currentAppThemeState by settingsViewModel.appTheme.collectAsState()
            val passwordEnabled by settingsViewModel.passwordEnabled.collectAsState()
            var unlocked by remember { mutableStateOf(!passwordEnabled) }

            MyApplicationTheme(
                darkTheme = when (currentAppThemeState) {
                    AppTheme.LIGHT -> false
                    AppTheme.DARK -> true
                    AppTheme.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
                if (unlocked) {
                    SeatingChartScreen(
                        seatingChartViewModel = seatingChartViewModel,
                        settingsViewModel = settingsViewModel,
                        studentGroupsViewModel = studentGroupsViewModel,
                        createDocumentLauncher = createDocumentLauncher,
                        lifecycleScope = lifecycleScope,
                        onNavigateToSettings = {
                            startActivity(Intent(this, SettingsActivity::class.java))
                        },
                        onPendingExportFilterOptionsChanged = { options -> pendingExportFilterOptions = options },
                        filteredExportLauncher = filteredExportLauncher
                    )
                } else {
                    PasswordScreen(settingsViewModel = settingsViewModel) {
                        unlocked = true
                    }
                }
            }
        }
    }
}

@Composable
fun ExportOptionsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    createDocumentLauncher: ActivityResultLauncher<String>,
    context: Context,
    seatingChartViewModel: SeatingChartViewModel,
    lifecycleScope: LifecycleCoroutineScope
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Export Options") },
            text = { Text("How would you like to export the data?") },
            confirmButton = {
                Button(onClick = {
                    // Save to Device
                    createDocumentLauncher.launch("seating_chart_data.xlsx")
                    onDismiss() // Dismiss dialog after action
                }) {
                    Text("Save to Device")
                }
            },
            dismissButton = {
                Button(onClick = {
                    // Share File
                    lifecycleScope.launch {
                        val exportData = seatingChartViewModel.getExportData()
                        if (exportData.students.isEmpty()) {
                            Toast.makeText(context, "No student data to share", Toast.LENGTH_SHORT).show()
                            onDismiss() // Dismiss dialog even if no data
                            return@launch
                        }
                        val tempFile = ExcelImportUtil.exportDataToTempFile(
                            context = context,
                            students = exportData.students,
                            behaviorLogs = exportData.behaviorEvents,
                            homeworkLogs = exportData.homeworkLogs,
                            quizLogs = exportData.quizLogs
                        )
                        tempFile?.let { tempFile ->
                            val fileUri: Uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                tempFile
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                putExtra(Intent.EXTRA_STREAM, fileUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Excel File"))
                        } ?: Toast.makeText(context, "Failed to create share file", Toast.LENGTH_SHORT).show()
                    }
                    onDismiss() // Dismiss dialog after action
                }) {
                    Text("Share File")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SeatingChartScreen(
    seatingChartViewModel: SeatingChartViewModel,
    settingsViewModel: SettingsViewModel,
    studentGroupsViewModel: StudentGroupsViewModel,
    createDocumentLauncher: ActivityResultLauncher<String>,
    lifecycleScope: LifecycleCoroutineScope,
    onNavigateToSettings: () -> Unit,
    onPendingExportFilterOptionsChanged: (ExportFilterOptions?) -> Unit,
    filteredExportLauncher: ActivityResultLauncher<String>
) {
    val students by seatingChartViewModel.studentsForDisplay.observeAsState(initial = emptyList())
    val furniture by seatingChartViewModel.furnitureForDisplay.observeAsState(initial = emptyList())
    val layouts by seatingChartViewModel.allLayoutTemplates.observeAsState(initial = emptyList())
    val selectedStudentIds by seatingChartViewModel.selectedStudentIds.observeAsState(initial = emptySet())
    
    var showBehaviorDialog by remember { mutableStateOf(false) }
    var showLogQuizScoreDialog by remember { mutableStateOf(false) }
    var showAdvancedHomeworkLogDialog by remember { mutableStateOf(false) }
    var showStudentActionMenu by remember { mutableStateOf(false) }
    var showSaveLayoutDialog by remember { mutableStateOf(false) }
    var showLoadLayoutDialog by remember { mutableStateOf(false) }
    var showDataViewerDialog by remember { mutableStateOf(false) }
    var showExportFilterDialog by remember { mutableStateOf(false) }
    var showChangeBoxSizeDialog by remember { mutableStateOf(false) }
    var selectMode by remember { mutableStateOf(false) }

    var selectedStudentUiItemForAction by remember { mutableStateOf<StudentUiItem?>(null) }
    
    var showAddEditStudentDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var showAddEditFurnitureDialog by remember { mutableStateOf(false) }
    var editingFurniture by remember { mutableStateOf<Furniture?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val behaviorTypes by settingsViewModel.customBehaviors.observeAsState(initial = emptyList())
    val behaviorTypeNames = remember(behaviorTypes) { behaviorTypes.map { it.name } }
    val showRecentBehavior by settingsViewModel.showRecentBehavior.collectAsState(initial = false)
    var sessionType by remember { mutableStateOf(SessionType.BEHAVIOR) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seating Chart") },
                actions = {
                    var showFileMenu by remember { mutableStateOf(false) }
                    var showViewMenu by remember { mutableStateOf(false) }
                    var showEditMenu by remember { mutableStateOf(false) }

                    IconButton(onClick = { selectMode = !selectMode }) {
                        Text(if (selectMode) "Exit Select Mode" else "Select Mode")
                    }

                    if (selectMode && selectedStudentIds.isNotEmpty()) {
                        var showActionsMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showActionsMenu = true }) {
                            Text("Actions")
                        }
                        DropdownMenu(
                            expanded = showActionsMenu,
                            onDismissRequest = { showActionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Log Behavior") },
                                onClick = {
                                    showBehaviorDialog = true
                                    showActionsMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Log Quiz") },
                                onClick = {
                                    showLogQuizScoreDialog = true
                                    showActionsMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    seatingChartViewModel.deleteStudents(selectedStudentIds)
                                    showActionsMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Change Box Size") },
                                onClick = {
                                    showChangeBoxSizeDialog = true
                                    showActionsMenu = false
                                }
                            )
                        }
                    }

                    IconButton(onClick = { seatingChartViewModel.undo() }) {
                        Icon(Icons.Filled.Undo, contentDescription = "Undo")
                    }
                    IconButton(onClick = { seatingChartViewModel.redo() }) {
                        Icon(Icons.Filled.Redo, contentDescription = "Redo")
                    }

                    IconButton(onClick = { showFileMenu = true }) {
                        Text("File")
                    }
                    DropdownMenu(
                        expanded = showFileMenu,
                        onDismissRequest = { showFileMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Save Layout") },
                            onClick = {
                                showSaveLayoutDialog = true
                                showFileMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Load Layout") },
                            onClick = {
                                showLoadLayoutDialog = true
                                showFileMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export to Excel") },
                            onClick = {
                                if (seatingChartViewModel.studentsForDisplay.value?.isNotEmpty() == true) {
                                    showExportFilterDialog = true
                                } else {
                                    Toast.makeText(
                                        context,
                                        "No student data to export",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                showFileMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("View Data") },
                            onClick = {
                                showDataViewerDialog = true
                                showFileMenu = false
                            }
                        )
                    }

                    IconButton(onClick = { showViewMenu = true }) {
                        Text("View")
                    }
                    DropdownMenu(
                        expanded = showViewMenu,
                        onDismissRequest = { showViewMenu = false }
                    ) {
                        AppTheme.entries.forEach { theme ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        theme.name.lowercase()
                                            .replaceFirstChar { it.titlecase(Locale.getDefault()) })
                                },
                                onClick = {
                                    settingsViewModel.updateAppTheme(theme)
                                    showViewMenu = false
                                }
                            )
                        }
                    }

                    var showModeMenu by remember { mutableStateOf(false) }

                    Box {
                        TextButton(onClick = { showModeMenu = true }) {
                            Text(sessionType.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) })
                        }
                        DropdownMenu(
                            expanded = showModeMenu,
                            onDismissRequest = { showModeMenu = false }
                        ) {
                            SessionType.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            mode.name.lowercase()
                                                .replaceFirstChar { it.titlecase(Locale.getDefault()) })
                                    },
                                    onClick = {
                                        seatingChartViewModel.endSession()
                                        sessionType = mode
                                        seatingChartViewModel.startSession()
                                        showModeMenu = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        editingStudent = null
                        showAddEditStudentDialog = true
                    }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Student")
                }
                FloatingActionButton(
                    onClick = {
                        editingFurniture = null
                        showAddEditFurnitureDialog = true
                    }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Furniture")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 3f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
            ) {
                students.forEach { studentItem ->
                    StudentDraggableIcon(
                        studentUiItem = studentItem,
                        viewModel = seatingChartViewModel,
                        showBehavior = showRecentBehavior,
                        scale = scale,
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
                                    SessionType.QUIZ -> showLogQuizScoreDialog = true
                                    SessionType.HOMEWORK -> showAdvancedHomeworkLogDialog = true
                                }
                            }
                        },
                        onLongClick = {
                            coroutineScope.launch {
                                editingStudent =
                                    seatingChartViewModel.getStudentForEditing(studentItem.id)
                                showAddEditStudentDialog = true
                            }
                        }
                    )
                }
                furniture.forEach { furnitureItem ->
                    FurnitureDraggableIcon(
                        furnitureUiItem = furnitureItem,
                        viewModel = seatingChartViewModel,
                        scale = scale,
                        onLongClick = {
                            coroutineScope.launch {
                                editingFurniture =
                                    seatingChartViewModel.getFurnitureById(furnitureItem.id)
                                showAddEditFurnitureDialog = true
                            }
                        }
                    )
                }
            }
        }

        if (showSaveLayoutDialog) {
            SaveLayoutDialog(
                onDismiss = { showSaveLayoutDialog = false },
                onSave = { name ->
                    seatingChartViewModel.saveLayout(name)
                    showSaveLayoutDialog = false
                }
            )
        }

        if (showLoadLayoutDialog) {
            LoadLayoutDialog(
                layouts = layouts,
                onDismiss = { showLoadLayoutDialog = false },
                onLoad = { layout ->
                    seatingChartViewModel.loadLayout(layout)
                    showLoadLayoutDialog = false
                },
                onDelete = { layout ->
                    seatingChartViewModel.deleteLayoutTemplate(layout)
                }
            )
        }

        if (showStudentActionMenu && selectedStudentUiItemForAction != null) {
            AlertDialog(
                onDismissRequest = {
                    showStudentActionMenu = false
                    selectedStudentUiItemForAction = null
                },
                title = { Text("Actions for ${selectedStudentUiItemForAction!!.fullName}") },
                text = {
                    Column {
                        TextButton(onClick = {
                            showStudentActionMenu = false
                            showLogQuizScoreDialog = true
                        }) {
                            Text("Log Quiz Score")
                        }
                        TextButton(onClick = {
                            showStudentActionMenu = false
                            showAdvancedHomeworkLogDialog = true
                        }) {
                            Text("Log Advanced Homework")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showStudentActionMenu = false
                        selectedStudentUiItemForAction = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showBehaviorDialog) {
            val studentIds = if (selectMode) {
                selectedStudentIds.map { it.toLong() }
            } else {
                listOfNotNull(selectedStudentUiItemForAction?.id?.toLong())
            }
            BehaviorDialog(
                studentIds = studentIds,
                viewModel = seatingChartViewModel,
                behaviorTypes = behaviorTypeNames, // Pass extracted names
                onDismiss = {
                    showBehaviorDialog = false
                    selectedStudentUiItemForAction = null
                }
            )
        }

        if (showLogQuizScoreDialog) {
            val studentIds = if (selectMode) {
                selectedStudentIds.map { it.toLong() }
            } else {
                listOf(selectedStudentUiItemForAction!!.id.toLong())
            }
            LogQuizScoreDialog(
                studentIds = studentIds,
                viewModel = seatingChartViewModel,
                settingsViewModel = settingsViewModel,
                onDismissRequest = {
                    showLogQuizScoreDialog = false
                    selectedStudentUiItemForAction = null
                },
                onSave = { quizLogs ->
                    if (sessionType == SessionType.QUIZ) {
                        quizLogs.forEach { seatingChartViewModel.addQuizLogToSession(it) }
                    } else {
                        quizLogs.forEach { seatingChartViewModel.saveQuizLog(it) }
                    }
                }
            )
        }

        if (showAdvancedHomeworkLogDialog && selectedStudentUiItemForAction != null) {
            AdvancedHomeworkLogDialog(
                studentId = selectedStudentUiItemForAction!!.id.toLong(),
                viewModel = seatingChartViewModel,
                settingsViewModel = settingsViewModel,
                onDismissRequest = {
                    showAdvancedHomeworkLogDialog = false
                    selectedStudentUiItemForAction = null
                },
                onSave = { homeworkLog ->
                    if (sessionType == SessionType.HOMEWORK) {
                        seatingChartViewModel.addHomeworkLogToSession(homeworkLog)
                    } else {
                        seatingChartViewModel.addHomeworkLog(homeworkLog)
                    }
                }
            )
        }


        if (showAddEditStudentDialog) {
            AddEditStudentDialog(
                studentToEdit = editingStudent,
                viewModel = seatingChartViewModel,
                studentGroupsViewModel = studentGroupsViewModel,
                settingsViewModel = settingsViewModel,
                onDismiss = {
                    showAddEditStudentDialog = false
                    editingStudent = null
                }
            )
        }

        if (showAddEditFurnitureDialog) {
            AddEditFurnitureDialog(
                furnitureToEdit = editingFurniture,
                viewModel = seatingChartViewModel,
                settingsViewModel = settingsViewModel,
                onDismiss = {
                    showAddEditFurnitureDialog = false
                    editingFurniture = null
                }
            )
        }

        if (showDataViewerDialog) {
            DataViewerScreen(
                seatingChartViewModel = seatingChartViewModel,
                onDismiss = { showDataViewerDialog = false }
            )
        }

        if (showExportFilterDialog) {
            ExportFilterDialog(
                viewModel = seatingChartViewModel,
                onDismissRequest = { showExportFilterDialog = false },
                onExport = { filterOptions ->
                    onPendingExportFilterOptionsChanged(filterOptions)
                    filteredExportLauncher.launch("seating_chart_filtered_export.xlsx")
                    showExportFilterDialog = false
                }
            )
        }

        if (showChangeBoxSizeDialog) {
            ChangeBoxSizeDialog(
                onDismissRequest = { showChangeBoxSizeDialog = false },
                onSave = { width, height ->
                    seatingChartViewModel.changeBoxSize(selectedStudentIds, width, height)
                    showChangeBoxSizeDialog = false
                }
            )
        }
    }
}