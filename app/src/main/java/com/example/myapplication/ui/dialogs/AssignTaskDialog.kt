package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.SystemBehavior
import com.example.myapplication.viewmodel.SeatingChartViewModel
import androidx.compose.material3.AlertDialog

@Composable
fun AssignTaskDialog(
    studentId: Long,
    viewModel: SeatingChartViewModel,
    systemBehaviors: List<SystemBehavior>,
    onDismissRequest: () -> Unit
) {
    var selectedOption by remember { mutableStateOf("system") }
    var customTask by remember { mutableStateOf("") }
    var selectedSystemBehavior by remember { mutableStateOf<SystemBehavior?>(null) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Assign Task") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedOption == "system",
                        onClick = { selectedOption = "system" }
                    )
                    Text("Use System Behavior")
                }

                if (selectedOption == "system") {
                    LazyColumn {
                        items(systemBehaviors) { behavior ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedSystemBehavior == behavior,
                                    onClick = { selectedSystemBehavior = behavior }
                                )
                                Text(behavior.name)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedOption == "custom",
                        onClick = { selectedOption = "custom" }
                    )
                    Text("Create Custom Task")
                }

                if (selectedOption == "custom") {
                    TextField(
                        value = customTask,
                        onValueChange = { customTask = it },
                        label = { Text("Task Description") }
                    )
                }
            }
        },
        confirmButton = {
            Row {
                Button(
                    onClick = {
                        val task = if (selectedOption == "system") {
                            selectedSystemBehavior?.name
                        } else {
                            customTask
                        }
                        task?.let {
                            viewModel.assignTaskToStudent(studentId, it)
                        }
                        onDismissRequest()
                    },
                enabled = (selectedOption == "system" && selectedSystemBehavior != null) || (selectedOption == "custom" && customTask.isNotBlank()) || (selectedOption == "system" && systemBehaviors.isEmpty())
                ) {
                    Text("Assign")
                }
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}