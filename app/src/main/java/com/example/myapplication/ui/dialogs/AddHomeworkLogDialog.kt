package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodel.HomeworkLogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHomeworkLogDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    homeworkLogViewModel: HomeworkLogViewModel // Pass the ViewModel to call its methods
) {
    if (showDialog) {
        var assignmentName by remember { mutableStateOf("") }
        var status by remember { mutableStateOf("") } // Consider making this a dropdown/selector later
        var comment by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Add Homework Log") },
            text = {
                Column(modifier = Modifier.padding(8.dp)) {
                    OutlinedTextField(
                        value = assignmentName,
                        onValueChange = { assignmentName = it },
                        label = { Text("Assignment Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = status,
                        onValueChange = { status = it },
                        label = { Text("Status (e.g., Completed, Pending)") }, // Placeholder label
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Comment (Optional)") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (assignmentName.isNotBlank() && status.isNotBlank()) {
                            homeworkLogViewModel.addHomeworkLog(
                                assignmentName = assignmentName,
                                status = status,
                                comment = comment.takeIf { it.isNotBlank() }
                            )
                            onDismiss() // Close dialog after adding
                        } // else: handle validation error, e.g., show a Toast or highlight fields
                    }
                ) {
                    Text("Add Log")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
