package com.example.myapplication.ui.dialogs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeSelected: (Long) -> Unit
) {
    val state = rememberTimePickerState()
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            androidx.compose.material3.Text("Select Time")
        },
        text = {
            TimePicker(state = state)
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, state.hour)
                    cal.set(Calendar.MINUTE, state.minute)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    onTimeSelected(cal.timeInMillis)
                }
            ) {
                androidx.compose.material3.Text("OK")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismissRequest
            ) {
                androidx.compose.material3.Text("Cancel")
            }
        }
    )
}