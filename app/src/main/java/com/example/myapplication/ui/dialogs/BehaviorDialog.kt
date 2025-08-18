package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BehaviorDialog(
    student: Student,
    viewModel: SeatingChartViewModel,
    behaviorTypes: List<String>,
    onDismiss: () -> Unit
) {
    var notes by remember { mutableStateOf("") }
    val selectedBehaviors = remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Behavior for ${student.firstName} ${student.lastName}") },
        text = {
            Column {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                Text("Select Behavior Types:")
                LazyColumn {
                    items(behaviorTypes) { behaviorType ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = behaviorType in selectedBehaviors.value,
                                onCheckedChange = { isChecked ->
                                    val currentBehaviors = selectedBehaviors.value.toMutableSet()
                                    if (isChecked) {
                                        currentBehaviors.add(behaviorType)
                                    } else {
                                        currentBehaviors.remove(behaviorType)
                                    }
                                    selectedBehaviors.value = currentBehaviors
                                }
                            )
                            Text(behaviorType)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val behaviorEvent = BehaviorEvent(
                    studentId = student.id,
                    comment = notes,
                    type = selectedBehaviors.value.joinToString(", "),
                    timestamp = System.currentTimeMillis()
                )
                viewModel.addBehaviorEvent(behaviorEvent)
                onDismiss()
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