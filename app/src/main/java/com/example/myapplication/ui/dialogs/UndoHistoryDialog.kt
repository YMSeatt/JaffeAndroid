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
    val history = remember { viewModel.getUndoHistory().reversed() }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Undo History") },
        text = {
            Column(modifier = Modifier.heightIn(max = 400.dp)) {
                Text(
                    "Select an action to return to. This will discard all subsequent changes.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (history.isEmpty()) {
                    Text("No actions in history.", style = MaterialTheme.typography.bodySmall)
                } else {
                    LazyColumn {
                        itemsIndexed(history) { index, description ->
                            val actualIndex = history.size - 1 - index
                            ListItem(
                                headlineContent = { Text("${actualIndex + 1}: $description") },
                                modifier = Modifier
                                    .clickable { selectedIndex = actualIndex }
                                    .fillMaxWidth(),
                                colors = if (selectedIndex == actualIndex) {
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
            TextButton(
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

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Action") },
            text = {
                Text("This will revert the application to the selected point in history, discarding all changes made after it. This cannot be undone.\n\nAre you sure you want to proceed?")
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedIndex?.let { viewModel.undoToStep(it) }
                    showConfirmDialog = false
                    onDismissRequest()
                }) {
                    Text("Revert")
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
