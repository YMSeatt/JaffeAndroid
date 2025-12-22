package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp

@Composable
fun ManageLiveHomeworkOptionsDialog(
    currentOptions: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var optionsList by remember { mutableStateOf(currentOptions.split(",").filter { it.isNotBlank() }) }
    var newOption by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Live Homework Options") },
        text = {
            Column {
                Text("Add or remove options for the 'Select' mode.")
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(optionsList) { option ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(option)
                            IconButton(onClick = {
                                optionsList = optionsList - option
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove option")
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = newOption,
                    onValueChange = { newOption = it; error = null },
                    label = { Text("New Option") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null
                )
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = {
                        if (newOption.isNotBlank()) {
                            if (optionsList.contains(newOption.trim())) {
                                error = "Option already exists."
                            } else {
                                optionsList = optionsList + newOption.trim()
                                newOption = ""
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    enabled = newOption.isNotBlank()
                ) {
                    Text("Add Option")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(optionsList.joinToString(","))
            }) {
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
