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

private fun Map<String, Int>.toMarksString(): String {
    return this.entries.joinToString(separator = ", ") { "${it.key}:${it.value}" }
}

private fun String.toMarksMap(): Pair<Map<String, Int>, Boolean> {
    if (this.isBlank()) {
        return Pair(emptyMap(), true)
    }
    val entries = this.split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    var isValid = true
    val map = entries.mapNotNull { entry ->
        val parts = entry.split(':', limit = 2)
        if (parts.size == 2) {
            val key = parts[0].trim()
            val value = parts[1].trim().toIntOrNull()
            if (key.isNotEmpty() && value != null) {
                key to value
            } else {
                isValid = false
                null
            }
        } else {
            isValid = false
            null
        }
    }.toMap()

    if (map.size != entries.size) {
        isValid = false
    }

    return Pair(map, isValid)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTemplateEditDialog(
    onDismiss: () -> Unit,
    onSave: (QuizTemplate) -> Unit,
    quizTemplate: QuizTemplate? = null
) {
    var name by remember { mutableStateOf(quizTemplate?.name ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }

    var numQuestions by remember { mutableStateOf(quizTemplate?.numQuestions?.toString() ?: "") }
    var numQuestionsError by remember { mutableStateOf<String?>(null) }

    var defaultMarks by remember { mutableStateOf(quizTemplate?.defaultMarks?.toMarksString() ?: "") }
    var defaultMarksError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        var isValid = true
        if (name.isBlank()) {
            nameError = "Template name cannot be empty."
            isValid = false
        } else {
            nameError = null
        }

        val numQ = numQuestions.toIntOrNull()
        if (numQ == null || numQ <= 0) {
            numQuestionsError = "Must be a positive number."
            isValid = false
        } else {
            numQuestionsError = null
        }

        val (_, marksAreValid) = defaultMarks.toMarksMap()
        if (!marksAreValid) {
            defaultMarksError = "Invalid format. Use Key:Value,..."
            isValid = false
        } else {
            defaultMarksError = null
        }

        return isValid
    }

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
                    supportingText = { nameError?.let { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                TextField(
                    value = numQuestions,
                    onValueChange = { numQuestions = it; numQuestionsError = null },
                    label = { Text("Number of Questions") },
                    isError = numQuestionsError != null,
                    supportingText = { numQuestionsError?.let { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                TextField(
                    value = defaultMarks,
                    onValueChange = { defaultMarks = it; defaultMarksError = null },
                    label = { Text("Default Marks (e.g., A:10,B:5)") },
                    isError = defaultMarksError != null,
                    supportingText = { defaultMarksError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validate()) {
                        val (marksMap, _) = defaultMarks.toMarksMap()
                        val template = QuizTemplate(
                            id = quizTemplate?.id ?: 0,
                            name = name,
                            numQuestions = numQuestions.toInt(),
                            defaultMarks = marksMap
                        )
                        onSave(template)
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
