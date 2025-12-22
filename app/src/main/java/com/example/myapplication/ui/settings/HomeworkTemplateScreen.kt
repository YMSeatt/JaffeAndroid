package com.example.myapplication.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.HomeworkTemplate
import com.example.myapplication.ui.dialogs.HomeworkTemplateEditDialog
import com.example.myapplication.viewmodel.HomeworkTemplateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkTemplateScreen(
    onDismiss: () -> Unit,
    viewModel: HomeworkTemplateViewModel = hiltViewModel()
) {
    val homeworkTemplates by viewModel.homeworkTemplates.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var templateToEdit by remember { mutableStateOf<HomeworkTemplate?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Homework Templates") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                templateToEdit = null
                showEditDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Homework Template")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            items(homeworkTemplates) { template ->
                ListItem(
                    headlineContent = { Text(template.name) },
                    trailingContent = {
                        IconButton(onClick = { viewModel.delete(template) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    },
                    modifier = Modifier.clickable {
                        templateToEdit = template
                        showEditDialog = true
                    }
                )
                HorizontalDivider()
            }
        }

        if (showEditDialog) {
            HomeworkTemplateEditDialog(
                template = templateToEdit,
                onDismiss = { showEditDialog = false },
                onSave = { template ->
                    if (templateToEdit == null) {
                        viewModel.insert(template)
                    } else {
                        viewModel.update(template)
                    }
                    showEditDialog = false
                }
            )
        }
    }
}
