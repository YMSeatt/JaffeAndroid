package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.QuizMarkType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizMarkTypeEditDialog(
    onDismiss: () -> Unit,
    onSave: (QuizMarkType) -> Unit,
    markType: QuizMarkType? = null
) {
    var name by remember { mutableStateOf(markType?.name ?: "") }
    var defaultPoints by remember { mutableStateOf(markType?.defaultPoints?.toString() ?: "0.0") }
    var contributesToTotal by remember { mutableStateOf(markType?.contributesToTotal ?: true) }
    var isExtraCredit by remember { mutableStateOf(markType?.isExtraCredit ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (markType == null) "Add Quiz Mark Type" else "Edit Quiz Mark Type") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = defaultPoints,
                    onValueChange = { defaultPoints = it },
                    label = { Text("Default Points") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = contributesToTotal,
                        onCheckedChange = { contributesToTotal = it }
                    )
                    Text("Contributes to Total", style = MaterialTheme.typography.bodyLarge)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isExtraCredit,
                        onCheckedChange = { isExtraCredit = it }
                    )
                    Text("Is Extra Credit", style = MaterialTheme.typography.bodyLarge)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val points = defaultPoints.toDoubleOrNull() ?: 0.0
                    val newMarkType = QuizMarkType(
                        id = markType?.id ?: 0,
                        name = name,
                        defaultPoints = points,
                        contributesToTotal = contributesToTotal,
                        isExtraCredit = isExtraCredit
                    )
                    onSave(newMarkType)
                },
                enabled = name.isNotBlank()
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
