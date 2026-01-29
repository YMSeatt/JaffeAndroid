package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Reminder
import com.example.myapplication.ui.dialogs.DatePickerDialog
import com.example.myapplication.ui.dialogs.TimePickerDialog
import com.example.myapplication.viewmodel.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: ReminderViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your
            // app.
        } else {
            // Explain to the user that the feature is unavailable because the
            // feature requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision.
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<Reminder?>(null) }
    val reminders by viewModel.allReminders.observeAsState(initial = emptyList())

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permission Required") },
            text = { Text("To schedule reminders, please grant the 'Alarms & reminders' permission in the app settings.") },
            confirmButton = {
                Button(onClick = {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                    showPermissionDialog = false
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                Button(onClick = { showPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (viewModel.canScheduleExactAlarms()) {
                    editingReminder = null
                    showAddEditDialog = true
                } else {
                    showPermissionDialog = true
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(reminders) { reminder ->
                ReminderItem(
                    reminder = reminder,
                    onEdit = {
                        editingReminder = it
                        showAddEditDialog = true
                    },
                    onDelete = { viewModel.delete(it.id) }
                )
            }
        }

        if (showAddEditDialog) {
            AddEditReminderDialog(
                reminder = editingReminder,
                onDismiss = { showAddEditDialog = false },
                onSave = {
                    if (editingReminder == null) {
                        viewModel.insert(it)
                    } else {
                        viewModel.update(it)
                    }
                    showAddEditDialog = false
                }
            )
        }
    }
}

@Composable
fun ReminderItem(
    reminder: Reminder,
    onEdit: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = reminder.title)
                Text(text = reminder.description)
                Text(text = "Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(reminder.timestamp))}")
            }
            IconButton(onClick = { onEdit(reminder) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Reminder")
            }
            IconButton(onClick = { onDelete(reminder) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Reminder")
            }
        }
    }
}

@Composable
fun AddEditReminderDialog(
    reminder: Reminder?,
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit
) {
    var title by remember { mutableStateOf(reminder?.title ?: "") }
    var description by remember { mutableStateOf(reminder?.description ?: "") }
    var timestamp by remember { mutableStateOf(reminder?.timestamp ?: System.currentTimeMillis()) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { newDate ->
                val calendar = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
                val newCalendar = java.util.Calendar.getInstance().apply { timeInMillis = newDate }
                calendar.set(
                    newCalendar.get(java.util.Calendar.YEAR),
                    newCalendar.get(java.util.Calendar.MONTH),
                    newCalendar.get(java.util.Calendar.DAY_OF_MONTH)
                )
                timestamp = calendar.timeInMillis
                showDatePicker = false
                showTimePicker = true
            }
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onTimeSelected = { newTime ->
                val calendar = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
                val newCalendar = java.util.Calendar.getInstance().apply { timeInMillis = newTime }
                calendar.set(
                    java.util.Calendar.HOUR_OF_DAY,
                    newCalendar.get(java.util.Calendar.HOUR_OF_DAY)
                )
                calendar.set(
                    java.util.Calendar.MINUTE,
                    newCalendar.get(java.util.Calendar.MINUTE)
                )
                timestamp = calendar.timeInMillis
                showTimePicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (reminder == null) "Add Reminder" else "Edit Reminder") },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { showDatePicker = true }) {
                    Text("Select Date and Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))}")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newReminder = reminder?.copy(
                        title = title,
                        description = description,
                        timestamp = timestamp
                    ) ?: Reminder(
                        title = title,
                        description = description,
                        timestamp = timestamp
                    )
                    onSave(newReminder)
                },
                enabled = title.isNotBlank() && description.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}