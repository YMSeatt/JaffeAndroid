package com.example.myapplication.ui.settings

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.preferences.AppTheme
import com.example.myapplication.preferences.QuizMarkTypeSetting
import com.example.myapplication.viewmodel.SettingsViewModel
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val recentLogsLimit by settingsViewModel.recentLogsLimit.collectAsState()
    // New state for recent behavior incidents limit
    val recentBehaviorIncidentsLimit by settingsViewModel.recentBehaviorIncidentsLimit.collectAsState()
    val useInitialsForBehavior by settingsViewModel.useInitialsForBehavior.collectAsState()
    val appTheme by settingsViewModel.appTheme.collectAsState()

    val behaviorTypesList by settingsViewModel.behaviorTypesList.collectAsState()
    var newBehaviorType by remember { mutableStateOf("") }
    var behaviorTypeError by remember { mutableStateOf<String?>(null) }

    val homeworkAssignmentTypesList by settingsViewModel.homeworkAssignmentTypesList.collectAsState()
    var newHomeworkAssignmentType by remember { mutableStateOf("") }
    var homeworkAssignmentTypeError by remember { mutableStateOf<String?>(null) }

    val homeworkStatusesList by settingsViewModel.homeworkStatusesList.collectAsState()
    var newHomeworkStatus by remember { mutableStateOf("") }
    var homeworkStatusError by remember { mutableStateOf<String?>(null) }

    val quizMarkTypesList by settingsViewModel.quizMarkTypesList.collectAsState()
    var newQuizMarkTypeName by remember { mutableStateOf("") }
    var newQuizMarkDefaultPoints by remember { mutableStateOf("") }
    var newQuizMarkContributesToTotal by remember { mutableStateOf(false) }
    var newQuizMarkIsExtraCredit by remember { mutableStateOf(false) }
    var quizMarkTypeNameError by remember { mutableStateOf<String?>(null) }
    var quizMarkPointsError by remember { mutableStateOf<String?>(null) }

    // Collect states for default student box appearance
    val defaultWidth by settingsViewModel.defaultStudentBoxWidth.collectAsState()
    val defaultHeight by settingsViewModel.defaultStudentBoxHeight.collectAsState()
    val defaultBgColor by settingsViewModel.defaultStudentBoxBackgroundColor.collectAsState()
    val defaultOutlineColor by settingsViewModel.defaultStudentBoxOutlineColor.collectAsState()
    val defaultTextColor by settingsViewModel.defaultStudentBoxTextColor.collectAsState()
    val defaultOutlineThickness by settingsViewModel.defaultStudentBoxOutlineThickness.collectAsState() // New state

    // Local states for text field inputs for student box appearance
    var defaultWidthInput by remember(defaultWidth) { mutableStateOf(defaultWidth.toString()) }
    var defaultHeightInput by remember(defaultHeight) { mutableStateOf(defaultHeight.toString()) }
    var defaultBgColorInput by remember(defaultBgColor) { mutableStateOf(defaultBgColor) }
    var defaultOutlineColorInput by remember(defaultOutlineColor) { mutableStateOf(defaultOutlineColor) }
    var defaultTextColorInput by remember(defaultTextColor) { mutableStateOf(defaultTextColor) }
    var defaultOutlineThicknessInput by remember(defaultOutlineThickness) { mutableStateOf(defaultOutlineThickness.toString()) } // New input state


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") // Updated icon
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
            // Recent Homework Logs Limit Setting
            item {
                Text("Number of recent homework logs to display:", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        value = recentLogsLimit.toFloat(),
                        onValueChange = { settingsViewModel.updateRecentLogsLimit(it.toInt()) },
                        valueRange = 1f..10f, // Adjusted range if needed
                        steps = 8, // for 1-10, means 9 steps (10-1 = 9)
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(recentLogsLimit.toString())
                }
                HorizontalDivider()
            }

            // Recent Behavior Incidents Limit Setting - NEW
            item {
                Text("Number of recent behavior incidents to display:", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        value = recentBehaviorIncidentsLimit.toFloat(),
                        onValueChange = { settingsViewModel.updateRecentBehaviorIncidentsLimit(it.toInt()) },
                        valueRange = 1f..10f, // Example range, adjust as needed
                        steps = 8,      // For 1-10, means 9 steps (10-1 = 9)
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(recentBehaviorIncidentsLimit.toString())
                }
                HorizontalDivider()
            }

            // Use Initials for Behavior Setting
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

            // App Theme Setting
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

            // Default Student Box Appearance Settings
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
                        // Basic validation for hex-like, can be improved
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
                        it.toIntOrNull()?.let { value -> settingsViewModel.updateDefaultStudentBoxOutlineThickness(value.coerceIn(0, 10)) } // Coerce to a reasonable range e.g. 0-10dp
                    },
                    label = { Text("Default Box Outline Thickness (dp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider(Modifier.padding(top = 16.dp))
            }

            // Manage Behavior Types
            item {
                Text("Manage Behavior Types", style = MaterialTheme.typography.titleMedium)
            }
            items(behaviorTypesList.toList()) { type ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(type)
                    IconButton(onClick = {
                        val updatedList = behaviorTypesList.toMutableSet().apply { remove(type) }
                        settingsViewModel.updateBehaviorTypes(updatedList)
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
                        val updatedList = behaviorTypesList.toMutableSet().apply { add(newBehaviorType.trim()) }
                        settingsViewModel.updateBehaviorTypes(updatedList)
                        newBehaviorType = "" // Clear field
                        behaviorTypeError = null
                    } else {
                        behaviorTypeError = "Name cannot be blank."
                    }
                }, enabled = newBehaviorType.isNotBlank()) {
                    Text("Add Behavior Type")
                }
                HorizontalDivider(Modifier.padding(top = 8.dp))
            }

            // Manage Homework Assignment Names/Types
            item {
                Text("Manage Homework Assignment Names/Types", style = MaterialTheme.typography.titleMedium)
            }
            items(homeworkAssignmentTypesList.toList()) { type ->
                 Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(type)
                    IconButton(onClick = {
                        val updatedList = homeworkAssignmentTypesList.toMutableSet().apply { remove(type) }
                        settingsViewModel.updateHomeworkAssignmentTypes(updatedList)
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
                        val updatedList = homeworkAssignmentTypesList.toMutableSet().apply { add(newHomeworkAssignmentType.trim()) }
                        settingsViewModel.updateHomeworkAssignmentTypes(updatedList)
                        newHomeworkAssignmentType = "" // Clear field
                        homeworkAssignmentTypeError = null
                    } else {
                        homeworkAssignmentTypeError = "Name cannot be blank."
                    }
                }, enabled = newHomeworkAssignmentType.isNotBlank()) {
                    Text("Add Homework Assignment Type")
                }
                HorizontalDivider(Modifier.padding(top = 8.dp))
            }

            // Manage Homework Statuses
            item {
                Text("Manage Homework Statuses", style = MaterialTheme.typography.titleMedium)
            }
            items(homeworkStatusesList.toList()) { status ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(status)
                    IconButton(onClick = {
                        val updatedList = homeworkStatusesList.toMutableSet().apply { remove(status) }
                        settingsViewModel.updateHomeworkStatuses(updatedList)
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
                        val updatedList = homeworkStatusesList.toMutableSet().apply { add(newHomeworkStatus.trim()) }
                        settingsViewModel.updateHomeworkStatuses(updatedList)
                        newHomeworkStatus = "" // Clear field
                        homeworkStatusError = null
                    } else {
                        homeworkStatusError = "Status cannot be blank."
                    }
                }, enabled = newHomeworkStatus.isNotBlank()) {
                    Text("Add Homework Status")
                }
                HorizontalDivider(Modifier.padding(top = 8.dp))
            }

            // Manage Quiz Mark Types
            item {
                Text("Manage Quiz Mark Types", style = MaterialTheme.typography.titleMedium)
            }
            items(quizMarkTypesList) { markType ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(markType.name, style = MaterialTheme.typography.bodyLarge)
                            IconButton(onClick = {
                                val updatedList = quizMarkTypesList.toMutableList().apply { remove(markType) }
                                settingsViewModel.updateQuizMarkTypes(updatedList)
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
                            val newMarkType = QuizMarkTypeSetting(
                                id = UUID.randomUUID().toString(),
                                name = newQuizMarkTypeName.trim(),
                                defaultPoints = pointsValue,
                                contributesToTotal = newQuizMarkContributesToTotal,
                                isExtraCredit = newQuizMarkIsExtraCredit
                            )
                            val updatedList = quizMarkTypesList.toMutableList().apply { add(newMarkType) }
                            settingsViewModel.updateQuizMarkTypes(updatedList)
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
