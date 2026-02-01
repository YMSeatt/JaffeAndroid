package com.example.myapplication.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.QuizMarkType
import com.example.myapplication.ui.dialogs.QuizMarkTypeEditDialog
import com.example.myapplication.viewmodel.QuizMarkTypeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizMarkTypeScreen(
    onDismiss: () -> Unit,
    viewModel: QuizMarkTypeViewModel = hiltViewModel()
) {
    val markTypes by viewModel.quizMarkTypes.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedMarkType by remember { mutableStateOf<QuizMarkType?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Mark Types") },
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
                selectedMarkType = null
                showEditDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Mark Type")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (markTypes.isEmpty()) {
                Text("No mark types defined.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(markTypes) { markType ->
                        ListItem(
                            headlineContent = { Text(markType.name) },
                            supportingContent = {
                                Text("Points: ${markType.defaultPoints}, Total: ${markType.contributesToTotal}, Bonus: ${markType.isExtraCredit}")
                            },
                            trailingContent = {
                                IconButton(onClick = { viewModel.delete(markType) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            },
                            modifier = Modifier.clickable {
                                selectedMarkType = markType
                                showEditDialog = true
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }

        if (showEditDialog) {
            QuizMarkTypeEditDialog(
                onDismiss = { showEditDialog = false },
                onSave = { markType ->
                    if (markType.id == 0L) {
                        viewModel.insert(markType)
                    } else {
                        viewModel.update(markType)
                    }
                    showEditDialog = false
                },
                markType = selectedMarkType
            )
        }

        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                title = { Text("Reset to Defaults") },
                text = { Text("Are you sure you want to add the default quiz mark types?") },
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
