package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri // Required for createDocumentLauncher
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState // Added for scrollable dialog
import androidx.compose.foundation.text.KeyboardOptions // Added for keyboard options
import androidx.compose.foundation.verticalScroll // Added for scrollable dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType // Added for keyboard options
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Student
import com.example.myapplication.preferences.AppTheme
import com.example.myapplication.ui.model.StudentUiItem // Import the new UI model
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.ExcelImportUtil // Corrected import path
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private val seatingChartViewModel: SeatingChartViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, now launch file creation
            createDocumentLauncher.launch("students.xlsx")
        } else {
            Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // ActivityResultLauncher for creating a document
    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
        uri?.let { // If URI is not null, proceed with export
            val studentsToExport = seatingChartViewModel.getAllStudentsForExport()
            if (studentsToExport.isNullOrEmpty()) {
                Toast.makeText(this, "No student data to export", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            lifecycleScope.launch {
                val result = ExcelImportUtil.exportToExcel(studentsToExport, this@MainActivity, it)
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
            MyApplicationTheme(
                darkTheme = when (currentAppThemeState) {
                    AppTheme.LIGHT -> false
                    AppTheme.DARK -> true
                    AppTheme.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
                SeatingChartScreen(
                    seatingChartViewModel = seatingChartViewModel,
                    settingsViewModel = settingsViewModel,
                    onExportClick = ::handleExportClick,
                    onNavigateToSettings = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    }
                )
            }
        }
    }

    private fun handleExportClick() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                 // Permission already granted, launch file creation
                createDocumentLauncher.launch("students.xlsx")
            }
            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                Toast.makeText(this, "Storage permission is needed to export data.", Toast.LENGTH_LONG).show()
                // Optionally, show a dialog explaining why permission is needed before requesting
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    // exportData function is now effectively handled by createDocumentLauncher's callback
    // The old exportData function is removed.
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatingChartScreen(
    seatingChartViewModel: SeatingChartViewModel,
    settingsViewModel: SettingsViewModel,
    onExportClick: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val students by seatingChartViewModel.studentsForDisplay.observeAsState(initial = emptyList())
    var showBehaviorDialog by remember { mutableStateOf(false) }
    var selectedStudentUiItemForBehavior by remember { mutableStateOf<StudentUiItem?>(null) }
    var showAddEditStudentDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) } 
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showThemeMenu by remember { mutableStateOf(false) }
    val behaviorTypesList by settingsViewModel.behaviorTypesList.collectAsState()
    val showRecentBehavior by settingsViewModel.showRecentBehavior.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seating Chart") },
                actions = {
                    IconButton(onClick = {
                        // Check if there are students to export from the dedicated ViewModel function
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
            FloatingActionButton(onClick = {
                editingStudent = null 
                showAddEditStudentDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Student")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            students.forEach { studentItem -> 
                StudentDraggableIcon(
                    studentUiItem = studentItem, 
                    viewModel = seatingChartViewModel,
                    showBehavior = showRecentBehavior, 
                    onClick = {
                        selectedStudentUiItemForBehavior = studentItem 
                        showBehaviorDialog = true
                    },
                    onLongClick = {
                        coroutineScope.launch {
                            editingStudent = seatingChartViewModel.getStudentForEditing(studentItem.id)
                            showAddEditStudentDialog = true
                        }
                    }
                )
            }
        }

        if (showBehaviorDialog && selectedStudentUiItemForBehavior != null) {
            val nameParts = selectedStudentUiItemForBehavior!!.fullName.split(" ", limit = 2)
            val firstNameForDialog = nameParts.getOrElse(0) { "" }
            val lastNameForDialog = nameParts.getOrElse(1) { "" }
            val studentForBehaviorDialog = Student(
                id = selectedStudentUiItemForBehavior!!.id,
                firstName = firstNameForDialog, 
                lastName = lastNameForDialog
            )
            BehaviorDialog(
                student = studentForBehaviorDialog,
                viewModel = seatingChartViewModel,
                behaviorTypes = behaviorTypesList.toList(),
                onDismiss = {
                    showBehaviorDialog = false
                    selectedStudentUiItemForBehavior = null
                }
            )
        }

        if (showAddEditStudentDialog) {
            AddEditStudentDialog(
                studentToEdit = editingStudent, 
                viewModel = seatingChartViewModel,
                onDismiss = { 
                    showAddEditStudentDialog = false
                    editingStudent = null 
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudentDraggableIcon(
    studentUiItem: StudentUiItem, 
    viewModel: SeatingChartViewModel,
    showBehavior: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(studentUiItem.xPosition.toFloat()) }
    var offsetY by remember { mutableFloatStateOf(studentUiItem.yPosition.toFloat()) }

    LaunchedEffect(studentUiItem.xPosition, studentUiItem.yPosition) {
        offsetX = studentUiItem.xPosition.toFloat()
        offsetY = studentUiItem.yPosition.toFloat()
    }

    Card(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(studentUiItem.id) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    },
                    onDragEnd = {
                        viewModel.updateStudentPosition(studentUiItem.id, offsetX, offsetY)
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
fun AddEditStudentDialog(
    studentToEdit: Student? = null, 
    viewModel: SeatingChartViewModel,
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

    LaunchedEffect(studentToEdit) { 
        firstName = studentToEdit?.firstName ?: ""
        lastName = studentToEdit?.lastName ?: ""
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
                        label = { Text("Custom Width (dp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customHeightInput,
                        onValueChange = { customHeightInput = it },
                        label = { Text("Custom Height (dp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customBgColorInput,
                        onValueChange = { customBgColorInput = it },
                        label = { Text("Custom Background Color (e.g., #AARRGGBB)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customOutlineColorInput,
                        onValueChange = { customOutlineColorInput = it },
                        label = { Text("Custom Outline Color (e.g., #AARRGGBB)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customTextColorInput,
                        onValueChange = { customTextColorInput = it },
                        label = { Text("Custom Text Color (e.g., #AARRGGBB)") },
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
                    val student = Student(
                        id = studentToEdit?.id ?: 0,
                        firstName = firstName,
                        lastName = lastName,
                        xPosition = studentToEdit?.xPosition ?: 0.0, 
                        yPosition = studentToEdit?.yPosition ?: 0.0, 
                        customWidth = if (useCustomAppearance) customWidthInput.toIntOrNull() else null,
                        customHeight = if (useCustomAppearance) customHeightInput.toIntOrNull() else null,
                        customBackgroundColor = if (useCustomAppearance && customBgColorInput.isNotBlank()) customBgColorInput.trim() else null,
                        customOutlineColor = if (useCustomAppearance && customOutlineColorInput.isNotBlank()) customOutlineColorInput.trim() else null,
                        customTextColor = if (useCustomAppearance && customTextColorInput.isNotBlank()) customTextColorInput.trim() else null
                    )
                    if (isEditMode) {
                        viewModel.updateStudent(student) 
                    } else {
                        viewModel.addStudent(student) 
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
                            studentToEdit?.let { viewModel.deleteStudent(it) } 
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
