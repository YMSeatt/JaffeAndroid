package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.myapplication.viewmodel.SettingsViewModel

@Composable
fun ArchiveViewerDialog(
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val archivedDatabases = viewModel.listArchivedDatabases()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Archived Databases") },
        text = {
            Column {
                LazyColumn {
                    items(archivedDatabases) { fileName ->
                        Button(onClick = {
                            viewModel.loadArchivedDatabase(fileName)
                            onDismiss()
                        }) {
                            Text(fileName)
                        }
                    }
                }
                Button(onClick = {
                    viewModel.restoreLiveDatabase()
                    onDismiss()
                }) {
                    Text("Restore Live Database")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}