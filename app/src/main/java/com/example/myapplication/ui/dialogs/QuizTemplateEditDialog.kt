package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.QuizTemplate
import androidx.compose.material3.MaterialTheme


private fun Map<String, Int>.toDisplayString(): String {
    return this.map { "${it.key}:${it.value}" }.joinToString(", ")
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTemplateEditDialog(
    onDismiss: () -> Unit,
    onSave: (QuizTemplate) -> Boolean,
    quizTemplate: QuizTemplate? = null
) {
    var name by remember { mutableStateOf(quizTemplate?.name ?: "") }
    var numQuestions by remember { mutableStateOf(quizTemplate?.numQuestions?.toString() ?: "10") }
    var defaultMarksString by remember { mutableStateOf(quizTemplate?.defaultMarks?.toDisplayString() ?: "") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var numQuestionsError by remember { mutableStateOf<String?>(null) }
    var defaultMarksError by remember { mutableStateOf<String?>(null) }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (quizTemplate == null) "Add Quiz Template" else "Edit Quiz Template") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text("Template Name") },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                TextField(
                    value = numQuestions,
                    onValueChange = { numQuestions = it; numQuestionsError = null },
                    label = { Text("Number of Questions") },
                    isError = numQuestionsError != null,
                    supportingText = { numQuestionsError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                TextField(
                    value = defaultMarksString,
                    onValueChange = { defaultMarksString = it; defaultMarksError = null },
                    label = { Text("Default Marks (e.g., A:10, B:5)") },
                    isError = defaultMarksError != null,
                    supportingText = { defaultMarksError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var isValid = true

                    // Name validation
                    if (name.isBlank()) {
                        nameError = "Template name cannot be empty."
                        isValid = false
                    }

                    // Number of Questions validation
                    val numQuestionsInt = numQuestions.toIntOrNull()
                    if (numQuestionsInt == null || numQuestionsInt <= 0) {
                        numQuestionsError = "Must be a positive number."
                        isValid = false
                    }

                    // Default Marks validation
                    val marksMap = mutableMapOf<String, Int>()
                    if (defaultMarksString.isNotBlank()) {
                        val entries = defaultMarksString.split(',')
                        for (entry in entries) {
                            val parts = entry.trim().split(':')
                            if (parts.size == 2) {
                                val key = parts[0].trim()
                                val value = parts[1].trim().toIntOrNull()
                                if (key.isNotEmpty() && value != null) {
                                    marksMap[key] = value
                                } else {
                                    defaultMarksError = "Invalid format. Use 'Key:Value,...'"
                                    isValid = false
                                    break
                                }
                            } else {
                                defaultMarksError = "Invalid format. Use 'Key:Value,...'"
                                isValid = false
                                break
                            }
                        }
                    }


                    if (isValid) {
                        val template = QuizTemplate(
                            id = quizTemplate?.id ?: 0,
                            name = name.trim(),
                            numQuestions = numQuestionsInt!!,
                            defaultMarks = marksMap
                        )
                        val success = onSave(template)
                        if (success) {
                            onDismiss()
                        } else {
                            nameError = "A template with this name already exists."
                        }
                    }
                }
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
