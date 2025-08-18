package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                Text("Select Behavior:")
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 128.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    items(behaviorTypes) { behaviorType ->
                        Button(
                            onClick = {
                                val behaviorEvent = BehaviorEvent(
                                    studentId = student.id,
                                    comment = notes,
                                    type = behaviorType,
                                    timestamp = System.currentTimeMillis()
                                )
                                viewModel.addBehaviorEvent(behaviorEvent)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(behaviorType)
                        }
                    }
                }
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}