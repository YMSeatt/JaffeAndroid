package com.example.myapplication.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import com.example.myapplication.viewmodel.StatsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Defines the available tabs within the [DataViewerScreen].
 */
enum class ViewerTab {
    /** List of students and their current chart positions. */
    STUDENTS,
    /** Chronological log of behavioral incidents. */
    BEHAVIOR_LOGS,
    /** Historical homework completion records. */
    HOMEWORK_LOGS,
    /** Longitudinal quiz performance data. */
    QUIZ_LOGS,
    /** Integrated classroom analytics dashboard. */
    STATS
}

/**
 * DataViewerScreen: A high-level auditing and inspection tool for classroom data.
 *
 * This screen provides a tab-based interface for teachers to review raw database entities
 * and aggregated statistics in a unified view. It serves as the primary "Audit Log" for
 * behavioral and academic history.
 *
 * ### Shield Security (PII Protection):
 * To prevent unauthorized capture of student data, this screen implements **`FLAG_SECURE`**
 * via a [DisposableEffect]. This blocks system-level screenshots and screen recordings
 * while the viewer is active, ensuring that sensitive Personally Identifiable Information
 * (PII) remains protected.
 *
 * ### BOLT Performance Patterns:
 * - **Tab State Persistence**: Uses [remember] to maintain the current tab selection
 *   during configuration changes.
 * - **Date Hoisting**: Individual log tabs (Behavior, Homework, Quiz) utilize a "Hoisted
 *   Date" pattern. By reusing a single [Date] object across all items in a [LazyColumn],
 *   the UI minimizes object churn and GC pressure during high-speed scrolling.
 *
 * @param seatingChartViewModel The primary coordinator for classroom state.
 * @param statsViewModel The specialized ViewModel for analytics.
 * @param onDismiss Callback to exit the viewer.
 * @param settingsViewModel The configuration manager.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataViewerScreen(
    seatingChartViewModel: SeatingChartViewModel,
    statsViewModel: StatsViewModel,
    onDismiss: () -> Unit,
    settingsViewModel: SettingsViewModel
) {
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

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
                    ViewerTab.STATS -> StatsScreen(statsViewModel)
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

/**
 * Displays a list of all students and their logical canvas coordinates.
 */
@Composable
fun StudentsTab(seatingChartViewModel: SeatingChartViewModel) {
    val students by seatingChartViewModel.allStudents.observeAsState(initial = emptyList())

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(students) {
            Text(text = "Name: ${it.firstName} ${it.lastName}, Position: (${it.xPosition}, ${it.yPosition})")
        }
    }
}

/**
 * Displays a chronological list of behavioral incidents for all students.
 *
 * BOLT: Reuses a single [Date] object across all log items to eliminate thousands
 * of allocations during high-frequency scrolling.
 */
@Composable
fun BehaviorLogsTab(seatingChartViewModel: SeatingChartViewModel) {
    val behaviorLogs by seatingChartViewModel.allBehaviorEvents.observeAsState(initial = emptyList())
    val students by seatingChartViewModel.allStudents.observeAsState(initial = emptyList())
    val studentMap = remember(students) { students.associateBy { it.id } }
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") } }
    val dayFormatter = remember { SimpleDateFormat("EEEE", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") } }

    // BOLT: Hoist Date object to avoid O(N) allocations during scrolling
    val date = remember { Date() }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(behaviorLogs) {
            val studentName = studentMap[it.studentId]?.let { student -> "${student.firstName} ${student.lastName}" } ?: "Unknown"
            date.time = it.timestamp
            Text(text = "Student: $studentName, Day: ${dayFormatter.format(date)}, Type: ${it.type}, Timestamp: ${dateFormatter.format(date)}, Comment: ${it.comment ?: "N/A"}")
        }
    }
}

/**
 * Displays a list of historical homework completion records.
 *
 * BOLT: Reuses a single [Date] object for timestamp formatting.
 */
@Composable
fun HomeworkLogsTab(seatingChartViewModel: SeatingChartViewModel) {
    val homeworkLogs by seatingChartViewModel.allHomeworkLogs.observeAsState(initial = emptyList())
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") } }

    // BOLT: Hoist Date object to avoid O(N) allocations during scrolling
    val date = remember { Date() }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(homeworkLogs) {
            date.time = it.loggedAt
            Text(text = "Student ID: ${it.studentId}, Assignment: ${it.assignmentName}, Status: ${it.status}, Logged At: ${dateFormatter.format(date)}, Comment: ${it.comment ?: "N/A"}")
        }
    }
}

/**
 * Displays a list of longitudinal quiz performance data.
 *
 * BOLT: Reuses a single [Date] object for timestamp formatting.
 */
@Composable
fun QuizLogsTab(seatingChartViewModel: SeatingChartViewModel) {
    val quizLogs by seatingChartViewModel.allQuizLogs.observeAsState(initial = emptyList())
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") } }

    // BOLT: Hoist Date object to avoid O(N) allocations during scrolling
    val date = remember { Date() }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(quizLogs) {
            date.time = it.loggedAt
            Text(text = "Student ID: ${it.studentId}, Quiz Name: ${it.quizName}, Mark Value: ${it.markValue ?: "N/A"}, Mark Type: ${it.markType ?: "N/A"}, Max Mark: ${it.maxMarkValue ?: "N/A"}, Logged At: ${dateFormatter.format(date)}, Comment: ${it.comment ?: "N/A"}")
        }
    }
}