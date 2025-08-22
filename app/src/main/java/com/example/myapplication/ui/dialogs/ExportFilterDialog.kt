package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportFilterDialog(
    viewModel: SeatingChartViewModel,
    onDismissRequest: () -> Unit,
    onExport: (ExportFilterOptions) -> Unit
) {
    val allStudents by viewModel.allStudents.observeAsState(initial = emptyList())

    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    var exportBehaviorLogs by remember { mutableStateOf(true) }
    var exportHomeworkLogs by remember { mutableStateOf(true) }
    var exportQuizLogs by remember { mutableStateOf(true) }

    val selectedStudentIds = remember { mutableStateListOf<Long>() }
    var studentDropdownExpanded by remember { mutableStateOf(false) }

    var exportType by remember { mutableStateOf(ExportType.MASTER_LOG) }
    var includeSummary by remember { mutableStateOf(true) }
    var separateSheets by remember { mutableStateOf(true) }
    var includeMasterLog by remember { mutableStateOf(true) }


    // Initialize dates to a reasonable default (e.g., last 30 days)
    LaunchedEffect(Unit) {
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val thirtyDaysAgo = calendar.timeInMillis

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        startDate = dateFormat.format(Date(thirtyDaysAgo))
        endDate = dateFormat.format(Date(today))
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Export Data Filter") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Text("Date Range:", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Text("Log Types:", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = exportBehaviorLogs, onCheckedChange = { exportBehaviorLogs = it })
                        Text("Behavior Logs")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = exportHomeworkLogs, onCheckedChange = { exportHomeworkLogs = it })
                        Text("Homework Logs")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = exportQuizLogs, onCheckedChange = { exportQuizLogs = it })
                        Text("Quiz Logs")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Text("Select Students:", style = MaterialTheme.typography.titleMedium)
                    ExposedDropdownMenuBox(
                        expanded = studentDropdownExpanded,
                        onExpandedChange = { studentDropdownExpanded = !studentDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = if (selectedStudentIds.isEmpty()) "All Students" else "${selectedStudentIds.size} selected",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Students") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = studentDropdownExpanded,
                            onDismissRequest = { studentDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Select All") },
                                onClick = {
                                    selectedStudentIds.clear()
                                    selectedStudentIds.addAll(allStudents.map { it.id })
                                    studentDropdownExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Deselect All") },
                                onClick = {
                                    selectedStudentIds.clear()
                                    studentDropdownExpanded = false
                                }
                            )
                            Divider()
                            allStudents.forEach { student ->
                                val isSelected = selectedStudentIds.contains(student.id)
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(checked = isSelected, onCheckedChange = {
                                                if (it) selectedStudentIds.add(student.id)
                                                else selectedStudentIds.remove(student.id)
                                            })
                                            Text(student.firstName + " " + student.lastName)
                                        }
                                    },
                                    onClick = {
                                        if (isSelected) selectedStudentIds.remove(student.id)
                                        else selectedStudentIds.add(student.id)
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Text("Export Format:", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = exportType == ExportType.MASTER_LOG, onClick = { exportType = ExportType.MASTER_LOG })
                        Text("Master Log (All selected logs in one sheet)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = exportType == ExportType.INDIVIDUAL_STUDENT_SHEETS, onClick = { exportType = ExportType.INDIVIDUAL_STUDENT_SHEETS })
                        Text("Individual Student Sheets")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Text("Additional Options:", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = includeSummary, onCheckedChange = { includeSummary = it })
                        Text("Include Summary")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = separateSheets, onCheckedChange = { separateSheets = it })
                        Text("Separate Sheets for each log type")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = includeMasterLog, onCheckedChange = { includeMasterLog = it })
                        Text("Include Master Log")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val startMillis = try { dateFormat.parse(startDate)?.time } catch (e: Exception) { null }
                val endMillis = try {
                    dateFormat.parse(endDate)?.let { date ->
                        val calendar = Calendar.getInstance()
                        calendar.time = date
                        calendar.set(Calendar.HOUR_OF_DAY, 23)
                        calendar.set(Calendar.MINUTE, 59)
                        calendar.set(Calendar.SECOND, 59)
                        calendar.timeInMillis
                    }
                } catch (e: Exception) { null }

                if (startMillis == null || endMillis == null) {
                    // Show error or toast
                    return@Button
                }

                onExport(
                    ExportFilterOptions(
                        startDate = startMillis,
                        endDate = endMillis,
                        exportBehaviorLogs = exportBehaviorLogs,
                        exportHomeworkLogs = exportHomeworkLogs,
                        exportQuizLogs = exportQuizLogs,
                        selectedStudentIds = if (selectedStudentIds.isEmpty()) allStudents.map { it.id } else selectedStudentIds.toList(),
                        exportType = exportType,
                        includeSummary = includeSummary,
                        separateSheets = separateSheets,
                        includeMasterLog = includeMasterLog
                    )
                )
                onDismissRequest()
            }) {
                Text("Export")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
