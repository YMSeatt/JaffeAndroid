package com.example.myapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (showDialog) {
                QuizTemplateEditDialog(
                    quizTemplate = selectedTemplate,
                    onDismiss = { showDialog = false },
                    onSave = { template ->
                        if (template.id == 0) {
                            viewModel.insert(template)
                        } else {
                            viewModel.update(template)
                        }
                        showDialog = false
                    },
                    isNameDuplicate = { name, id ->
                        quizTemplates.any { it.name.equals(name, ignoreCase = true) && it.id != id }
                    }
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(quizTemplates) { template ->
                    QuizTemplateItem(
                        template = template,
                        onEdit = {
                            selectedTemplate = it
                            showDialog = true
                        },
                        onDelete = { viewModel.delete(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun QuizTemplateItem(
    template: QuizTemplate,
    onEdit: (QuizTemplate) -> Unit,
    onDelete: (QuizTemplate) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onEdit(template) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = template.name)
                Text(text = "${template.numQuestions} Questions")
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = { onDelete(template) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Template")
            }
        }
    }
}
