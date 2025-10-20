package com.example.myapplication.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.Reminder
import com.example.myapplication.ui.dialogs.AddReminderDialog
import com.example.myapplication.ui.dialogs.EditReminderDialog
import com.example.myapplication.viewmodel.RemindersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(onDismiss: () -> Unit, remindersViewModel: RemindersViewModel = viewModel()) {
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var showEditReminderDialog by remember { mutableStateOf(false) }
    var selectedReminder by remember { mutableStateOf<Reminder?>(null) }
    val reminders by remindersViewModel.allReminders.observeAsState(emptyList())

    if (showAddReminderDialog) {
        AddReminderDialog(
            onDismiss = { showAddReminderDialog = false },
            onAddReminder = { reminder ->
                remindersViewModel.addReminder(reminder)
                showAddReminderDialog = false
            }
        )
    }

    if (showEditReminderDialog) {
        EditReminderDialog(
            reminder = selectedReminder!!,
            onDismiss = { showEditReminderDialog = false },
            onUpdateReminder = { reminder ->
                remindersViewModel.updateReminder(reminder)
                showEditReminderDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddReminderDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(reminders) { reminder ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = reminder.title)
                    Row {
                        IconButton(onClick = {
                            selectedReminder = reminder
                            showEditReminderDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Reminder")
                        }
                        IconButton(onClick = { remindersViewModel.deleteReminder(reminder) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Reminder")
                        }
                    }
                }
            }
        }
    }
}
