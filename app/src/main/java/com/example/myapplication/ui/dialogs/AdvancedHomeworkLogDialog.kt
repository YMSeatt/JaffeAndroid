package com.example.myapplication.ui.dialogs

import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.HomeworkTemplate
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


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
    val homeworkTypes by viewModel.customHomeworkTypes.collectAsState(emptyList())
    var selectedHomeworkType by remember { mutableStateOf("") }
    val homeworkStatuses by viewModel.customHomeworkStatuses.collectAsState(emptyList())
    var selectedHomeworkStatus by remember { mutableStateOf("") }


    val marksData = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(selectedTemplate) {
        selectedTemplate?.let { template ->
            if (template.marksData.isNotBlank()) {
                try {
                    val deserializedMarks = Json.decodeFromString<Map<String, String>>(template.marksData)
                    marksData.clear()
                    marksData.putAll(deserializedMarks)
                } catch (e: Exception) {
                    Log.e("AdvancedHomeworkLogDialog", "Error deserializing marksData", e)
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Log Homework") },
        text = {
            LazyColumn {
                item {
                    OutlinedTextField(
                        value = assignmentName,
                        onValueChange = { assignmentName = it },
                        label = { Text("Assignment Name") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    var homeworkTypeExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = homeworkTypeExpanded,
                        onExpandedChange = { homeworkTypeExpanded = !homeworkTypeExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedHomeworkType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Homework Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = homeworkTypeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = homeworkTypeExpanded,
                            onDismissRequest = { homeworkTypeExpanded = false }
                        ) {
                            homeworkTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        selectedHomeworkType = type
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
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = homeworkStatusExpanded,
                            onDismissRequest = { homeworkStatusExpanded = false }
                        ) {
                            homeworkStatuses.forEach { status ->
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
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = templateExpanded,
                            onDismissRequest = { templateExpanded = false }
                        ) {
                            homeworkTemplates.forEach { template ->
                                DropdownMenuItem(
                                    text = { Text(template.name) },
                                    onClick = {
                                        selectedTemplate = template
                                        templateExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Marks:", style = MaterialTheme.typography.titleMedium)
                }

                items(homeworkTypes) { type ->
                    OutlinedTextField(
                        value = marksData[type] ?: "",
                        onValueChange = { marksData[type] = it },
                        label = { Text(type) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Comment (Optional)") },
                        modifier = Modifier.padding(top = 8.dp)
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
                            marksData = Json.encodeToString(marksData.toMap()) // Serialize marksData map to JSON string
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
