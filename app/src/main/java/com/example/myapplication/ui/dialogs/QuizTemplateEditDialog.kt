package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.QuizTemplate

@Composable
fun QuizTemplateEditDialog(
    quizTemplate: QuizTemplate? = null, // Allow editing
    onDismiss: () -> Unit,
    onSave: (QuizTemplate) -> Unit
) {
    var name by remember { mutableStateOf(quizTemplate?.name ?: "") }
    var numQuestions by remember { mutableStateOf(quizTemplate?.numQuestions?.toString() ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var numQuestionsError by remember { mutableStateOf<String?>(null) }
    val nameErrorMessage = stringResource(R.string.quiz_template_edit_dialog_name_error)
    val numQuestionsErrorMessage = stringResource(R.string.quiz_template_edit_dialog_num_questions_error)


    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (quizTemplate == null) stringResource(R.string.quiz_template_edit_dialog_add_title)
                else stringResource(R.string.quiz_template_edit_dialog_edit_title)
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text(stringResource(R.string.quiz_template_edit_dialog_name_label)) },
                    isError = nameError != null,
                    singleLine = true
                )
                if (nameError != null) {
                    Text(text = nameError!!, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = numQuestions,
                    onValueChange = { numQuestions = it; numQuestionsError = null },
                    label = { Text(stringResource(R.string.quiz_template_edit_dialog_num_questions_label)) },
                    isError = numQuestionsError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                if (numQuestionsError != null) {
                    Text(text = numQuestionsError!!, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val questionsInt = numQuestions.toIntOrNull()
                    if (name.isBlank()) {
                        nameError = nameErrorMessage
                    }
                    if (questionsInt == null || questionsInt <= 0) {
                        numQuestionsError = numQuestionsErrorMessage
                    }

                    if (name.isNotBlank() && questionsInt != null && questionsInt > 0) {
                        val newQuizTemplate = quizTemplate?.copy(
                            name = name,
                            numQuestions = questionsInt
                        ) ?: QuizTemplate(
                            name = name,
                            numQuestions = questionsInt,
                            defaultMarks = emptyMap() // Keep it simple for now
                        )
                        onSave(newQuizTemplate)
                    }
                }
            ) {
                Text(stringResource(R.string.dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}
