package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.QuizMarkType
import com.example.myapplication.data.QuizTemplate
import com.example.myapplication.viewmodel.SeatingChartViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogQuizScoreDialog(
    studentId: Long,
    viewModel: SeatingChartViewModel,
    onDismissRequest: () -> Unit,
    onSave: (QuizLog) -> Unit
) {
    var quizName by remember { mutableStateOf("") }
    var numQuestions by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    val quizMarkTypes by viewModel.quizMarkTypes.collectAsState(initial = emptyList())
    val quizTemplates by viewModel.allQuizTemplates.collectAsState(initial = emptyList())
    var selectedTemplate by remember { mutableStateOf<QuizTemplate?>(null) }

    val marksData = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(selectedTemplate) {
        selectedTemplate?.let { template ->
            if (template.marksData.isNotBlank()) {
                try {
                    val deserializedMarks = Json.decodeFromString<Map<String, String>>(template.marksData)
                    marksData.clear()
                    marksData.putAll(deserializedMarks)
                } catch (_: Exception) {
                    // Handle decoding error if necessary
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Log Quiz Score") },
        text = {
            LazyColumn {
                item {
                    OutlinedTextField(
                        value = quizName,
                        onValueChange = { quizName = it },
                        label = { Text("Quiz Name") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = numQuestions,
                        onValueChange = { numQuestions = it },
                        label = { Text("Number of Questions") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedTemplate?.name ?: "Select Template (Optional)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Template") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            quizTemplates.forEach { template ->
                                DropdownMenuItem(
                                    text = { Text(template.name) },
                                    onClick = {
                                        selectedTemplate = template
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Marks:", style = MaterialTheme.typography.titleMedium)
                }

                items(quizMarkTypes) { markType ->
                    OutlinedTextField(
                        value = marksData[markType] ?: "",
                        onValueChange = { marksData[markType] = it },
                        label = { Text(markType) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Comment (Optional)") },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val serializedMarks = Json.encodeToString(marksData.toMap())
                    val log = QuizLog(
                        studentId = studentId,
                        quizName = quizName,
                        comment = comment,
                        loggedAt = System.currentTimeMillis(),
                        marksData = serializedMarks,
                        numQuestions = numQuestions.toIntOrNull() ?: 0,
                        id = TODO(),
                        markValue = TODO(),
                        markType = TODO(),
                        maxMarkValue = TODO()
                    )
                    onSave(log)
                    onDismissRequest()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
