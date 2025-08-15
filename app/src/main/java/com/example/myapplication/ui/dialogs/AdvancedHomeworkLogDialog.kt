package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel
import kotlinx.coroutines.launch

@Composable
fun AdvancedHomeworkLogDialog(
    student: Student,
    viewModel: SeatingChartViewModel,
    onDismiss: () -> Unit
) {
    var homeworkName by remember { mutableStateOf("") }
    var numItems by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val isFormValid = homeworkName.isNotBlank()

    // Detailed marks state
    val markTypes = listOf("Complete", "Incomplete", "Not Done", "Effort Score (1-5)")
    val markCounts = remember { mutableStateMapOf<String, String>() }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Homework for ${student.firstName} ${student.lastName}") },
        text = {
            Column {
                OutlinedTextField(
                    value = homeworkName,
                    onValueChange = { homeworkName = it },
                    label = { Text("Homework Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = homeworkName.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = numItems,
                    onValueChange = { numItems = it },
                    label = { Text("Number of Items") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Marks Details:", style = MaterialTheme.typography.titleMedium)
                markTypes.forEach { markType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(markType)
                        OutlinedTextField(
                            value = markCounts[markType] ?: "",
                            onValueChange = { markCounts[markType] = it },
                            modifier = Modifier.width(100.dp),
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
                    val log = HomeworkLog(
                        studentId = student.id,
                        homeworkName = homeworkName,
                        timestamp = System.currentTimeMillis(),
                        marksData = kotlinx.serialization.json.Json.encodeToString(marksDataMap),
                        numItems = numItems.toIntOrNull(),
                        comment = comment,
                        status = "Completed"
                    )
                    coroutineScope.launch {
                        viewModel.addHomeworkLog(log)
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
