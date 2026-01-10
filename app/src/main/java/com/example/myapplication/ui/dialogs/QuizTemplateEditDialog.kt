package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.MaterialTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTemplateEditDialog(
    template: QuizTemplate?,
    onDismiss: () -> Unit,
    onSave: (QuizTemplate) -> Unit
) {
    var name by remember { mutableStateOf(template?.name ?: "") }
    var numQuestions by remember { mutableStateOf(template?.numQuestions?.toString() ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var numQuestionsError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (template == null) "Add Template" else "Edit Template") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text("Template Name") },
                    isError = nameError != null
                )
                nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = numQuestions,
                    onValueChange = { numQuestions = it; numQuestionsError = null },
                    label = { Text("Number of Questions") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = numQuestionsError != null
                )
                numQuestionsError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = {
                val numQuestionsInt = numQuestions.toIntOrNull()
                if (name.isBlank()) {
                    nameError = "Name cannot be empty"
                } else if (numQuestionsInt == null || numQuestionsInt <= 0) {
                    numQuestionsError = "Please enter a valid number of questions"
                } else {
                    onSave(
                        template?.copy(
                            name = name,
                            numQuestions = numQuestionsInt
                        ) ?: QuizTemplate(
                            name = name,
                            numQuestions = numQuestionsInt,
                            defaultMarks = emptyMap()
                        )
                    )
                }
            }) {
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
