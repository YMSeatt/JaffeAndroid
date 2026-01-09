package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    quizTemplate: QuizTemplate?,
    onDismiss: () -> Unit,
    onSave: (QuizTemplate) -> Unit,
    isNameDuplicate: (String, Int) -> Boolean
) {
    var name by remember { mutableStateOf(quizTemplate?.name ?: "") }
    var numQuestions by remember { mutableStateOf(quizTemplate?.numQuestions?.toString() ?: "10") }
    var defaultMarks by remember {
        mutableStateOf(
            quizTemplate?.let { QuizTemplateParser.formatDefaultMarks(it.defaultMarks) } ?: ""
        )
    }

    var nameError by remember { mutableStateOf<String?>(null) }
    var numQuestionsError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        val currentId = quizTemplate?.id ?: 0
        nameError = when {
            name.isBlank() -> "Name cannot be empty"
            isNameDuplicate(name, currentId) -> "This name is already used"
            else -> null
        }
        numQuestionsError = when {
            numQuestions.isBlank() -> "Cannot be empty"
            numQuestions.toIntOrNull() == null -> "Must be a valid number"
            numQuestions.toInt() <= 0 -> "Must be a positive number"
            else -> null
        }
        return nameError == null && numQuestionsError == null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (quizTemplate == null) "Add Quiz Template" else "Edit Quiz Template") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text("Template Name") },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = numQuestions,
                    onValueChange = { numQuestions = it; numQuestionsError = null },
                    label = { Text("Number of Questions") },
                    isError = numQuestionsError != null,
                    supportingText = { numQuestionsError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = defaultMarks,
                    onValueChange = { defaultMarks = it },
                    label = { Text("Default Marks") },
                    placeholder = { Text("e.g., Correct:1, Incorrect:0") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validate()) {
                        val parsedMarks = QuizTemplateParser.parseDefaultMarks(defaultMarks)
                        val newTemplate = QuizTemplate(
                            id = quizTemplate?.id ?: 0,
                            name = name.trim(),
                            numQuestions = numQuestions.toInt(),
                            defaultMarks = parsedMarks
                        )
                        onSave(newTemplate)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
