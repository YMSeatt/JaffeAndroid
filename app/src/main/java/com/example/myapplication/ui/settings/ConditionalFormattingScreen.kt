package com.example.myapplication.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.ConditionalFormattingRule
import com.example.myapplication.ui.dialogs.AddEditConditionalFormattingRuleDialog
import com.example.myapplication.viewmodel.ConditionalFormattingRuleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionalFormattingScreen(viewModel: ConditionalFormattingRuleViewModel, onDismiss: () -> Unit) {
    val rules by viewModel.rules.observeAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var selectedRule by remember { mutableStateOf<ConditionalFormattingRule?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conditional Formatting") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedRule = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Rule")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(rules) { rule ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            selectedRule = rule
                            showDialog = true
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(rule.name, style = MaterialTheme.typography.titleMedium)
                        Text("Priority: ${rule.priority}", style = MaterialTheme.typography.bodySmall)
                        Text("Target: ${rule.targetType}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        if (showDialog) {
            AddEditConditionalFormattingRuleDialog(
                rule = selectedRule,
                viewModel = viewModel,
                onDismiss = { showDialog = false }
            )
        }
    }
}