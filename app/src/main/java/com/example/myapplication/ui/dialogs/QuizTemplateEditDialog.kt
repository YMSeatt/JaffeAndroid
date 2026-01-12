package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
    var defaultMarks by remember { mutableStateOf(quizTemplate?.defaultMarks?.map { "${it.key}=${it.value}" }?.joinToString(", ") ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (quizTemplate == null) "Add Quiz Template" else "Edit Quiz Template") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Template Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = numQuestions,
                    onValueChange = { numQuestions = it },
                    label = { Text("Number of Questions") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = defaultMarks,
                    onValueChange = { defaultMarks = it },
                    label = { Text("Default Marks (e.g., correct=1, incorrect=0)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val marksMap = parseDefaultMarks(defaultMarks)
                    val template = QuizTemplate(
                        id = quizTemplate?.id ?: 0,
                        name = name,
                        numQuestions = numQuestions.toIntOrNull() ?: 0,
                        defaultMarks = marksMap
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

private fun parseDefaultMarks(marks: String): Map<String, Int> {
    return marks.split(",")
        .map { it.trim().split("=") }
        .filter { it.size == 2 }
        .associate { it[0] to (it[1].toIntOrNull() ?: 0) }
}
