package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun EmailDialog(
    onDismissRequest: () -> Unit,
    onSend: (String, String, String) -> Unit,
    fromAddress: String
) {
    var to by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Send Email") },
        text = {
            Column {
                TextField(
                    value = fromAddress,
                    onValueChange = { },
                    label = { Text("From") },
                    readOnly = true
                )
                TextField(
                    value = to,
                    onValueChange = { to = it },
                    label = { Text("To") }
                )
                TextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") }
                )
                TextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Body") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSend(to, subject, body)
                    onDismissRequest()
                }
            ) {
                Text("Send")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
