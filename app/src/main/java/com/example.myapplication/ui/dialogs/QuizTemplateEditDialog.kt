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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTemplateEditDialog(
    onDismiss: () -> Unit,
    onSave: (QuizTemplate) -> Unit,
    quizTemplate: QuizTemplate? = null
) {
    var name by remember { mutableStateOf(quizTemplate?.name ?: "") }
    var numQuestions by remember { mutableStateOf(quizTemplate?.numQuestions?.toString() ?: "") }
    var defaultMarks by remember { mutableStateOf(quizTemplate?.defaultMarks?.map { "${it.key}:${it.value}" }?.joinToString(", ") ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (quizTemplate == null) "Add Quiz Template" else "Edit Quiz Template") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Template Name") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    isError = nameError != null,

                )
                val currentNameError = nameError
                if (currentNameError != null) {
                    Text(text = currentNameError, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                }
                TextField(
                    value = numQuestions,
                    onValueChange = { numQuestions = it },
                    label = { Text("Number of Questions") },
                    modifier = Modifier.fillMaxWidth()
                )
                 TextField(
                    value = defaultMarks,
                    onValueChange = { defaultMarks = it },
                    label = { Text("Default Marks (e.g., A:10, B:5)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = "Template name cannot be empty."
                        return@Button
                    }
                    val marks = defaultMarks.split(",").mapNotNull {
                        val parts = it.trim().split(":")
                        if (parts.size == 2) {
                            val key = parts[0].trim()
                            val value = parts[1].trim().toIntOrNull()
                            if (value != null) {
                                key to value
                            } else {
                                null
                            }
                        } else {
                            null
                        }
                    }.toMap()

                    val template = QuizTemplate(
                        id = quizTemplate?.id ?: 0,
                        name = name,
                        numQuestions = numQuestions.toIntOrNull() ?: 0,
                        defaultMarks = marks
                    )
                    onSave(template)
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
