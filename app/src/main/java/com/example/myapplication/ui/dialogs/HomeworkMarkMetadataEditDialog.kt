package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.HomeworkMarkMetadata

@Composable
fun HomeworkMarkMetadataEditDialog(
    onDismiss: () -> Unit,
    onSave: (HomeworkMarkMetadata) -> Unit,
    metadata: HomeworkMarkMetadata? = null
) {
    var name by remember { mutableStateOf(metadata?.name ?: "") }
    var defaultPoints by remember { mutableStateOf(metadata?.defaultPoints?.toString() ?: "0.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (metadata == null) "Add Homework Mark Type" else "Edit Homework Mark Type") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val points = defaultPoints.toDoubleOrNull() ?: 0.0
                    val newMetadata = HomeworkMarkMetadata(
                        id = metadata?.id ?: 0,
                        name = name,
                        defaultPoints = points
                    )
                    onSave(newMetadata)
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
