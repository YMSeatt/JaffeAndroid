package com.example.myapplication.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.ConditionalFormattingRule
import com.example.myapplication.ui.dialogs.BulkEditConditionalRulesDialog
import com.example.myapplication.viewmodel.ConditionalFormattingRuleViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ConditionalFormattingScreen(viewModel: ConditionalFormattingRuleViewModel, onDismiss: () -> Unit) {
    val rules by viewModel.rules.observeAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var showBulkEditDialog by remember { mutableStateOf(false) }
    var selectedRule by remember { mutableStateOf<ConditionalFormattingRule?>(null) }
    var selectedRuleIds by remember { mutableStateOf(setOf<Int>()) }

    val isSelectionMode = selectedRuleIds.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSelectionMode) {
                        Text("${selectedRuleIds.size} Selected")
                    } else {
                        Text("Conditional Formatting")
                    }
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = { selectedRuleIds = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Selection")
                        }
                    } else {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = { showBulkEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Bulk Edit")
                        }
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
                .padding(horizontal = 16.dp)
        ) {
            items(rules) { rule ->
                val isSelected = selectedRuleIds.contains(rule.id)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .alpha(if (rule.enabled) 1.0f else 0.6f)
                        .combinedClickable(
                            onClick = {
                                if (isSelectionMode) {
                                    selectedRuleIds = if (isSelected) {
                                        selectedRuleIds - rule.id
                                    } else {
                                        selectedRuleIds + rule.id
                                    }
                                } else {
                                    selectedRule = rule
                                    showDialog = true
                                }
                            },
                            onLongClick = {
                                if (!isSelectionMode) {
                                    selectedRuleIds = setOf(rule.id)
                                }
                            }
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(rule.name, style = MaterialTheme.typography.titleMedium)
                            Text("Priority: ${rule.priority}", style = MaterialTheme.typography.bodySmall)
                            Text("Target: ${rule.targetType}", style = MaterialTheme.typography.bodySmall)
                            if (!rule.enabled) {
                                Text("Disabled", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        if (isSelectionMode) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    selectedRuleIds = if (checked == true) {
                                        selectedRuleIds + rule.id
                                    } else {
                                        selectedRuleIds - rule.id
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showDialog) {
            ConditionalFormattingRuleEditor(
                rule = selectedRule,
                viewModel = viewModel,
                onDismiss = { showDialog = false }
            )
        }

        if (showBulkEditDialog) {
            val selectedRules = rules.filter { selectedRuleIds.contains(it.id) }
            BulkEditConditionalRulesDialog(
                selectedRules = selectedRules,
                viewModel = viewModel,
                onDismiss = {
                    showBulkEditDialog = false
                    selectedRuleIds = emptySet()
                }
            )
        }
    }
}