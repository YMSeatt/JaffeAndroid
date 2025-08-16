package com.example.myapplication.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Updated import
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.StudentRepository
import com.example.myapplication.data.QuizMarkType
import com.example.myapplication.preferences.AppTheme
import com.example.myapplication.ui.dialogs.ChangePasswordDialog
import com.example.myapplication.ui.dialogs.SetPasswordDialog
import com.example.myapplication.utils.ExcelImportUtil
import com.example.myapplication.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import androidx.compose.runtime.livedata.observeAsState

@OptIn(ExperimentalMaterial3Api::class)
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

    val recentLogsLimit by settingsViewModel.recentLogsLimit.collectAsState()
    val recentBehaviorIncidentsLimit by settingsViewModel.recentBehaviorIncidentsLimit.collectAsState()
    val useInitialsForBehavior by settingsViewModel.useInitialsForBehavior.collectAsState()
    val appTheme by settingsViewModel.appTheme.collectAsState()
    val passwordEnabled by settingsViewModel.passwordEnabled.collectAsState()

    var showSetPasswordDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    val behaviorTypesList by settingsViewModel.customBehaviors.observeAsState(initial = emptyList())
    var newBehaviorType by remember { mutableStateOf("") }
    var behaviorTypeError by remember { mutableStateOf<String?>(null) }

    val homeworkAssignmentTypesList by settingsViewModel.customHomeworkTypes.observeAsState(initial = emptyList())
    var newHomeworkAssignmentType by remember { mutableStateOf("") }
    var homeworkAssignmentTypeError by remember { mutableStateOf<String?>(null) }

    val homeworkStatusesList by settingsViewModel.customHomeworkStatuses.observeAsState(initial = emptyList())
    var newHomeworkStatus by remember { mutableStateOf("") }
    var homeworkStatusError by remember { mutableStateOf<String?>(null) }

    val quizMarkTypesList by settingsViewModel.quizMarkTypes.observeAsState(initial = emptyList())
    var newQuizMarkTypeName by remember { mutableStateOf("") }
    var newQuizMarkDefaultPoints by remember { mutableStateOf("") }
    var newQuizMarkContributesToTotal by remember { mutableStateOf(false) }
    var newQuizMarkIsExtraCredit by remember { mutableStateOf(false) }
    var quizMarkTypeNameError by remember { mutableStateOf<String?>(null) }
    var quizMarkPointsError by remember { mutableStateOf<String?>(null) }

    val defaultWidth by settingsViewModel.defaultStudentBoxWidth.collectAsState()
    val defaultHeight by settingsViewModel.defaultStudentBoxHeight.collectAsState()
    val defaultBgColor by settingsViewModel.defaultStudentBoxBackgroundColor.collectAsState()
    val defaultOutlineColor by settingsViewModel.defaultStudentBoxOutlineColor.collectAsState()
    val defaultTextColor by settingsViewModel.defaultStudentBoxTextColor.collectAsState()
    val defaultOutlineThickness by settingsViewModel.defaultStudentBoxOutlineThickness.collectAsState()

    var defaultWidthInput by remember(defaultWidth) { mutableStateOf(defaultWidth.toString()) }
    var defaultHeightInput by remember(defaultHeight) { mutableStateOf(defaultHeight.toString()) }
    var defaultBgColorInput by remember(defaultBgColor) { mutableStateOf(defaultBgColor) }
    var defaultOutlineColorInput by remember(defaultOutlineColor) { mutableStateOf(defaultOutlineColor) }
    var defaultTextColorInput by remember(defaultTextColor) { mutableStateOf(defaultTextColor) }
    var defaultOutlineThicknessInput by remember(defaultOutlineThickness) { mutableStateOf(defaultOutlineThickness.toString()) }

    val importStudentsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                coroutineScope.launch {
                    ExcelImportUtil.importStudentsFromExcel(it, context, studentRepository)
                }
            }
        }
    )

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri: Uri? ->
            uri?.let {
                coroutineScope.launch {
                    settingsViewModel.backupDatabase(it)
                }
            }
        }
    )

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                coroutineScope.launch {
                    settingsViewModel.restoreDatabase(it)
                }
            }
        }
    )

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
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Button(onClick = onNavigateToStudentGroups, modifier = Modifier.fillMaxWidth()) {
                    Text("Manage Student Groups")
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onNavigateToConditionalFormatting, modifier = Modifier.fillMaxWidth()) {
                    Text("Manage Conditional Formatting Rules")
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onNavigateToExport, modifier = Modifier.fillMaxWidth()) {
                    Text("Export Data")
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { importStudentsLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Import Students from Excel")
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { backupLauncher.launch("seating_chart_backup.db") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Backup Database")
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { restoreLauncher.launch("application/octet-stream") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Restore Database")
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable Password Protection", modifier = Modifier.weight(1f))
                    Switch(checked = passwordEnabled, onCheckedChange = { 
                        if (it) {
                            showSetPasswordDialog = true
                        } else {
                            settingsViewModel.updatePasswordEnabled(false)
                        }
                    })
                }
                Button(onClick = { showChangePasswordDialog = true }, enabled = passwordEnabled) {
                    Text("Change Password")
                }
                HorizontalDivider()
            }

            item {
                Text("Number of recent homework logs to display:", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        value = recentLogsLimit.toFloat(),
                        onValueChange = { settingsViewModel.updateRecentLogsLimit(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(recentLogsLimit.toString())
                }
                HorizontalDivider()
            }

            item {
                Text("Number of recent behavior incidents to display:", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        value = recentBehaviorIncidentsLimit.toFloat(),
                        onValueChange = { settingsViewModel.updateRecentBehaviorIncidentsLimit(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(recentBehaviorIncidentsLimit.toString())
                }
                HorizontalDivider()
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Use initials for behavior logs (e.g., OOS)", modifier = Modifier.weight(1f))
                    Switch(
                        checked = useInitialsForBehavior,
                        onCheckedChange = { settingsViewModel.updateUseInitialsForBehavior(it) }
                    )
                }
                HorizontalDivider()
            }

            item {
                Text("App Theme:", style = MaterialTheme.typography.titleMedium)
                AppTheme.entries.forEach { theme ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = theme.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }, modifier = Modifier.padding(start = 8.dp))
                        RadioButton(
                            selected = (appTheme == theme),
                            onClick = { settingsViewModel.updateAppTheme(theme) }
                        )
                    }
                }
                HorizontalDivider()
            }

            item {
                Text("Default Student Box Appearance", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = defaultWidthInput,
                    onValueChange = {
                        defaultWidthInput = it
                        it.toIntOrNull()?.let { value -> settingsViewModel.updateDefaultStudentBoxWidth(value) }
                    },
                    label = { Text("Default Box Width (dp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = defaultHeightInput,
                    onValueChange = {
                        defaultHeightInput = it
                        it.toIntOrNull()?.let { value -> settingsViewModel.updateDefaultStudentBoxHeight(value) }
                    },
                    label = { Text("Default Box Height (dp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = defaultBgColorInput,
                    onValueChange = {
                        defaultBgColorInput = it
                        if (it.startsWith("#") && it.length == 7 || it.length == 9) {
                            settingsViewModel.updateDefaultStudentBoxBackgroundColor(it)
                        }
                    },
                    label = { Text("Default Background Color (e.g., #RRGGBB)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = defaultOutlineColorInput,
                    onValueChange = {
                        defaultOutlineColorInput = it
                        if (it.startsWith("#") && it.length == 7 || it.length == 9) {
                           settingsViewModel.updateDefaultStudentBoxOutlineColor(it)
                        }
                    },
                    label = { Text("Default Outline Color (e.g., #RRGGBB)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = defaultTextColorInput,
                    onValueChange = {
                        defaultTextColorInput = it
                        if (it.startsWith("#") && it.length == 7 || it.length == 9) {
                            settingsViewModel.updateDefaultStudentBoxTextColor(it)
                        }
                    },
                    label = { Text("Default Text Color (e.g., #RRGGBB)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = defaultOutlineThicknessInput,
                    onValueChange = {
                        defaultOutlineThicknessInput = it
                        it.toIntOrNull()?.let { value -> settingsViewModel.updateDefaultStudentBoxOutlineThickness(value.coerceIn(0, 10)) } 
                    },
                    label = { Text("Default Box Outline Thickness (dp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider(Modifier.padding(top = 16.dp))
            }

            item {
                Text("Manage Behavior Types", style = MaterialTheme.typography.titleMedium)
            }
            items(behaviorTypesList) { type ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(type.name)
                    IconButton(onClick = {
                        settingsViewModel.deleteCustomBehavior(type)
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remove behavior type")
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = newBehaviorType,
                    onValueChange = { newBehaviorType = it; behaviorTypeError = null },
                    label = { Text("New behavior type") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = behaviorTypeError != null
                )
                behaviorTypeError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Button(onClick = {
                    if (newBehaviorType.isNotBlank()) {
                        settingsViewModel.addCustomBehavior(newBehaviorType.trim())
                        newBehaviorType = ""
                        behaviorTypeError = null
                    } else {
                        behaviorTypeError = "Name cannot be blank."
                    }
                }, enabled = newBehaviorType.isNotBlank()) {
                    Text("Add Behavior Type")
                }
                HorizontalDivider(Modifier.padding(top = 8.dp))
            }

            item {
                Text("Manage Homework Assignment Names/Types", style = MaterialTheme.typography.titleMedium)
            }
            items(homeworkAssignmentTypesList) { type ->
                 Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(type.name)
                    IconButton(onClick = {
                        settingsViewModel.deleteCustomHomeworkType(type)
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remove homework assignment type")
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = newHomeworkAssignmentType,
                    onValueChange = { newHomeworkAssignmentType = it; homeworkAssignmentTypeError = null },
                    label = { Text("New homework assignment type") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = homeworkAssignmentTypeError != null
                )
                homeworkAssignmentTypeError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Button(onClick = {
                    if (newHomeworkAssignmentType.isNotBlank()) {
                        settingsViewModel.addCustomHomeworkType(newHomeworkAssignmentType.trim())
                        newHomeworkAssignmentType = ""
                        homeworkAssignmentTypeError = null
                    } else {
                        homeworkAssignmentTypeError = "Name cannot be blank."
                    }
                }, enabled = newHomeworkAssignmentType.isNotBlank()) {
                    Text("Add Homework Assignment Type")
                }
                HorizontalDivider(Modifier.padding(top = 8.dp))
            }

            item {
                Text("Manage Homework Statuses", style = MaterialTheme.typography.titleMedium)
            }
            items(homeworkStatusesList) { status ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(status.name)
                    IconButton(onClick = {
                        settingsViewModel.deleteCustomHomeworkStatus(status)
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remove homework status")
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = newHomeworkStatus,
                    onValueChange = { newHomeworkStatus = it; homeworkStatusError = null },
                    label = { Text("New homework status") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = homeworkStatusError != null
                )
                homeworkStatusError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Button(onClick = {
                    if (newHomeworkStatus.isNotBlank()) {
                        settingsViewModel.addCustomHomeworkStatus(newHomeworkStatus.trim())
                        newHomeworkStatus = ""
                        homeworkStatusError = null
                    } else {
                        homeworkStatusError = "Status cannot be blank."
                    }
                }, enabled = newHomeworkStatus.isNotBlank()) {
                    Text("Add Homework Status")
                }
                HorizontalDivider(Modifier.padding(top = 8.dp))
            }

            item {
                Text("Manage Quiz Mark Types", style = MaterialTheme.typography.titleMedium)
            }
            items(quizMarkTypesList) { markType ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(markType.name, style = MaterialTheme.typography.bodyLarge)
                            IconButton(onClick = {
                                settingsViewModel.deleteQuizMarkType(markType)
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove quiz mark type")
                            }
                        }
                        Text("Points: ${markType.defaultPoints}")
                        Text("Contributes to total: ${if (markType.contributesToTotal) "Yes" else "No"}")
                        Text("Extra credit: ${if (markType.isExtraCredit) "Yes" else "No"}")
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text("Add New Quiz Mark Type:", style = MaterialTheme.typography.titleSmall)
                OutlinedTextField(
                    value = newQuizMarkTypeName,
                    onValueChange = { newQuizMarkTypeName = it; quizMarkTypeNameError = null },
                    label = { Text("Mark type name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = quizMarkTypeNameError != null
                )
                quizMarkTypeNameError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = newQuizMarkDefaultPoints,
                    onValueChange = { newQuizMarkDefaultPoints = it; quizMarkPointsError = null },
                    label = { Text("Default points") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = quizMarkPointsError != null
                )
                quizMarkPointsError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Contributes to total score", modifier = Modifier.weight(1f))
                    Switch(
                        checked = newQuizMarkContributesToTotal,
                        onCheckedChange = { newQuizMarkContributesToTotal = it }
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Is extra credit", modifier = Modifier.weight(1f))
                    Switch(
                        checked = newQuizMarkIsExtraCredit,
                        onCheckedChange = { newQuizMarkIsExtraCredit = it }
                    )
                }
                Button(
                    onClick = {
                        var isValid = true
                        if (newQuizMarkTypeName.isBlank()) {
                            quizMarkTypeNameError = "Name cannot be blank."
                            isValid = false
                        } else {
                            quizMarkTypeNameError = null
                        }

                        val pointsValue = newQuizMarkDefaultPoints.toDoubleOrNull()
                        if (pointsValue == null) {
                            quizMarkPointsError = "Points must be a valid number."
                            isValid = false
                        } else {
                            quizMarkPointsError = null
                        }

                        if (isValid && pointsValue != null) {
                            val newMarkType = QuizMarkType(
                                name = newQuizMarkTypeName.trim(),
                                defaultPoints = pointsValue,
                                contributesToTotal = newQuizMarkContributesToTotal,
                                isExtraCredit = newQuizMarkIsExtraCredit
                            )
                            settingsViewModel.addQuizMarkType(newMarkType)
                            newQuizMarkTypeName = ""
                            newQuizMarkDefaultPoints = ""
                            newQuizMarkContributesToTotal = false
                            newQuizMarkIsExtraCredit = false
                            quizMarkTypeNameError = null
                            quizMarkPointsError = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = newQuizMarkTypeName.isNotBlank() && newQuizMarkDefaultPoints.toDoubleOrNull() != null
                ) {
                    Text("Add Quiz Mark Type")
                }
                HorizontalDivider(Modifier.padding(top = 8.dp))
            }
        }
    }
}
