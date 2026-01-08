package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.example.myapplication.util.QuizTemplateParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTemplateEditDialog(
    template: QuizTemplate?,
    onDismiss: () -> Unit,
    onSave: (QuizTemplate) -> Unit
) {
    var name by remember { mutableStateOf(template?.name ?: "") }
    var numQuestions by remember { mutableStateOf(template?.numQuestions?.toString() ?: "10") }
    var defaultMarks by remember { mutableStateOf(template?.let { QuizTemplateParser.formatMarks(it.defaultMarks) } ?: "") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var numQuestionsError by remember { mutableStateOf<String?>(null) }
    var defaultMarksError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        var isValid = true
        if (name.isBlank()) {
            nameError = "Template name cannot be empty."
            isValid = false
        } else {
            nameError = null
        }

        val num = numQuestions.toIntOrNull()
        if (num == null || num <= 0) {
            numQuestionsError = "Number of questions must be a positive integer."
            isValid = false
        } else {
            numQuestionsError = null
        }

        val marks = defaultMarks.split(",").map { it.trim() }
        val invalidMarks = marks.filter {
            if (it.isBlank()) return@filter false
            val parts = it.split(":")
            parts.size != 2 || parts[1].trim().toIntOrNull() == null
        }

        if (invalidMarks.isNotEmpty()) {
            defaultMarksError = "Invalid entries: ${invalidMarks.joinToString()}"
            // We don't set isValid = false, as we can still save the valid parts
        } else {
            defaultMarksError = null
        }

        return isValid
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (template == null) "Add Quiz Template" else "Edit Quiz Template") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Template Name") },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } }
                )
                TextField(
                    value = numQuestions,
                    onValueChange = { numQuestions = it },
                    label = { Text("Number of Questions") },
                    isError = numQuestionsError != null,
                    supportingText = { numQuestionsError?.let { Text(it) } }
                )
                TextField(
                    value = defaultMarks,
                    onValueChange = { defaultMarks = it },
                    label = { Text("Default Marks (e.g., A:10, B:5)") },
                    isError = defaultMarksError != null,
                    supportingText = { defaultMarksError?.let { Text(it) } }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validate()) {
                        val marksMap = QuizTemplateParser.parseMarks(defaultMarks)

                        val newTemplate = template?.copy(
                            name = name,
                            numQuestions = numQuestions.toInt(),
                            defaultMarks = marksMap
                        ) ?: QuizTemplate(
                            name = name,
                            numQuestions = numQuestions.toInt(),
                            defaultMarks = marksMap
                        )
                        onSave(newTemplate)
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
