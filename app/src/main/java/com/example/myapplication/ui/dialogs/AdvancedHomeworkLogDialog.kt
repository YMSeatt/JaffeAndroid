package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.HomeworkTemplate
import com.example.myapplication.viewmodel.SeatingChartViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedHomeworkLogDialog(
    studentId: Long,
    viewModel: SeatingChartViewModel,
    onDismissRequest: () -> Unit,
    onSave: (HomeworkLog) -> Unit
) {
    var assignmentName by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    val homeworkTemplates by viewModel.allHomeworkTemplates.collectAsState(emptyList())
    var selectedTemplate by remember { mutableStateOf<HomeworkTemplate?>(null) }
    val homeworkTypes by viewModel.customHomeworkTypes.collectAsState(emptyList())
    var selectedHomeworkType by remember { mutableStateOf("") }
    val homeworkStatuses by viewModel.customHomeworkStatuses.collectAsState(emptyList())
    var selectedHomeworkStatus by remember { mutableStateOf("") }


    val marksData = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(selectedTemplate) {
        selectedTemplate?.let { template ->
            // TODO: Deserialize template.marksData and populate the marksData map
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
                            modifier = Modifier.fillMaxWidth().menuAnchor()
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
                            modifier = Modifier.fillMaxWidth().menuAnchor()
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
                            modifier = Modifier.fillMaxWidth().menuAnchor()
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

                // TODO: Add items for homework mark types, similar to quiz mark types

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
                    val log = HomeworkLog(
                        studentId = studentId,
                        assignmentName = assignmentName,
                        status = selectedHomeworkStatus,
                        comment = comment,
                        loggedAt = System.currentTimeMillis(),
                        marksData = Json.encodeToString(marksData.toMap()) // Serialize marksData map to JSON string
                    )
                    onSave(log)
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
