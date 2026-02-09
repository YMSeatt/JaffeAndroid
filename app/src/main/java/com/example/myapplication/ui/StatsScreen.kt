package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.exporter.ExportOptions
import com.example.myapplication.viewmodel.StatsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel) {
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

    val students by viewModel.allStudents.observeAsState(initial = emptyList())
    val customBehaviors by viewModel.allCustomBehaviors.observeAsState(initial = emptyList())
    val customHomeworkTypes by viewModel.allCustomHomeworkTypes.observeAsState(initial = emptyList())

    val behaviorSummary by viewModel.behaviorSummary.observeAsState(initial = emptyList())
    val quizSummary by viewModel.quizSummary.observeAsState(initial = emptyList())
    val homeworkSummary by viewModel.homeworkSummary.observeAsState(initial = emptyList())

    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    LaunchedEffect(
        startDateState.selectedDateMillis,
        endDateState.selectedDateMillis,
        selectedStudentIds,
        studentFilter,
        selectedBehaviorTypes,
        behaviorFilter,
        selectedHomeworkTypes,
        homeworkFilter
    ) {
        val options = ExportOptions(
            startDate = startDateState.selectedDateMillis,
            endDate = endDateState.selectedDateMillis,
            studentIds = if (studentFilter == "all") null else selectedStudentIds,
            behaviorTypes = if (behaviorFilter == "all") null else selectedBehaviorTypes,
            homeworkTypes = if (homeworkFilter == "all") null else selectedHomeworkTypes,
            includeBehaviorLogs = true,
            includeQuizLogs = true,
            includeHomeworkLogs = true,
            includeSummarySheet = true,
            separateSheets = false,
            includeMasterLog = false
        )
        viewModel.updateStats(options)
    }


    Column(modifier = Modifier.padding(16.dp)) {
        // Filters
        Row {
            Button(onClick = { showStartDatePicker = true }) { Text("Start Date") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { showEndDatePicker = true }) { Text("End Date") }
        }
        Text("Start: ${startDateState.selectedDateMillis?.let { dateFormatter.format(Date(it)) } ?: "Not set"}")
        Text("End: ${endDateState.selectedDateMillis?.let { dateFormatter.format(Date(it)) } ?: "Not set"}")

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
            LazyColumn(modifier = Modifier.height(150.dp)) {
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
        }

        // Stats Display
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                Text("Behavior Summary", style = MaterialTheme.typography.headlineMedium)
            }
            items(behaviorSummary) {
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Student: ${it.studentName}")
                        Text("Behavior: ${it.behavior}")
                        Text("Count: ${it.count}")
                    }
                }
            }

            item {
                Text("Quiz Summary", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 16.dp))
            }
            items(quizSummary) {
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Student: ${it.studentName}")
                        Text("Quiz: ${it.quizName}")
                        Text("Average Score: ${"%.2f".format(it.averageScore)}%")
                        Text("Times Taken: ${it.timesTaken}")
                    }
                }
            }

            item {
                Text("Homework Summary", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 16.dp))
            }
            items(homeworkSummary) {
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Student: ${it.studentName}")
                        Text("Assignment: ${it.assignmentName}")
                        Text("Count: ${it.count}")
                        Text("Total Points: ${it.totalPoints}")
                    }
                }
            }
        }
    }
}