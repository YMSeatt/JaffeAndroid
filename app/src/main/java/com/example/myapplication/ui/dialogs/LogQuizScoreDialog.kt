package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.QuizTemplate
import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogQuizScoreDialog(
    studentIds: List<Long>,
    viewModel: SeatingChartViewModel,
    settingsViewModel: SettingsViewModel,
    onDismissRequest: () -> Unit,
    onSave: (List<QuizLog>) -> Unit
) {
    val stickyQuizNameDuration by settingsViewModel.stickyQuizNameDurationSeconds.collectAsState(0)
    val lastQuizName by settingsViewModel.lastQuizName.collectAsState(null)
    val lastQuizTimestamp by settingsViewModel.lastQuizTimestamp.collectAsState(null)

    var quizName by remember {
        mutableStateOf(
            if (stickyQuizNameDuration > 0 && lastQuizName != null && lastQuizTimestamp != null &&
                (System.currentTimeMillis() - lastQuizTimestamp!!) / 1000 < stickyQuizNameDuration
            ) {
                lastQuizName!!
            } else {
                ""
            }
        )
    }
    var numQuestions by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    val quizMarkTypes by viewModel.quizMarkTypes.observeAsState(initial = emptyList())
    val quizTemplates by viewModel.allQuizTemplates.observeAsState(initial = emptyList())
    var selectedTemplate by remember { mutableStateOf<QuizTemplate?>(null) }

    val marksData = remember { mutableStateMapOf<String, String>() }
    var student by remember { mutableStateOf<Student?>(null) }

    LaunchedEffect(studentIds) {
        if (studentIds.size == 1) {
            student = viewModel.getStudentForEditing(studentIds.first())
        }
    }

    LaunchedEffect(selectedTemplate) {
        selectedTemplate?.let { template ->
            numQuestions = template.numQuestions?.toString() ?: ""
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
        title = {
            val titleText = if (student != null) {
                "Log Quiz Score for ${student!!.firstName} ${student!!.lastName}"
            } else {
                "Log Quiz Score for ${studentIds.size} students"
            }
            Text(titleText)
        },
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
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable, true)
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
                        value = marksData[markType.name] ?: "",
                        onValueChange = { marksData[markType.name] = it },
                        label = { Text(markType.name) },
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
                    val logs = studentIds.map { studentId ->
                        QuizLog(
                            id = 0,
                            studentId = studentId,
                            quizName = quizName,
                            comment = comment,
                            loggedAt = System.currentTimeMillis(),
                            marksData = serializedMarks,
                            numQuestions = numQuestions.toIntOrNull() ?: 0,
                            markValue = null,
                            markType = null,
                            maxMarkValue = null
                        )
                    }
                    onSave(logs)
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
