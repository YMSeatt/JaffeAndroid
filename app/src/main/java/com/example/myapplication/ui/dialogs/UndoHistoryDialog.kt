package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodel.SeatingChartViewModel

@Composable
fun UndoHistoryDialog(
    viewModel: SeatingChartViewModel,
    onDismissRequest: () -> Unit
) {
    val undoStack by viewModel.undoStackState.collectAsState()
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Undo History") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                Text(
                    "Select an action to return to. This will discard all subsequent changes.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (undoStack.isEmpty()) {
                    Text("No actions in history.", modifier = Modifier.padding(vertical = 16.dp))
                } else {
                    LazyColumn {
                        // Show in reverse order (newest first)
                        itemsIndexed(undoStack.reversed()) { reversedIndex, command ->
                            val originalIndex = undoStack.size - 1 - reversedIndex
                            ListItem(
                                headlineContent = {
                                    Text("${originalIndex + 1}: ${command.getDescription()}")
                                },
                                modifier = Modifier
                                    .clickable { selectedIndex = originalIndex }
                                    .fillMaxWidth(),
                                colors = if (selectedIndex == originalIndex) {
                                    ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                } else {
                                    ListItemDefaults.colors()
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { showConfirmDialog = true },
                enabled = selectedIndex != null
            ) {
                Text("Go to This Action")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )

    if (showConfirmDialog && selectedIndex != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Action") },
            text = {
                Text("This will revert the application to the selected point in history, discarding all changes made after it. This cannot be undone.\n\nAre you sure you want to proceed?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.selectiveUndo(selectedIndex!!)
                        showConfirmDialog = false
                        onDismissRequest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Proceed")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
