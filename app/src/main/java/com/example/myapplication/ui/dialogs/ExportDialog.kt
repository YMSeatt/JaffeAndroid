package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Student
import com.example.myapplication.data.exporter.ExportOptions
import com.example.myapplication.viewmodel.SeatingChartViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDialog(
    viewModel: SeatingChartViewModel,
    onDismissRequest: () -> Unit,
    onExport: (ExportOptions, Boolean) -> Unit
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val startDateState = rememberDatePickerState()
    val endDateState = rememberDatePickerState()

    var studentFilter by remember { mutableStateOf("all") }
    var selectedStudentIds by remember { mutableStateOf<List<Long>>(emptyList()) }
    var behaviorFilter by remember { mutableStateOf("all") }
    var selectedBehaviorTypes by remember { mutableStateOf<List<String>>(emptyList()) }
    var homeworkFilter by remember { mutableStateOf("all") }
    var selectedHomeworkTypes by remember { mutableStateOf<List<String>>(emptyList()) }

    var includeBehaviorLogs by remember { mutableStateOf(true) }
    var includeQuizLogs by remember { mutableStateOf(true) }
    var includeHomeworkLogs by remember { mutableStateOf(true) }
    var includeSummarySheet by remember { mutableStateOf(true) }
    var separateSheets by remember { mutableStateOf(true) }
    var includeMasterLog by remember { mutableStateOf(true) }

    val students by viewModel.allStudents.observeAsState(initial = emptyList())
    val customBehaviors by viewModel.allCustomBehaviors.observeAsState(initial = emptyList())
    val customHomeworkTypes by viewModel.allCustomHomeworkTypes.observeAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Export Options") },
        text = {
            Column {
                // Date Range
                Row {
                    Button(onClick = { showStartDatePicker = true }) { Text("Start Date") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { showEndDatePicker = true }) { Text("End Date") }
                }
                Text("Start: ${startDateState.selectedDateMillis?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)) } ?: "Not set"}")
                Text("End: ${endDateState.selectedDateMillis?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)) } ?: "Not set"}")

                if (showStartDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showStartDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = { showStartDatePicker = false }) { Text("OK") }
                        }
                    ) {
                        DatePicker(state = startDateState)
                    }
                }

                if (showEndDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showEndDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = { showEndDatePicker = false }) { Text("OK") }
                        }
                    ) {
                        DatePicker(state = endDateState)
                    }
                }

                // Student Selection
                Text("Students")
                Row {
                    RadioButton(selected = studentFilter == "all", onClick = { studentFilter = "all" })
                    Text("All")
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = studentFilter == "specific", onClick = { studentFilter = "specific" })
                    Text("Specific")
                }
                if (studentFilter == "specific") {
                    Box(modifier = Modifier.height(150.dp)) {
                        val listState = rememberLazyListState()
                        LazyColumn(state = listState) {
                            items(students) { student ->
                                var isChecked by remember { mutableStateOf(selectedStudentIds.contains(student.id)) }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = {
                                            isChecked = it
                                            selectedStudentIds = if (isChecked) {
                                                selectedStudentIds + student.id
                                            } else {
                                                selectedStudentIds - student.id
                                            }
                                        }
                                    )
                                    Text("${student.firstName} ${student.lastName}")
                                }
                            }
                        }
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                            adapter = rememberScrollbarAdapter(scrollState = listState)
                        )
                    }
                }

                // Behavior Types
                Text("Behavior Types")
                Row {
                    RadioButton(selected = behaviorFilter == "all", onClick = { behaviorFilter = "all" })
                    Text("All")
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = behaviorFilter == "specific", onClick = { behaviorFilter = "specific" })
                    Text("Specific")
                }
                if (behaviorFilter == "specific") {
                    Box(modifier = Modifier.height(150.dp)) {
                        val listState = rememberLazyListState()
                        LazyColumn(state = listState) {
                            items(customBehaviors) { behavior ->
                                var isChecked by remember { mutableStateOf(selectedBehaviorTypes.contains(behavior.name)) }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = {
                                            isChecked = it
                                            selectedBehaviorTypes = if (isChecked) {
                                                selectedBehaviorTypes + behavior.name
                                            } else {
                                                selectedBehaviorTypes - behavior.name
                                            }
                                        }
                                    )
                                    Text(behavior.name)
                                }
                            }
                        }
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                            adapter = rememberScrollbarAdapter(scrollState = listState)
                        )
                    }
                }

                // Homework Types
                Text("Homework Types")
                Row {
                    RadioButton(selected = homeworkFilter == "all", onClick = { homeworkFilter = "all" })
                    Text("All")
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = homeworkFilter == "specific", onClick = { homeworkFilter = "specific" })
                    Text("Specific")
                }
                if (homeworkFilter == "specific") {
                    Box(modifier = Modifier.height(150.dp)) {
                        val listState = rememberLazyListState()
                        LazyColumn(state = listState) {
                            items(customHomeworkTypes) { homework ->
                                var isChecked by remember { mutableStateOf(selectedHomeworkTypes.contains(homework.name)) }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = {
                                            isChecked = it
                                            selectedHomeworkTypes = if (isChecked) {
                                                selectedHomeworkTypes + homework.name
                                            } else {
                                                selectedHomeworkTypes - homework.name
                                            }
                                        }
                                    )
                                    Text(homework.name)
                                }
                            }
                        }
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                            adapter = rememberScrollbarAdapter(scrollState = listState)
                        )
                    }
                }


                // Log Types
                Text("Log Types")
                Row {
                    Checkbox(checked = includeBehaviorLogs, onCheckedChange = { includeBehaviorLogs = it })
                    Text("Behavior")
                }
                Row {
                    Checkbox(checked = includeQuizLogs, onCheckedChange = { includeQuizLogs = it })
                    Text("Quiz")
                }
                Row {
                    Checkbox(checked = includeHomeworkLogs, onCheckedChange = { includeHomeworkLogs = it })
                    Text("Homework")
                }

                // Output Options
                Text("Output Options")
                Row {
                    Checkbox(checked = includeSummarySheet, onCheckedChange = { includeSummarySheet = it })
                    Text("Summary Sheet")
                }
                Row {
                    Checkbox(checked = separateSheets, onCheckedChange = { separateSheets = it })
                    Text("Separate Sheets")
                }
                Row {
                    Checkbox(checked = includeMasterLog, onCheckedChange = { includeMasterLog = it })
                    Text("Master Log")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val options = ExportOptions(
                        startDate = startDateState.selectedDateMillis,
                        endDate = endDateState.selectedDateMillis,
                        studentIds = if (studentFilter == "all") null else selectedStudentIds,
                        behaviorTypes = if (behaviorFilter == "all") null else selectedBehaviorTypes,
                        homeworkTypes = if (homeworkFilter == "all") null else selectedHomeworkTypes,
                        includeBehaviorLogs = includeBehaviorLogs,
                        includeQuizLogs = includeQuizLogs,
                        includeHomeworkLogs = includeHomeworkLogs,
                        includeSummarySheet = includeSummarySheet,
                        separateSheets = separateSheets,
                        includeMasterLog = includeMasterLog
                    )
                    onExport(options, false)
                }
            ) {
                Text("Export")
            }
        },
        dismissButton = {
            Row {
                Button(onClick = {
                    val options = ExportOptions(
                        startDate = startDateState.selectedDateMillis,
                        endDate = endDateState.selectedDateMillis,
                        studentIds = if (studentFilter == "all") null else selectedStudentIds,
                        behaviorTypes = if (behaviorFilter == "all") null else selectedBehaviorTypes,
                        homeworkTypes = if (homeworkFilter == "all") null else selectedHomeworkTypes,
                        includeBehaviorLogs = includeBehaviorLogs,
                        includeQuizLogs = includeQuizLogs,
                        includeHomeworkLogs = includeHomeworkLogs,
                        includeSummarySheet = includeSummarySheet,
                        separateSheets = separateSheets,
                        includeMasterLog = includeMasterLog
                    )
                    onExport(options, true)
                }) {
                    Text("Share via Email")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onDismissRequest) {
                    Text("Cancel")
                }
            }
        }
    )
}
