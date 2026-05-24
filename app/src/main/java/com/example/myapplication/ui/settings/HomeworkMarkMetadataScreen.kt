package com.example.myapplication.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.HomeworkMarkMetadata
import com.example.myapplication.ui.dialogs.HomeworkMarkMetadataEditDialog
import com.example.myapplication.viewmodel.HomeworkMarkMetadataViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkMarkMetadataScreen(
    onDismiss: () -> Unit,
    viewModel: HomeworkMarkMetadataViewModel = hiltViewModel()
) {
    val metadataList by viewModel.homeworkMarkMetadata.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedMetadata by remember { mutableStateOf<HomeworkMarkMetadata?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Homework Mark Types") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showResetConfirm = true }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset to Defaults")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedMetadata = null
                showEditDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Mark Type")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (metadataList.isEmpty()) {
                Text("No homework mark types defined.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(metadataList) { metadata ->
                        ListItem(
                            headlineContent = { Text(metadata.name) },
                            supportingContent = {
                                Text("Points: ${metadata.defaultPoints}")
                            },
                            trailingContent = {
                                IconButton(onClick = { viewModel.delete(metadata) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            },
                            modifier = Modifier.clickable {
                                selectedMetadata = metadata
                                showEditDialog = true
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }

        if (showEditDialog) {
            HomeworkMarkMetadataEditDialog(
                onDismiss = { showEditDialog = false },
                onSave = { metadata ->
                    if (metadata.id == 0L) {
                        viewModel.insert(metadata)
                    } else {
                        viewModel.update(metadata)
                    }
                    showEditDialog = false
                },
                metadata = selectedMetadata
            )
        }

        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                title = { Text("Reset to Defaults") },
                text = { Text("Are you sure you want to add the default homework mark types?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.resetToDefaults()
                        showResetConfirm = false
                    }) {
                        Text("Reset")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
