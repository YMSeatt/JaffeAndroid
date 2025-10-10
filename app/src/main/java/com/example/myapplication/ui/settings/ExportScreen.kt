package com.example.myapplication.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.StudentRepository
import com.example.myapplication.data.exporter.ExportOptions
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    studentRepository: StudentRepository,
    onDismiss: () -> Unit
) {
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var includeBehaviorLogs by remember { mutableStateOf(true) }
    var includeQuizLogs by remember { mutableStateOf(true) }
    var includeHomeworkLogs by remember { mutableStateOf(true) }
    var separateSheets by remember { mutableStateOf(true) }
    var includeMasterLog by remember { mutableStateOf(true) }
    var includeSummarySheet by remember { mutableStateOf(true) }
    var includeIndividualStudentSheets by remember { mutableStateOf(true) }
    var includeStudentInfoSheet by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        onResult = { uri ->
            uri?.let {
                coroutineScope.launch {
                    val options = ExportOptions(
                        startDate = startDate,
                        endDate = endDate,
                        studentIds = null, // Implement student selection
                        includeBehaviorLogs = includeBehaviorLogs,
                        includeQuizLogs = includeQuizLogs,
                        includeHomeworkLogs = includeHomeworkLogs,
                        separateSheets = separateSheets,
                        includeMasterLog = includeMasterLog,
                        includeSummarySheet = includeSummarySheet,
                        includeIndividualStudentSheets = includeIndividualStudentSheets,
                        includeStudentInfoSheet = includeStudentInfoSheet
                    )
                    // studentRepository.exportToExcel(uri, options)
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Data") },
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
                Text("Date Range", style = MaterialTheme.typography.titleMedium)
                // Implement Date Pickers here
            }

            item {
                Text("Log Types", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = includeBehaviorLogs, onCheckedChange = { includeBehaviorLogs = it })
                    Text("Behavior Logs")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = includeQuizLogs, onCheckedChange = { includeQuizLogs = it })
                    Text("Quiz Logs")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = includeHomeworkLogs, onCheckedChange = { includeHomeworkLogs = it })
                    Text("Homework Logs")
                }
            }

            item {
                Text("Output Options", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = separateSheets, onCheckedChange = { separateSheets = it })
                    Text("Separate sheets for each log type")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = includeMasterLog, onCheckedChange = { includeMasterLog = it }, enabled = separateSheets)
                    Text("Include Master Log")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = includeSummarySheet, onCheckedChange = { includeSummarySheet = it })
                    Text("Include Summary Sheet")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = includeIndividualStudentSheets, onCheckedChange = { includeIndividualStudentSheets = it })
                    Text("Include Individual Student Sheets")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = includeStudentInfoSheet, onCheckedChange = { includeStudentInfoSheet = it })
                    Text("Include Student Info Sheet")
                }
            }

            item {
                Button(
                    onClick = {
                        exportLauncher.launch("behavior_log_export.xlsx")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Export")
                }
            }
        }
    }
}
