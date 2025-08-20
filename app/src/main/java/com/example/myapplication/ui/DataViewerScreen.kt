package com.example.myapplication.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodel.SeatingChartViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class ViewerTab {
    STUDENTS,
    BEHAVIOR_LOGS,
    HOMEWORK_LOGS,
    QUIZ_LOGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataViewerScreen(
    seatingChartViewModel: SeatingChartViewModel,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(ViewerTab.STUDENTS) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Viewer") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                ViewerTab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab.ordinal == index,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    ViewerTab.STUDENTS -> StudentsTab(seatingChartViewModel)
                    ViewerTab.BEHAVIOR_LOGS -> BehaviorLogsTab(seatingChartViewModel)
                    ViewerTab.HOMEWORK_LOGS -> HomeworkLogsTab(seatingChartViewModel)
                    ViewerTab.QUIZ_LOGS -> QuizLogsTab(seatingChartViewModel)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onDismiss) {
                    Text("Back")
                }
            }
        }
    }
}

@Composable
fun StudentsTab(seatingChartViewModel: SeatingChartViewModel) {
    val students by seatingChartViewModel.allStudents.observeAsState(initial = emptyList())

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(students) {
            Text(text = "Name: ${it.firstName} ${it.lastName}, Position: (${it.xPosition}, ${it.yPosition})")
        }
    }
}

@Composable
fun BehaviorLogsTab(seatingChartViewModel: SeatingChartViewModel) {
    val behaviorLogs by seatingChartViewModel.allBehaviorEvents.observeAsState(initial = emptyList())
    val students by seatingChartViewModel.allStudents.observeAsState(initial = emptyList())
    val studentMap = remember(students) { students.associateBy { it.id } }
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") } }
    val dayFormatter = remember { SimpleDateFormat("EEEE", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") } }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(behaviorLogs) {
            val studentName = studentMap[it.studentId]?.let { student -> "${student.firstName} ${student.lastName}" } ?: "Unknown"
            Text(text = "Student: $studentName, Day: ${dayFormatter.format(Date(it.timestamp))}, Type: ${it.type}, Timestamp: ${dateFormatter.format(Date(it.timestamp))}, Comment: ${it.comment ?: "N/A"}")
        }
    }
}

@Composable
fun HomeworkLogsTab(seatingChartViewModel: SeatingChartViewModel) {
    val homeworkLogs by seatingChartViewModel.allHomeworkLogs.observeAsState(initial = emptyList())
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") } }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(homeworkLogs) {
            Text(text = "Student ID: ${it.studentId}, Assignment: ${it.assignmentName}, Status: ${it.status}, Logged At: ${dateFormatter.format(Date(it.loggedAt))}, Comment: ${it.comment ?: "N/A"}")
        }
    }
}

@Composable
fun QuizLogsTab(seatingChartViewModel: SeatingChartViewModel) {
    val quizLogs by seatingChartViewModel.allQuizLogs.observeAsState(initial = emptyList())
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") } }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(quizLogs) {
            Text(text = "Student ID: ${it.studentId}, Quiz Name: ${it.quizName}, Mark Value: ${it.markValue ?: "N/A"}, Mark Type: ${it.markType ?: "N/A"}, Max Mark: ${it.maxMarkValue ?: "N/A"}, Logged At: ${dateFormatter.format(Date(it.loggedAt))}, Comment: ${it.comment ?: "N/A"}")
        }
    }
}