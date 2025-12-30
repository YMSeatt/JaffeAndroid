package com.example.myapplication.ui.settings

import androidx.compose.foundation.layout.Arrangement
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
    viewModel: QuizTemplateViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val templates by viewModel.quizTemplates.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedTemplate by remember { mutableStateOf<QuizTemplate?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Quiz Templates") },
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
                showEditDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Quiz Template")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(templates) { template ->
                QuizTemplateItem(
                    template = template,
                    onEdit = {
                        selectedTemplate = it
                        showEditDialog = true
                    },
                    onDelete = { viewModel.delete(it) }
                )
            }
        }
    }

    if (showEditDialog) {
        QuizTemplateEditDialog(
            onDismiss = { showEditDialog = false },
            onSave = {
                if (it.id == 0) {
                    viewModel.insert(it)
                } else {
                    viewModel.update(it)
                }
                showEditDialog = false
            },
            quizTemplate = selectedTemplate
        )
    }
}

@Composable
fun QuizTemplateItem(
    template: QuizTemplate,
    onEdit: (QuizTemplate) -> Unit,
    onDelete: (QuizTemplate) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Name: ${template.name}")
            Text("Questions: ${template.numQuestions}")
            Text("Default Marks: ${template.defaultMarks.map { "${it.key}:${it.value}" }.joinToString(", ")}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { onEdit(template) }) {
                    Text("Edit")
                }
                Button(
                    onClick = { onDelete(template) },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}
