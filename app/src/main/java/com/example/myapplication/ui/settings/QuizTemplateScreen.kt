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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.dialogs.QuizTemplateEditDialog
import com.example.myapplication.viewmodel.QuizTemplateViewModel
import com.example.myapplication.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTemplateScreen(
    onDismiss: () -> Unit,
    viewModel: QuizTemplateViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val quizTemplates by viewModel.quizTemplates.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    val quizMarkTypes by settingsViewModel.quizMarkTypes.observeAsState(initial = emptyList())
    var selectedTemplate by remember { mutableStateOf<com.example.myapplication.data.QuizTemplate?>(null) }


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
                showEditDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Quiz Template")
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(quizTemplates) { template ->
                Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = template.name)
                            Text(text = "${template.numQuestions} questions")
                        }
                        Button(onClick = {
                            selectedTemplate = template
                            showEditDialog = true
                        }) {
                            Text("Edit")
                        }
                        Button(onClick = { viewModel.delete(template) }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }

        if (showEditDialog) {
            QuizTemplateEditDialog(
                onDismiss = { showEditDialog = false },
                onSave = { quizTemplate ->
                    if (selectedTemplate == null) {
                        viewModel.insert(quizTemplate)
                    } else {
                        viewModel.update(quizTemplate)
                    }
                    showEditDialog = false
                },
                quizTemplate = selectedTemplate,
                quizMarkTypes = quizMarkTypes
            )
        }
    }
}
