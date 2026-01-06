package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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

@Composable
fun QuizTemplateEditDialog(
    onDismiss: () -> Unit,
    onSave: (QuizTemplate) -> Unit,
    quizTemplate: QuizTemplate? = null
) {
    var name by remember { mutableStateOf(quizTemplate?.name ?: "") }
    var numQuestions by remember { mutableStateOf(quizTemplate?.numQuestions?.toString() ?: "10") }
    var defaultMarksString by remember { mutableStateOf(QuizTemplateParser.marksToString(quizTemplate?.defaultMarks)) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var marksError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (quizTemplate == null) "Add Quiz Template" else "Edit Quiz Template") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("Template Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    isError = nameError != null,
                    supportingText = { if (nameError != null) Text(nameError!!) }
                )
                TextField(
                    value = numQuestions,
                    onValueChange = { numQuestions = it },
                    label = { Text("Number of Questions") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                TextField(
                    value = defaultMarksString,
                    onValueChange = {
                        defaultMarksString = it
                        marksError = null
                    },
                    label = { Text("Default Marks (optional)") },
                    placeholder = { Text("e.g., A:10, B:5, C:0") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = marksError != null,
                    supportingText = { if (marksError != null) Text(marksError!!) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    nameError = if (name.isBlank()) "Name cannot be empty" else null
                    val parsedMarks = try {
                        marksError = null
                        QuizTemplateParser.parseMarks(defaultMarksString)
                    } catch (e: IllegalArgumentException) {
                        marksError = "Invalid format. Use Key:Value, Key:Value"
                        null
                    }

                    if (nameError == null && marksError == null) {
                        val template = QuizTemplate(
                            id = quizTemplate?.id ?: 0,
                            name = name.trim(),
                            numQuestions = numQuestions.toIntOrNull() ?: 0,
                            defaultMarks = parsedMarks ?: emptyMap()
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
