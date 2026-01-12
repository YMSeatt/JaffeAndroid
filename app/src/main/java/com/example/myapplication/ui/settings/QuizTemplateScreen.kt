package com.example.myapplication.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.myapplication.data.QuizTemplate
import com.example.myapplication.ui.dialogs.QuizTemplateEditDialog
import com.example.myapplication.viewmodel.QuizTemplateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTemplateScreen(
    onDismiss: () -> Unit,
    viewModel: QuizTemplateViewModel = hiltViewModel()
) {
    val quizTemplates by viewModel.quizTemplates.collectAsState()
    var editingTemplate by remember { mutableStateOf<QuizTemplate?>(null) }
    var templateToDelete by remember { mutableStateOf<QuizTemplate?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Templates") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Quiz Template")
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(quizTemplates) { template ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = template.name)
                            Text(text = "(${template.numQuestions} Qs)")
                        }
                        Row {
                            IconButton(onClick = { editingTemplate = template }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { templateToDelete = template }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            QuizTemplateEditDialog(
                onDismiss = { showAddDialog = false },
                onSave = { quizTemplate ->
                    viewModel.insert(quizTemplate)
                    showAddDialog = false
                }
            )
        }

        editingTemplate?.let { template ->
            QuizTemplateEditDialog(
                quizTemplate = template,
                onDismiss = { editingTemplate = null },
                onSave = { updatedTemplate ->
                    viewModel.update(updatedTemplate)
                    editingTemplate = null
                }
            )
        }

        templateToDelete?.let { template ->
            AlertDialog(
                onDismissRequest = { templateToDelete = null },
                title = { Text("Delete Template") },
                text = { Text("Are you sure you want to delete '${template.name}'?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.delete(template)
                            templateToDelete = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { templateToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
