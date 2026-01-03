package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
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
    var nameError by remember { mutableStateOf<String?>(null) }

    var numQuestions by remember { mutableStateOf(quizTemplate?.numQuestions?.toString() ?: "") }
    var numQuestionsError by remember { mutableStateOf<String?>(null) }

    val marksString = quizTemplate?.defaultMarks?.map { "${it.key}:${it.value}" }?.joinToString(", ") ?: ""
    var defaultMarks by remember { mutableStateOf(marksString) }

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
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                TextField(
                    value = numQuestions,
                    onValueChange = {
                        numQuestions = it
                        numQuestionsError = null
                    },
                    label = { Text("Number of Questions") },
                    isError = numQuestionsError != null,
                    supportingText = { numQuestionsError?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                TextField(
                    value = defaultMarks,
                    onValueChange = { defaultMarks = it },
                    label = { Text("Default Marks (e.g., A:10, B:5)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var isValid = true
                    if (name.isBlank()) {
                        nameError = "Template name cannot be empty."
                        isValid = false
                    }

                    val questionsInt = numQuestions.toIntOrNull()
                    if (questionsInt == null || questionsInt <= 0) {
                        numQuestionsError = "Number of questions must be a positive integer."
                        isValid = false
                    }

                    if (isValid) {
                        val marksMap = defaultMarks.split(',')
                            .mapNotNull { it.trim().split(':').takeIf { parts -> parts.size == 2 } }
                            .mapNotNull { parts ->
                                val key = parts[0].trim()
                                val value = parts[1].trim().toIntOrNull()
                                if (key.isNotBlank() && value != null) key to value else null
                            }
                            .toMap()

                        val template = QuizTemplate(
                            id = quizTemplate?.id ?: 0,
                            name = name,
                            numQuestions = questionsInt!!,
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
