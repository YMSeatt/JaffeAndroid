package com.example.myapplication.ui.settings

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.StudentRepository
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(studentRepository: StudentRepository, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val students by studentRepository.allStudents.observeAsState(initial = emptyList())
    val behaviorLogs by studentRepository.getAllBehaviorEvents().observeAsState(initial = emptyList())
    val homeworkLogs by studentRepository.getAllHomeworkLogs().observeAsState(initial = emptyList())
    val quizLogs by studentRepository.getAllQuizLogs().observeAsState(initial = emptyList())

    var exportType by remember { mutableStateOf<ExportType?>(null) }


    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val jsonString = when (exportType) {
                        ExportType.STUDENTS -> Json.encodeToString(students)
                        ExportType.BEHAVIOR_LOGS -> Json.encodeToString(behaviorLogs)
                        ExportType.HOMEWORK_LOGS -> Json.encodeToString(homeworkLogs)
                        ExportType.QUIZ_LOGS -> Json.encodeToString(quizLogs)
                        else -> ""
                    }

                    if (jsonString.isNotEmpty() && jsonString != "[]") {
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(jsonString.toByteArray())
                        }
                        Toast.makeText(context, "Data exported successfully", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to export data: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    exportType = ExportType.STUDENTS
                    createDocumentLauncher.launch("students.json")
                          },
                enabled = students.isNotEmpty()
            ) {
                Text("Export Students to JSON")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    exportType = ExportType.BEHAVIOR_LOGS
                    createDocumentLauncher.launch("behavior_logs.json")
                          },
                enabled = behaviorLogs.isNotEmpty()
            ) {
                Text("Export Behavior Logs to JSON")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    exportType = ExportType.HOMEWORK_LOGS
                    createDocumentLauncher.launch("homework_logs.json")
                          },
                enabled = homeworkLogs.isNotEmpty()
            ) {
                Text("Export Homework Logs to JSON")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    exportType = ExportType.QUIZ_LOGS
                    createDocumentLauncher.launch("quiz_logs.json")
                          },
                enabled = quizLogs.isNotEmpty()
            ) {
                Text("Export Quiz Logs to JSON")
            }
        }
    }
}
enum class ExportType {
    STUDENTS,
    BEHAVIOR_LOGS,
    HOMEWORK_LOGS,
    QUIZ_LOGS
}