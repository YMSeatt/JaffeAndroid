package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.QuizMarkType
import com.example.myapplication.data.QuizTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTemplateEditDialog(
    onDismiss: () -> Unit,
    onSave: (QuizTemplate) -> Unit,
    quizTemplate: QuizTemplate? = null,
    markTypes: List<QuizMarkType> = emptyList()
) {
    var name by remember { mutableStateOf(quizTemplate?.name ?: "") }
    var numQuestions by remember { mutableStateOf(quizTemplate?.numQuestions?.toString() ?: "") }

    // marksData: { markTypeName: points }
    val marksData = remember {
        mutableStateMapOf<String, String>().apply {
            quizTemplate?.defaultMarks?.forEach { (k, v) -> put(k, v.toString()) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (quizTemplate == null) "Add Quiz Template" else "Edit Quiz Template") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Template Name") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = numQuestions,
                    onValueChange = { numQuestions = it },
                    label = { Text("Number of Questions") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )

                if (markTypes.isNotEmpty()) {
                    Text("Default Marks (optional):", style = MaterialTheme.typography.titleMedium)
                    LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                        items(markTypes) { markType ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(markType.name, modifier = Modifier.weight(1f))
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = marksData[markType.name] ?: "",
                                    onValueChange = { marksData[markType.name] = it },
                                    label = { Text("Pts") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.width(100.dp)
                                )
                            }
                        }
                    }
                } else {
                    Text("No Quiz Mark Types defined. You can manage them in Settings.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val marksMap = marksData.mapNotNull { (k, v) ->
                        val value = v.toIntOrNull()
                        if (value != null) k to value else null
                    }.toMap()

                    val template = QuizTemplate(
                        id = quizTemplate?.id ?: 0,
                        name = name,
                        numQuestions = numQuestions.toIntOrNull() ?: 0,
                        defaultMarks = marksMap
                    )
                    onSave(template)
                },
                enabled = name.isNotBlank() && numQuestions.isNotBlank()
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
