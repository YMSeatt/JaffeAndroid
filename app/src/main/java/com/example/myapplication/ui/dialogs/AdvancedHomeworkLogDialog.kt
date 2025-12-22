package com.example.myapplication.ui.dialogs

import android.util.Log
import androidx.compose.foundation.layout.Column // Added
import androidx.compose.foundation.layout.Row // Added
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox // Added
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider // Added
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment // Added
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.HomeworkMarkType // Added
import com.example.myapplication.data.HomeworkTemplate
import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import com.google.gson.Gson // Added
import kotlin.math.roundToInt // Added


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedHomeworkLogDialog(
    studentIds: List<Long>,
    viewModel: SeatingChartViewModel,
    settingsViewModel: SettingsViewModel,
    onDismissRequest: () -> Unit,
    onSave: (List<HomeworkLog>) -> Unit
) {
    val stickyHomeworkNameDuration by settingsViewModel.stickyHomeworkNameDurationSeconds.collectAsState(0)
    val lastHomeworkName by settingsViewModel.lastHomeworkName.collectAsState(null)
    val lastHomeworkTimestamp by settingsViewModel.lastHomeworkTimestamp.collectAsState(null)

    var assignmentName by remember {
        mutableStateOf(
            if (stickyHomeworkNameDuration > 0 && lastHomeworkName != null && lastHomeworkTimestamp != null &&
                (System.currentTimeMillis() - lastHomeworkTimestamp!!) / 1000 < stickyHomeworkNameDuration
            ) {
                lastHomeworkName!!
            } else {
                ""
            }
        )
    }
    var comment by remember { mutableStateOf("") }
    val homeworkTemplates by viewModel.allHomeworkTemplates.observeAsState(initial = emptyList())
    var selectedTemplate by remember { mutableStateOf<HomeworkTemplate?>(null) }
    val homeworkTypes by settingsViewModel.customHomeworkTypes.observeAsState(initial = emptyList()) // kept for name suggestion if needed?
    var selectedHomeworkType by remember { mutableStateOf("") }
    val homeworkStatuses by settingsViewModel.customHomeworkStatuses.observeAsState(initial = emptyList())
    val statusNames = remember(homeworkStatuses) { homeworkStatuses.map { it.name } } // Extract names
    var selectedHomeworkStatus by remember { mutableStateOf("") }


    val markValues = remember { mutableStateMapOf<String, Any>() } // stepId -> value
    var student by remember { mutableStateOf<Student?>(null) }

    LaunchedEffect(studentIds) {
        if (studentIds.size == 1) {
            student = viewModel.getStudentForEditing(studentIds.first())
        }
    }

    // When template is selected, initialize mark values
    LaunchedEffect(selectedTemplate) {
        markValues.clear()
        selectedTemplate?.getSteps()?.forEach { step ->
             // Use label as key for readability, ensure uniqueness if needed (simple approach: label)
             // In a perfect world we'd handle duplicate labels, but for now we assume distinctness or last-win.
             val key = step.label
            when (step.type) {
                HomeworkMarkType.CHECKBOX -> markValues[key] = false
                HomeworkMarkType.SCORE -> markValues[key] = 0
                HomeworkMarkType.COMMENT -> markValues[key] = ""
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            val titleText = if (student != null) {
                "Log Homework for ${student!!.firstName} ${student!!.lastName}"
            } else {
                "Log Homework for ${studentIds.size} ${if (studentIds.size == 1) "student" else "students"}"
            }
            Text(titleText)
        },
        text = {
            LazyColumn {
                item {
                    OutlinedTextField(
                        value = assignmentName,
                        onValueChange = { assignmentName = it },
                        label = { Text("Assignment Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                     // Homework Type - Maybe use it to populate assignment name? Or just a category? 
                     // For now, let's keep it as an autocomplete for Assignment Name or separate field?
                     // The original code had it as a separate dropdown.
                     
                    var homeworkTypeExpanded by remember { mutableStateOf(false) }
                     ExposedDropdownMenuBox(
                        expanded = homeworkTypeExpanded,
                        onExpandedChange = { homeworkTypeExpanded = !homeworkTypeExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedHomeworkType,
                            onValueChange = { selectedHomeworkType = it }, // Allow typing?
                            readOnly = true,
                            label = { Text("Homework Type (Optional)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = homeworkTypeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                        )
                        ExposedDropdownMenu(
                            expanded = homeworkTypeExpanded,
                            onDismissRequest = { homeworkTypeExpanded = false }
                        ) {
                            homeworkTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name) },
                                    onClick = {
                                        selectedHomeworkType = type.name
                                        if (assignmentName.isBlank()) assignmentName = type.name // Auto-fill name if blank
                                        homeworkTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    var homeworkStatusExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = homeworkStatusExpanded,
                        onExpandedChange = { homeworkStatusExpanded = !homeworkStatusExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedHomeworkStatus,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = homeworkStatusExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                        )
                        ExposedDropdownMenu(
                            expanded = homeworkStatusExpanded,
                            onDismissRequest = { homeworkStatusExpanded = false }
                        ) {
                            // Standard statuses
                            listOf("Done", "Not Done", "Completed", "Incomplete", "Late", "Excused").forEach { status ->
                                 DropdownMenuItem(
                                    text = { Text(status) },
                                    onClick = {
                                        selectedHomeworkStatus = status
                                        homeworkStatusExpanded = false
                                    }
                                )
                            }
                            // Custom statuses
                            statusNames.forEach { status: String ->
                                DropdownMenuItem(
                                    text = { Text(status) },
                                    onClick = {
                                        selectedHomeworkStatus = status
                                        homeworkStatusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    var templateExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = templateExpanded,
                        onExpandedChange = { templateExpanded = !templateExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedTemplate?.name ?: "Select Template (Optional)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Template") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = templateExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                        )
                        ExposedDropdownMenu(
                            expanded = templateExpanded,
                            onDismissRequest = { templateExpanded = false }
                        ) {
                            DropdownMenuItem(text = { Text("None")}, onClick = { selectedTemplate = null; templateExpanded = false})
                            homeworkTemplates.forEach { template ->
                                DropdownMenuItem(
                                    text = { Text(template.name) },
                                    onClick = {
                                        selectedTemplate = template
                                        if (assignmentName.isBlank()) assignmentName = template.name
                                        templateExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (selectedTemplate != null) {
                    val steps = selectedTemplate!!.getSteps()
                    items(steps) { step ->
                         Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(step.label, style = MaterialTheme.typography.labelMedium)
                            val key = step.label
                            when (step.type) {
                                HomeworkMarkType.CHECKBOX -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = (markValues[key] as? Boolean) == true,
                                            onCheckedChange = { markValues[key] = it }
                                        )
                                        Text(if ((markValues[key] as? Boolean) == true) "Yes" else "No")
                                    }
                                }
                                HomeworkMarkType.SCORE -> {
                                    var score by remember(markValues[key]) { mutableStateOf(((markValues[key] as? Number)?.toFloat() ?: 0f)) }
                                    Column {
                                        Slider(
                                            value = score,
                                            onValueChange = { 
                                                score = it
                                                markValues[key] = it.roundToInt() 
                                            },
                                            valueRange = 0f..step.maxValue.toFloat(),
                                            steps = if (step.maxValue > 0) step.maxValue - 1 else 0
                                        )
                                        Text("Score: ${score.roundToInt()} / ${step.maxValue}")
                                    }
                                }
                                HomeworkMarkType.COMMENT -> {
                                    OutlinedTextField(
                                        value = (markValues[key] as? String) ?: "",
                                        onValueChange = { markValues[key] = it },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Global Comment (Optional)") },
                        modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val logs = studentIds.map { studentId ->
                        HomeworkLog(
                            studentId = studentId,
                            assignmentName = assignmentName,
                            status = selectedHomeworkStatus,
                            comment = comment,
                            loggedAt = System.currentTimeMillis(),
                            marksData = Gson().toJson(markValues)
                        )
                    }
                    onSave(logs)
                    onDismissRequest()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
