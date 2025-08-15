package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun LogQuizScoreDialog(
    student: Student,
    viewModel: SeatingChartViewModel,
    onDismiss: () -> Unit
) {
    var quizName by remember { mutableStateOf("") }
    var numQuestions by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    val markTypes by viewModel.quizMarkTypes.collectAsState()
    val markCounts = remember { mutableStateMapOf<String, String>() }
    val coroutineScope = rememberCoroutineScope()
    val isFormValid = quizName.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Quiz Score for ${student.firstName} ${student.lastName}") },
        text = {
            Column {
                OutlinedTextField(
                    value = quizName,
                    onValueChange = { quizName = it },
                    label = { Text("Quiz Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = quizName.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = numQuestions,
                    onValueChange = { numQuestions = it },
                    label = { Text("Number of Questions") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Marks:", style = MaterialTheme.typography.titleMedium)
                markTypes.forEach { markType ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(markType.name)
                        OutlinedTextField(
                            value = markCounts[markType.id] ?: "",
                            onValueChange = { markCounts[markType.id] = it },
                            modifier = Modifier.width(80.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val marksDataMap = markCounts.mapValues { it.value.toIntOrNull() ?: 0 }
                    val log = QuizLog(
                        studentId = student.id,
                        quizName = quizName,
                        timestamp = System.currentTimeMillis(),
                        marksData = Json.encodeToString(marksDataMap),
                        numQuestions = numQuestions.toIntOrNull() ?: 0,
                        comment = comment
                    )
                    coroutineScope.launch {
                        viewModel.insertQuizLog(log)
                    }
                    onDismiss()
                },
                enabled = isFormValid
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
