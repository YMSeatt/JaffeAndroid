package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.LayoutTemplate

@Composable
fun LoadLayoutDialog(
    layouts: List<LayoutTemplate>,
    onDismiss: () -> Unit,
    onLoad: (LayoutTemplate) -> Unit,
    onDelete: (LayoutTemplate) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Load Layout") },
        text = {
            Column {
                if (layouts.isEmpty()) {
                    Text("No saved layouts found.")
                } else {
                    LazyColumn {
                        items(layouts) { layout ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Button(onClick = { onLoad(layout) }) {
                                    Text(layout.name)
                                }
                                Button(onClick = { onDelete(layout) },
                                    modifier = Modifier.padding(start = 8.dp)) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}