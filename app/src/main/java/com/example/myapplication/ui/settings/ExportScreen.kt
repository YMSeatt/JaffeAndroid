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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.StudentRepository
import com.example.myapplication.utils.ExcelImportUtil
import kotlinx.coroutines.launch

private enum class ExportType {
    NONE, STUDENTS, BEHAVIOR, HOMEWORK, QUIZ
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(studentRepository: StudentRepository, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var exportType by remember { mutableStateOf(ExportType.NONE) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                val result = when (exportType) {
                    ExportType.STUDENTS -> {
                        val students = studentRepository.allStudents.value
                        if (students.isNullOrEmpty()) {
                            Toast.makeText(context, "No student data to export", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        ExcelImportUtil.exportData(context, it, students, null, null, null)
                    }
                    ExportType.BEHAVIOR -> {
                        val behaviorLogs = studentRepository.getAllBehaviorEvents().value
                        if (behaviorLogs.isNullOrEmpty()) {
                            Toast.makeText(context, "No behavior logs to export", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        ExcelImportUtil.exportData(context, it, null, behaviorLogs, null, null)
                    }
                    ExportType.HOMEWORK -> {
                        val homeworkLogs = studentRepository.getAllHomeworkLogs().value
                        if (homeworkLogs.isNullOrEmpty()) {
                            Toast.makeText(context, "No homework logs to export", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        ExcelImportUtil.exportData(context, it, null, null, homeworkLogs, null)
                    }
                    ExportType.QUIZ -> {
                        val quizLogs = studentRepository.getAllQuizLogs().value
                        if (quizLogs.isNullOrEmpty()) {
                            Toast.makeText(context, "No quiz logs to export", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        ExcelImportUtil.exportData(context, it, null, null, null, quizLogs)
                    }
                    ExportType.NONE -> {
                        return@launch
                    }
                }
                handleExportResult(result, context)
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
            Button(onClick = {
                exportType = ExportType.STUDENTS
                createDocumentLauncher.launch("students.xlsx")
            }) {
                Text("Export Students")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                exportType = ExportType.BEHAVIOR
                createDocumentLauncher.launch("behavior_logs.xlsx")
            }) {
                Text("Export Behavior Logs")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                exportType = ExportType.HOMEWORK
                createDocumentLauncher.launch("homework_logs.xlsx")
            }) {
                Text("Export Homework Logs")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                exportType = ExportType.QUIZ
                createDocumentLauncher.launch("quiz_logs.xlsx")
            }) {
                Text("Export Quiz Logs")
            }
        }
    }
}

private fun handleExportResult(result: Result<Unit>, context: Context) {
    if (result.isSuccess) {
        Toast.makeText(context, "Data exported successfully", Toast.LENGTH_LONG).show()
    } else {
        Toast.makeText(context, "Failed to export data: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
    }
}