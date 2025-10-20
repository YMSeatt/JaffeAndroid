package com.example.myapplication.ui.settings

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.data.EmailSchedule
import java.util.Calendar

@Composable
fun ScheduleEditorDialog(
    schedule: EmailSchedule?,
    onDismiss: () -> Unit,
    onSave: (EmailSchedule) -> Unit
) {
    val context = LocalContext.current
    var hour by remember { mutableStateOf(schedule?.hour ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableStateOf(schedule?.minute ?: Calendar.getInstance().get(Calendar.MINUTE)) }
    var daysOfWeek by remember { mutableStateOf(schedule?.daysOfWeek ?: 0) }
    var recipientEmail by remember { mutableStateOf(schedule?.recipientEmail ?: "") }
    var subject by remember { mutableStateOf(schedule?.subject ?: "") }
    var body by remember { mutableStateOf(schedule?.body ?: "") }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            hour = selectedHour
            minute = selectedMinute
        }, hour, minute, true
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(onClick = { timePickerDialog.show() }) {
                    Text(text = String.format("%02d:%02d", hour, minute))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Days of the week:")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    days.forEachIndexed { index, day ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(day)
                            Checkbox(
                                checked = (daysOfWeek and (1 shl index)) != 0,
                                onCheckedChange = {
                                    daysOfWeek = if (it) {
                                        daysOfWeek or (1 shl index)
                                    } else {
                                        daysOfWeek and (1 shl index).inv()
                                    }
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = recipientEmail,
                    onValueChange = { recipientEmail = it },
                    label = { Text("Recipient Email") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Body") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = {
                        val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                        val selectedDays = days.filterIndexed { index, _ ->
                            (daysOfWeek and (1 shl index)) != 0
                        }.toSet()
                        onSave(
                            EmailSchedule(
                                id = schedule?.id ?: 0,
                                hour = hour,
                                minute = minute,
                                daysOfWeek = daysOfWeek,
                                recipientEmail = recipientEmail,
                                subject = subject,
                                body = body,
                                enabled = schedule?.enabled ?: true,
                                days = selectedDays
                            )
                        )
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
