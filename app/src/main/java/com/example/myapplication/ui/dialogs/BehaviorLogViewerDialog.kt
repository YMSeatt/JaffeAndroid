package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.viewmodel.SeatingChartViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BehaviorLogViewerDialog(
    studentId: Long,
    viewModel: SeatingChartViewModel,
    onDismiss: () -> Unit
) {
    val behaviorEvents by viewModel.allBehaviorEvents.observeAsState(emptyList())
    var selectedEvent by remember { mutableStateOf<BehaviorEvent?>(null) }
    var notes by remember { mutableStateOf("") }

    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Behavior Log") },
        text = {
            LazyColumn {
                items(behaviorEvents.filter { it.studentId == studentId }) { event ->
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Text(
                            text = "${sdf.format(Date(event.timestamp))} - ${event.type}: ${event.comment ?: ""}",
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = {
                            selectedEvent = event
                            notes = event.comment ?: ""
                        }) {
                            Text("Edit")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )

    if (selectedEvent != null) {
        AlertDialog(
            onDismissRequest = { selectedEvent = null },
            title = { Text("Edit Note") },
            text = {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedEvent?.let {
                            val updatedEvent = it.copy(comment = notes)
                            viewModel.updateBehaviorEvent(updatedEvent)
                        }
                        selectedEvent = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { selectedEvent = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
