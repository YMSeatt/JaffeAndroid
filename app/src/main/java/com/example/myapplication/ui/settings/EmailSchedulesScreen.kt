package com.example.myapplication.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.EmailSchedule
import com.example.myapplication.viewmodel.EmailSchedulesViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailSchedulesScreen(
    viewModel: EmailSchedulesViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val schedules by viewModel.schedules.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedSchedule by remember { mutableStateOf<EmailSchedule?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Email Schedules") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedSchedule = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Schedule")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn {
                items(schedules) { schedule ->
                    ScheduleItem(
                        schedule = schedule,
                        onEdit = {
                            selectedSchedule = it
                            showDialog = true
                        },
                        onDelete = {
                            viewModel.deleteSchedule(it)
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        ScheduleEditorDialog(
            schedule = selectedSchedule,
            onDismiss = { showDialog = false },
            onSave = {
                if (it.id == 0L) {
                    viewModel.addSchedule(it)
                } else {
                    viewModel.updateSchedule(it)
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun ScheduleItem(
    schedule: EmailSchedule,
    onEdit: (EmailSchedule) -> Unit,
    onDelete: (EmailSchedule) -> Unit
) {
    Card(modifier = Modifier.padding(8.dp)) {
        Row {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = String.format("%02d:%02d", schedule.hour, schedule.minute))
                Text(text = daysOfWeekToString(schedule.daysOfWeek))
                Text(text = "To: ${schedule.recipientEmail}")
            }
            IconButton(onClick = { onEdit(schedule) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = { onDelete(schedule) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

fun daysOfWeekToString(daysOfWeek: Int): String {
    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val selectedDays = mutableListOf<String>()
    for (i in days.indices) {
        if ((daysOfWeek and (1 shl i)) != 0) {
            selectedDays.add(days[i])
        }
    }
    return selectedDays.joinToString(", ")
}
