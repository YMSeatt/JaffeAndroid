package com.example.myapplication.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    var showDialog by remember { mutableStateOf(false) }
    var selectedTemplate by remember { mutableStateOf<QuizTemplate?>(null) }

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
            FloatingActionButton(onClick = {
                selectedTemplate = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Quiz Template")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showDialog) {
                QuizTemplateEditDialog(
                    onDismiss = { showDialog = false },
                    onSave = {
                        if (it.id == 0) {
                            viewModel.insert(it)
                        } else {
                            viewModel.update(it)
                        }
                        showDialog = false
                    },
                    quizTemplate = selectedTemplate
                )
            }

            LazyColumn {
                items(quizTemplates) { template ->
                    ListItem(
                        headlineContent = { Text(template.name) },
                        supportingContent = { Text("${template.numQuestions} questions") },
                        trailingContent = {
                            Row {
                                IconButton(onClick = {
                                    selectedTemplate = template
                                    showDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { viewModel.delete(template) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
