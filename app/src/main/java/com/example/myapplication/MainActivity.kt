package com.example.myapplication

// import androidx.compose.foundation.lazy.items // Removed unused import
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Furniture
import com.example.myapplication.data.LayoutTemplate
import com.example.myapplication.data.Student
import com.example.myapplication.data.StudentGroup
import com.example.myapplication.preferences.AppTheme
import com.example.myapplication.ui.PasswordScreen
import com.example.myapplication.ui.dialogs.AdvancedHomeworkLogDialog
import com.example.myapplication.ui.dialogs.LogQuizScoreDialog
import com.example.myapplication.ui.model.FurnitureUiItem
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.ExcelImportUtil
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import com.example.myapplication.viewmodel.StudentGroupsViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

enum class SessionType {
    NONE,
    QUIZ,
    HOMEWORK
}

class MainActivity : ComponentActivity() {
    private val seatingChartViewModel: SeatingChartViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            createDocumentLauncher.launch("students.xlsx")
        } else {
            Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
        uri?.let {
            val studentsToExport = seatingChartViewModel.getAllStudentsForExport()
            if (studentsToExport.isNullOrEmpty()) {
                Toast.makeText(this, "No student data to export", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            lifecycleScope.launch {
                val result = ExcelImportUtil.exportData(this@MainActivity, it, studentsToExport, null, null, null)
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
                        onExportClick = ::handleExportClick,
                        onNavigateToSettings = {
                            startActivity(Intent(this, SettingsActivity::class.java))
                        }
                    )
                } else {
                    PasswordScreen(settingsViewModel = settingsViewModel) {
                        unlocked = true
                    }
                }
            }
        }
    }

    private fun handleExportClick() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                createDocumentLauncher.launch("students.xlsx")
            }
            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                Toast.makeText(this, "Storage permission is needed to export data.", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SeatingChartScreen(
    seatingChartViewModel: SeatingChartViewModel,
    settingsViewModel: SettingsViewModel,
    studentGroupsViewModel: StudentGroupsViewModel,
    onExportClick: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val students by seatingChartViewModel.studentsForDisplay.observeAsState(initial = emptyList())
    val furniture by seatingChartViewModel.furnitureForDisplay.observeAsState(initial = emptyList())
    val layouts by seatingChartViewModel.allLayoutTemplates.observeAsState(initial = emptyList())
    
    var showBehaviorDialog by remember { mutableStateOf(false) }
    var showLogQuizScoreDialog by remember { mutableStateOf(false) }
    var showAdvancedHomeworkLogDialog by remember { mutableStateOf(false) }
    var showStudentActionMenu by remember { mutableStateOf(false) }
    var showSaveLayoutDialog by remember { mutableStateOf(false) }
    var showLoadLayoutDialog by remember { mutableStateOf(false) }

    var selectedStudentUiItemForAction by remember { mutableStateOf<StudentUiItem?>(null) }
    
    var showAddEditStudentDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var showAddEditFurnitureDialog by remember { mutableStateOf(false) }
    var editingFurniture by remember { mutableStateOf<Furniture?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showThemeMenu by remember { mutableStateOf(false) } // Fixed typo: mutableStateOF -> mutableStateOf

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val behaviorTypes by settingsViewModel.customBehaviors.observeAsState(initial = emptyList()) // Corrected to observe LiveData
    val behaviorTypeNames = remember(behaviorTypes) { behaviorTypes.map { it.name } } // Extract names
    val showRecentBehavior by settingsViewModel.showRecentBehavior.collectAsState(initial = false) // Explicit initial value

    var sessionType by remember { mutableStateOf(SessionType.NONE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seating Chart") },
                actions = {
                    IconButton(onClick = { seatingChartViewModel.undo() }) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                    }
                    IconButton(onClick = { seatingChartViewModel.redo() }) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                    }

                    if (sessionType == SessionType.NONE) {
                        IconButton(onClick = { 
                            sessionType = SessionType.QUIZ 
                            seatingChartViewModel.startSession()
                        }) {
                            Text("Quiz")
                        }
                        IconButton(onClick = { 
                            sessionType = SessionType.HOMEWORK
                            seatingChartViewModel.startSession()
                        }) {
                            Text("HW")
                        }
                    } else {
                        Button(onClick = { 
                            sessionType = SessionType.NONE 
                            seatingChartViewModel.endSession()
                        }) {
                            Text("End Session")
                        }
                    }

                    IconButton(onClick = { showSaveLayoutDialog = true }) {
                        Text("Save")
                    }
                    IconButton(onClick = { showLoadLayoutDialog = true }) {
                        Text("Load")
                    }

                    IconButton(onClick = {
                        if (seatingChartViewModel.getAllStudentsForExport()?.isNotEmpty() == true) {
                            onExportClick()
                        } else {
                            Toast.makeText(context, "No student data to export", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Export to Excel")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                    Box {
                        IconButton(onClick = { showThemeMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Change Theme")
                        }
                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false }
                        ) {
                            AppTheme.entries.forEach { theme ->
                                DropdownMenuItem(
                                    text = { Text(theme.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }) },
                                    onClick = {
                                        settingsViewModel.updateAppTheme(theme)
                                        showThemeMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        onClick = { 
                            selectedStudentUiItemForAction = studentItem
                            when (sessionType) {
                                SessionType.NONE -> showStudentActionMenu = true
                                SessionType.QUIZ -> showLogQuizScoreDialog = true
                                SessionType.HOMEWORK -> showAdvancedHomeworkLogDialog = true
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
                                editingFurniture = seatingChartViewModel.getFurnitureById(furnitureItem.id)
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
                            showBehaviorDialog = true 
                        }) {
                            Text("Log Behavior")
                        }
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

        if (showBehaviorDialog && selectedStudentUiItemForAction != null) {
            val nameParts = selectedStudentUiItemForAction!!.fullName.split(" ", limit = 2)
            val firstNameForDialog = nameParts.getOrElse(0) { "" }
            val lastNameForDialog = nameParts.getOrElse(1) { "" }
            val studentForBehaviorDialog = Student(
                id = selectedStudentUiItemForAction!!.id.toLong(),
                firstName = firstNameForDialog, 
                lastName = lastNameForDialog
            )
            BehaviorDialog(
                student = studentForBehaviorDialog,
                viewModel = seatingChartViewModel, 
                behaviorTypes = behaviorTypeNames, // Pass extracted names
                onDismiss = {
                    showBehaviorDialog = false
                    selectedStudentUiItemForAction = null 
                }
            )
        }

        if (showLogQuizScoreDialog && selectedStudentUiItemForAction != null) {
            LogQuizScoreDialog(
                studentId = selectedStudentUiItemForAction!!.id.toLong(),
                viewModel = seatingChartViewModel,
                onDismissRequest = {
                    showLogQuizScoreDialog = false
                    selectedStudentUiItemForAction = null
                },
                onSave = { quizLog ->
                    if (sessionType == SessionType.QUIZ) {
                        seatingChartViewModel.addQuizLogToSession(quizLog)
                    } else {
                        seatingChartViewModel.saveQuizLog(quizLog)
                    }
                }
            )
        }

        if (showAdvancedHomeworkLogDialog && selectedStudentUiItemForAction != null) {
            AdvancedHomeworkLogDialog(
                studentId = selectedStudentUiItemForAction!!.id.toLong(),
                viewModel = seatingChartViewModel,
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
                studentGroupsViewModel = studentGroupsViewModel, // Pass StudentGroupsViewModel
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
                onDismiss = {
                    showAddEditFurnitureDialog = false
                    editingFurniture = null
                }
            )
        }
    }
}

@Composable
fun SaveLayoutDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Layout") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Layout Name") }
            )
        },
        confirmButton = {
            Button(onClick = { onSave(name) }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LoadLayoutDialog(
    layouts: List<LayoutTemplate>,
    onDismiss: () -> Unit,
    onLoad: (LayoutTemplate) -> Unit,
    onDelete: (LayoutTemplate) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Load Layout") },
        text = {
            LazyColumn {
                items(layouts.size) { index -> // Changed to use items(count)
                    val layout = layouts[index] // Get layout by index
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onLoad(layout) },
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(layout.name)
                        IconButton(onClick = { onDelete(layout) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Layout")
                        }
                    }
                }
            }
        },
        confirmButton = { }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FurnitureDraggableIcon(
    furnitureUiItem: FurnitureUiItem,
    viewModel: SeatingChartViewModel,
    scale: Float,
    onLongClick: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(furnitureUiItem.xPosition) }
    var offsetY by remember { mutableFloatStateOf(furnitureUiItem.yPosition) }

    LaunchedEffect(furnitureUiItem.xPosition, furnitureUiItem.yPosition) {
        offsetX = furnitureUiItem.xPosition
        offsetY = furnitureUiItem.yPosition
    }

    key(furnitureUiItem) {
        Card(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(furnitureUiItem.id) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x / scale
                            offsetY += dragAmount.y / scale
                        },
                        onDragEnd = {
                            viewModel.updateFurniturePosition(furnitureUiItem.id, offsetX, offsetY)
                        }
                    )
                }
                .combinedClickable(
                    onClick = { /* Furniture might not have a default click action */ },
                    onLongClick = onLongClick
                )
                .width(furnitureUiItem.displayWidth)
                .height(furnitureUiItem.displayHeight)
                .border(BorderStroke(furnitureUiItem.displayOutlineThickness, furnitureUiItem.displayOutlineColor)),
            colors = CardDefaults.cardColors(containerColor = furnitureUiItem.displayBackgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(4.dp)
            ) {
                Text(
                    text = furnitureUiItem.name,
                    color = furnitureUiItem.displayTextColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudentDraggableIcon(
    studentUiItem: StudentUiItem,
    viewModel: SeatingChartViewModel,
    showBehavior: Boolean,
    scale: Float,
    onClick: () -> Unit, 
    onLongClick: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(studentUiItem.xPosition.toFloat()) } // Changed to mutableFloatStateOf
    var offsetY by remember { mutableFloatStateOf(studentUiItem.yPosition.toFloat()) } // Changed to mutableFloatStateOf

    LaunchedEffect(studentUiItem.xPosition, studentUiItem.yPosition) {
        offsetX = studentUiItem.xPosition.toFloat()
        offsetY = studentUiItem.yPosition.toFloat()
    }

    key(studentUiItem) {
        Card(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(studentUiItem.id) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x / scale
                            offsetY += dragAmount.y / scale
                        },
                        onDragEnd = {
                            viewModel.updateStudentPosition(
                                studentUiItem.id,
                                studentUiItem.xPosition.toFloat(), // This should be the original X
                                studentUiItem.yPosition.toFloat(), // This should be the original Y
                                offsetX,
                                offsetY
                            )
                        }
                    )
                }
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .width(studentUiItem.displayWidth)
                .height(studentUiItem.displayHeight)
                .border(BorderStroke(studentUiItem.displayOutlineThickness, studentUiItem.displayOutlineColor)),
            colors = CardDefaults.cardColors(containerColor = studentUiItem.displayBackgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = studentUiItem.initials,
                        color = studentUiItem.displayTextColor
                    )
                    if (showBehavior) {
                        studentUiItem.recentBehaviorDescription?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                color = studentUiItem.displayTextColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BehaviorDialog(
    student: Student, 
    viewModel: SeatingChartViewModel,
    behaviorTypes: List<String>,
    onDismiss: () -> Unit
) {
    var selectedBehavior by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Log Behavior for ${student.firstName}") },
        text = {
            Column {
                if (behaviorTypes.isEmpty()) {
                    Text("No behavior types defined. Please add them in Settings.")
                } else {
                    behaviorTypes.forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = (selectedBehavior == option),
                                onClick = { selectedBehavior = option }
                            )
                            Text(text = option, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
                if (selectedBehavior.isNotBlank()) {
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Comment (Optional)") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedBehavior.isNotEmpty()) {
                        val event = BehaviorEvent(
                            studentId = student.id,
                            type = selectedBehavior,
                            timestamp = System.currentTimeMillis(),
                            comment = comment.ifBlank { null }
                        )
                        viewModel.addBehaviorEvent(event)
                        onDismiss()
                    }
                },
                enabled = selectedBehavior.isNotBlank() && behaviorTypes.isNotEmpty() 
            ) {
                Text("Log Event")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFurnitureDialog(
    furnitureToEdit: Furniture?,
    viewModel: SeatingChartViewModel,
    onDismiss: () -> Unit
) {
    var name by remember(furnitureToEdit?.id) { mutableStateOf(furnitureToEdit?.name ?: "") }
    var type by remember(furnitureToEdit?.id) { mutableStateOf(furnitureToEdit?.type ?: "Desk") }
    var width by remember(furnitureToEdit?.id) { mutableStateOf(furnitureToEdit?.width?.toString() ?: "100") }
    var height by remember(furnitureToEdit?.id) { mutableStateOf(furnitureToEdit?.height?.toString() ?: "60") }
    val context = LocalContext.current
    val isEditMode = furnitureToEdit != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditMode) "Edit Furniture" else "Add Furniture") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    isError = name.isBlank()
                )
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Type (e.g., Desk, Bookshelf)") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = width,
                        onValueChange = { width = it },
                        label = { Text("Width (dp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("Height (dp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val widthInt = width.toIntOrNull()
                    val heightInt = height.toIntOrNull()
                    if (name.isBlank() || widthInt == null || heightInt == null) {
                        Toast.makeText(context, "Please fill all fields correctly.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val updatedFurniture = Furniture( // Renamed to avoid confusion
                        id = furnitureToEdit?.id ?: 0,
                        name = name,
                        type = type.ifBlank { "Furniture" },
                        width = widthInt,
                        height = heightInt,
                        xPosition = furnitureToEdit?.xPosition ?: 50f,
                        yPosition = furnitureToEdit?.yPosition ?: 50f
                    )
                    if (isEditMode) {
                        // Pass the updated furniture object
                        viewModel.updateFurniture(furnitureToEdit!!, updatedFurniture)
                    } else {
                        viewModel.addFurniture(updatedFurniture)
                    }
                    onDismiss()
                }
            ) {
                Text(if (isEditMode) "Save" else "Add")
            }
        },
        dismissButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isEditMode) {
                    Button(
                        onClick = {
                            furnitureToEdit?.let {
                                viewModel.deleteFurnitureById(it.id.toLong())
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Furniture")
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                }
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditStudentDialog(
    studentToEdit: Student? = null,
    viewModel: SeatingChartViewModel,
    studentGroupsViewModel: StudentGroupsViewModel, // Added StudentGroupsViewModel
    onDismiss: () -> Unit
) {
    var firstName by remember(studentToEdit?.id) { mutableStateOf(studentToEdit?.firstName ?: "") }
    var lastName by remember(studentToEdit?.id) { mutableStateOf(studentToEdit?.lastName ?: "") }
    val context = LocalContext.current
    val isEditMode = studentToEdit != null

    var useCustomAppearance by remember(studentToEdit?.id) { 
        mutableStateOf(studentToEdit?.customWidth != null || 
                       studentToEdit?.customHeight != null || 
                       studentToEdit?.customBackgroundColor?.isNotBlank() == true || 
                       studentToEdit?.customOutlineColor?.isNotBlank() == true || 
                       studentToEdit?.customTextColor?.isNotBlank() == true)
    }
    var customWidthInput by remember(studentToEdit?.id) { mutableStateOf(studentToEdit?.customWidth?.toString() ?: "") }
    var customHeightInput by remember(studentToEdit?.id) { mutableStateOf(studentToEdit?.customHeight?.toString() ?: "") }
    var customBgColorInput by remember(studentToEdit?.id) { mutableStateOf(studentToEdit?.customBackgroundColor ?: "") }
    var customOutlineColorInput by remember(studentToEdit?.id) { mutableStateOf(studentToEdit?.customOutlineColor ?: "") }
    var customTextColorInput by remember(studentToEdit?.id) { mutableStateOf(studentToEdit?.customTextColor ?: "") }

    // Group selection states
    val studentGroups by studentGroupsViewModel.studentGroups
        .collectAsState(initial = emptyList()) 
    var selectedGroupId by remember(studentToEdit?.groupId) { mutableStateOf(studentToEdit?.groupId) }
    var groupDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(studentToEdit) { 
        firstName = studentToEdit?.firstName ?: ""
        lastName = studentToEdit?.lastName ?: ""
        selectedGroupId = studentToEdit?.groupId // Make sure groupId is Long? in Student entity
        useCustomAppearance = studentToEdit?.customWidth != null || 
                              studentToEdit?.customHeight != null || 
                              studentToEdit?.customBackgroundColor?.isNotBlank() == true || 
                              studentToEdit?.customOutlineColor?.isNotBlank() == true || 
                              studentToEdit?.customTextColor?.isNotBlank() == true
        customWidthInput = studentToEdit?.customWidth?.toString() ?: ""
        customHeightInput = studentToEdit?.customHeight?.toString() ?: ""
        customBgColorInput = studentToEdit?.customBackgroundColor ?: ""
        customOutlineColorInput = studentToEdit?.customOutlineColor ?: ""
        customTextColorInput = studentToEdit?.customTextColor ?: ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditMode) "Edit Student" else "Add New Student") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    isError = firstName.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    isError = lastName.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Group Selection Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = groupDropdownExpanded,
                        onExpandedChange = { groupDropdownExpanded = !groupDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = studentGroups.find { group: StudentGroup -> group.id == selectedGroupId }?.name ?: "Select Group (Optional)",
                            onValueChange = { }, // Not directly editable, changed by dropdown
                            label = { Text("Group") },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        )
                        ExposedDropdownMenu(
                            expanded = groupDropdownExpanded,
                            onDismissRequest = { groupDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth() 
                        ) {
                            studentGroups.forEach { group ->
                                DropdownMenuItem(
                                    text = { 
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            group.color?.let { 
                                                Box(modifier = Modifier.size(16.dp).background(Color(android.graphics.Color.parseColor(it)))) 
                                            }
                                            Spacer(Modifier.width(8.dp))
                                            Text(group.name)
                                        }
                                    },
                                    onClick = {
                                        selectedGroupId = group.id
                                        groupDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Use Custom Appearance", modifier = Modifier.weight(1f))
                    Switch(
                        checked = useCustomAppearance,
                        onCheckedChange = { useCustomAppearance = it }
                    )
                }

                if (useCustomAppearance) {
                    OutlinedTextField(
                        value = customWidthInput,
                        onValueChange = { customWidthInput = it },
                        label = { Text("Custom Width (dp - default is 120)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customHeightInput,
                        onValueChange = { customHeightInput = it },
                        label = { Text("Custom Height (dp - default is 100") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth() // Changed from weight(1f)
                    )
                    OutlinedTextField(
                        value = customBgColorInput,
                        onValueChange = { customBgColorInput = it },
                        label = { Text("Custom Background Color (e.g., #AARRGGBB - default is #FFFFFFFF)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customOutlineColorInput,
                        onValueChange = { customOutlineColorInput = it },
                        label = { Text("Custom Outline Color (e.g., #AARRGGBB - default is #FF000000)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customTextColorInput,
                        onValueChange = { customTextColorInput = it },
                        label = { Text("Custom Text Color (e.g., #AARRGGBB - default is #FF000000)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (firstName.isBlank() || lastName.isBlank()) {
                        Toast.makeText(context, "Please fill all fields correctly.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val newStudent = Student(
                        id = studentToEdit?.id ?: 0,
                        firstName = firstName,
                        lastName = lastName,
                        xPosition = studentToEdit?.xPosition ?: 0.0F,
                        yPosition = studentToEdit?.yPosition ?: 0.0F,
                        groupId = selectedGroupId, // Save selected group ID
                        customWidth = if (useCustomAppearance) customWidthInput.toIntOrNull() else null,
                        customHeight = if (useCustomAppearance) customHeightInput.toIntOrNull() else null,
                        customBackgroundColor = if (useCustomAppearance && customBgColorInput.isNotBlank()) customBgColorInput.trim() else null,
                        customOutlineColor = if (useCustomAppearance && customOutlineColorInput.isNotBlank()) customOutlineColorInput.trim() else null,
                        customTextColor = if (useCustomAppearance && customTextColorInput.isNotBlank()) customTextColorInput.trim() else null
                    )
                    if (isEditMode) {
                        viewModel.updateStudent(studentToEdit, newStudent) 
                    } else {
                        viewModel.addStudent(newStudent)
                    }
                    onDismiss()
                }
            ) {
                Text(if (isEditMode) "Save Changes" else "Add Student")
            }
        },
        dismissButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isEditMode) {
                    Button(
                        onClick = {
                            studentToEdit?.let {
                                viewModel.deleteStudent(it)
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Student")
                        Spacer(Modifier.width(4.dp))
                        Text("Delete")
                    }
                } else {
                    Spacer(Modifier.weight(1f)) 
                }
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}