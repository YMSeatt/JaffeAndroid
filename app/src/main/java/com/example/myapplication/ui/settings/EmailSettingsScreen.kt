package com.example.myapplication.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.EmailSchedule
import com.example.myapplication.viewmodel.EmailSchedulesViewModel
import com.example.myapplication.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailSettingsScreen(
    settingsViewModel: SettingsViewModel,
    emailSchedulesViewModel: EmailSchedulesViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val defaultEmail by settingsViewModel.defaultEmailAddress.collectAsState()
    val emailPassword by settingsViewModel.emailPassword.collectAsState()
    val autoSendOnClose by settingsViewModel.autoSendEmailOnClose.collectAsState()
    val schedules by emailSchedulesViewModel.schedules.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedSchedule by remember { mutableStateOf<EmailSchedule?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Email Settings") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(16.dp)
        ) {
            TextField(
                value = defaultEmail,
                onValueChange = { settingsViewModel.updateDefaultEmailAddress(it) },
                label = { Text("Default Email Address") }
            )
            Spacer(modifier = Modifier.width(16.dp))
            TextField(
                value = emailPassword,
                onValueChange = { settingsViewModel.updateEmailPassword(it) },
                label = { Text("Email Password") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Auto-send on Close")
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = autoSendOnClose,
                    onCheckedChange = { settingsViewModel.updateAutoSendEmailOnClose(it) }
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Scheduled Email Times")
            LazyColumn {
                items(schedules) { schedule ->
                    ScheduleItem(
                        schedule = schedule,
                        onEdit = {
                            selectedSchedule = it
                            showDialog = true
                        },
                        onDelete = {
                            emailSchedulesViewModel.deleteSchedule(it)
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
                    emailSchedulesViewModel.addSchedule(it)
                } else {
                    emailSchedulesViewModel.updateSchedule(it)
                }
                showDialog = false
            }
        )
    }
}