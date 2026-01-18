package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.QuizMarkType
import com.example.myapplication.data.QuizTemplate

@Composable
fun QuizTemplateEditDialog(
    onDismiss: () -> Unit,
    onSave: (QuizTemplate) -> Unit,
    quizTemplate: QuizTemplate? = null,
    quizMarkTypes: List<QuizMarkType>
) {
    var name by remember { mutableStateOf(quizTemplate?.name ?: "") }
    var numQuestions by remember { mutableStateOf(quizTemplate?.numQuestions?.toString() ?: "10") }
    val defaultMarks = remember {
        mutableStateOf(
            quizMarkTypes.associate {
                it.name to (quizTemplate?.defaultMarks?.get(it.name)?.toString() ?: it.defaultPoints.toString())
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (quizTemplate == null) "Add Quiz Template" else "Edit Quiz Template") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Template Name") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                TextField(
                    value = numQuestions,
                    onValueChange = { numQuestions = it },
                    label = { Text("Number of Questions") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                quizMarkTypes.forEach { markType ->
                    TextField(
                        value = defaultMarks.value[markType.name] ?: "",
                        onValueChange = {
                            defaultMarks.value = defaultMarks.value.toMutableMap().apply {
                                put(markType.name, it)
                            }
                        },
                        label = { Text(markType.name) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val marks = defaultMarks.value.mapValues { it.value.toIntOrNull() ?: 0 }
                    val template = QuizTemplate(
                        id = quizTemplate?.id ?: 0,
                        name = name,
                        numQuestions = numQuestions.toIntOrNull() ?: 0,
                        defaultMarks = marks
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
