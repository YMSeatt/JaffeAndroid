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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.data.HomeworkMarkStep
import com.example.myapplication.data.HomeworkMarkType
import com.example.myapplication.data.HomeworkTemplate
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkTemplateEditDialog(
    onDismiss: () -> Unit,
    onSave: (HomeworkTemplate) -> Unit,
    template: HomeworkTemplate? = null
) {
    var name by remember { mutableStateOf(template?.name ?: "") }
    var steps by remember { mutableStateOf(template?.getSteps() ?: emptyList()) }
    var showAddStepResult by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (template == null) "Add Homework Template" else "Edit Homework Template") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Template Name") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )

                Text("Marking Steps:", style = MaterialTheme.typography.titleMedium)
                
                LazyColumn(modifier = Modifier.weight(1f, fill = false).padding(vertical = 8.dp)) {
                    items(steps) { step ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(step.label, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        "Type: ${step.type.name} ${if (step.type == HomeworkMarkType.SCORE) "(Max: ${step.maxValue})" else ""}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(onClick = { steps = steps - step }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Step")
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { showAddStepResult = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Step")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newTemplate = HomeworkTemplate.fromSteps(
                        name = name,
                        steps = steps,
                        id = template?.id ?: 0
                    )
                    onSave(newTemplate)
                },
                enabled = name.isNotBlank() && steps.isNotEmpty()
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

    if (showAddStepResult) {
        AddStepDialog(
            onDismiss = { showAddStepResult = false },
            onAdd = { newStep ->
                steps = steps + newStep
                showAddStepResult = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStepDialog(
    onDismiss: () -> Unit,
    onAdd: (HomeworkMarkStep) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(HomeworkMarkType.CHECKBOX) }
    var maxValue by remember { mutableStateOf("1") } // Default for checkbox is conceptually 1
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Add Marking Step", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (e.g., 'Homework Signed')") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        value = type.name,
                        onValueChange = {},
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        HomeworkMarkType.entries.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption.name) },
                                onClick = {
                                    type = selectionOption
                                    maxValue = if (selectionOption == HomeworkMarkType.SCORE) "5" else "1"
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                if (type == HomeworkMarkType.SCORE) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = maxValue,
                        onValueChange = { if (it.all { char -> char.isDigit() }) maxValue = it },
                        label = { Text("Max Score") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onAdd(
                                HomeworkMarkStep(
                                    label = label,
                                    type = type,
                                    maxValue = maxValue.toIntOrNull() ?: 1
                                )
                            )
                        },
                        enabled = label.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}
