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
import com.example.myapplication.util.QuizTemplateParser

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
    var defaultMarks by remember {
        mutableStateOf(
            quizTemplate?.defaultMarks?.map { "${it.key}:${it.value}" }?.joinToString(", ") ?: ""
        )
    }
    var defaultMarksError by remember { mutableStateOf<String?>(null) }

    fun validateMarks(marks: String) {
        defaultMarksError = if (marks.isNotBlank() &&
            marks.split(',').any {
                val parts = it.trim().split(':')
                parts.size != 2 || parts[0].isBlank() || parts[1].trim().toIntOrNull() == null
            }
        ) {
            "Invalid format"
        } else {
            null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (quizTemplate == null) "Add Quiz Template" else "Edit Quiz Template") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = if (it.isBlank()) "Name cannot be empty" else null
                    },
                    label = { Text("Template Name") },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                TextField(
                    value = numQuestions,
                    onValueChange = {
                        numQuestions = it
                        numQuestionsError =
                            if (it.toIntOrNull() == null) "Must be a valid number" else null
                    },
                    label = { Text("Number of Questions") },
                    isError = numQuestionsError != null,
                    supportingText = { numQuestionsError?.let { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                TextField(
                    value = defaultMarks,
                    onValueChange = {
                        defaultMarks = it
                        validateMarks(it)
                    },
                    label = { Text("Default Marks (e.g., A:10, B:5)") },
                    isError = defaultMarksError != null,
                    supportingText = { defaultMarksError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedMarks = QuizTemplateParser.parseDefaultMarks(defaultMarks)

                    val template = QuizTemplate(
                        id = quizTemplate?.id ?: 0,
                        name = name,
                        numQuestions = numQuestions.toIntOrNull() ?: 0,
                        defaultMarks = parsedMarks
                    )
                    onSave(template)
                },
                enabled = name.isNotBlank() && numQuestions.toIntOrNull() != null && defaultMarksError == null
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
