package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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
    var numQuestions by remember { mutableStateOf(quizTemplate?.numQuestions?.toString() ?: "10") }
    var defaultMarks by remember { mutableStateOf(quizTemplate?.defaultMarks?.map { "${it.key}:${it.value}" }?.joinToString(", ") ?: "") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var numQuestionsError by remember { mutableStateOf<String?>(null) }
    var defaultMarksError by remember { mutableStateOf<String?>(null) }

    val isFormValid by remember {
        derivedStateOf {
            name.isNotBlank() && numQuestions.toIntOrNull() != null && numQuestions.toInt() > 0 && nameError == null && numQuestionsError == null && defaultMarksError == null
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
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } }
                )
                TextField(
                    value = numQuestions,
                    onValueChange = {
                        numQuestions = it
                        numQuestionsError = when {
                            it.toIntOrNull() == null -> "Must be a number"
                            it.toInt() <= 0 -> "Must be positive"
                            else -> null
                        }
                    },
                    label = { Text("Number of Questions") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = numQuestionsError != null,
                    supportingText = { numQuestionsError?.let { Text(it) } }
                )
                TextField(
                    value = defaultMarks,
                    onValueChange = {
                        defaultMarks = it
                        try {
                            QuizTemplateParser.parseDefaultMarks(it)
                            defaultMarksError = null
                        } catch (e: IllegalArgumentException) {
                            defaultMarksError = e.message
                        }
                    },
                    label = { Text("Default Marks (e.g., A:10, B:5)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = defaultMarksError != null,
                    supportingText = { defaultMarksError?.let { Text(it) } }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedMarks = QuizTemplateParser.parseDefaultMarks(defaultMarks)
                    val template = QuizTemplate(
                        id = quizTemplate?.id ?: 0,
                        name = name.trim(),
                        numQuestions = numQuestions.toInt(),
                        defaultMarks = parsedMarks
                    )
                    onSave(template)
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
