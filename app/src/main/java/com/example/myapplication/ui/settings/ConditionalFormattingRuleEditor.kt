package com.example.myapplication.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.ConditionalFormattingRule
import com.example.myapplication.viewmodel.ConditionalFormattingRuleViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionalFormattingRuleEditor(
    rule: ConditionalFormattingRule?,
    viewModel: ConditionalFormattingRuleViewModel,
    onDismiss: () -> Unit,
) {
    var name by remember(rule) { mutableStateOf(rule?.name ?: "") }
    var priority by remember(rule) { mutableStateOf(rule?.priority?.toString() ?: "0") }
    var ruleType by remember(rule) { mutableStateOf(rule?.type ?: "group") }
    var targetType by remember(rule) { mutableStateOf(rule?.targetType ?: "student") }
    var expanded by remember { mutableStateOf(false) }

    val conditionState = remember(rule, ruleType) {
        mutableStateOf(
            rule?.conditionJson?.let {
                gson.fromJson<Map<String, Any>>(it, object : TypeToken<Map<String, Any>>() {}.type)
            } ?: emptyMap()
        )
    }

    val formatState = remember(rule) {
        mutableStateOf(
            rule?.formatJson?.let {
                Gson().fromJson<Map<String, Any>>(it, object : TypeToken<Map<String, Any>>() {}.type)
            } ?: emptyMap()
        )
    }

    val ruleTypes = listOf(
        "group", "behavior_count", "quiz_score_threshold", "quiz_mark_count",
        "live_quiz_response", "live_homework_yes_no", "live_homework_select"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (rule != null) "Edit Rule" else "Add Rule") },
        text = {
            LazyColumn(modifier = Modifier.padding(vertical = 8.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Rule Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                    ) {
                        OutlinedTextField(
                            value = ruleType,
                            onValueChange = { },
                            label = { Text("Rule Type") },
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            ruleTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        ruleType = type
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Dynamic Condition Fields
                    when (ruleType) {
                        "group" -> {
                            OutlinedTextField(
                                value = conditionState.value["group_id"]?.toString() ?: "",
                                onValueChange = {
                                    conditionState.value = conditionState.value.toMutableMap().apply { put("group_id", it) }
                                },
                                label = { Text("Group ID") }
                            )
                        }
                        "behavior_count" -> {
                            OutlinedTextField(
                                value = conditionState.value["behavior_name"]?.toString() ?: "",
                                onValueChange = {
                                    conditionState.value = conditionState.value.toMutableMap().apply { put("behavior_name", it) }
                                },
                                label = { Text("Behavior Name") }
                            )
                            OutlinedTextField(
                                value = conditionState.value["count_threshold"]?.toString() ?: "1",
                                onValueChange = {
                                    conditionState.value = conditionState.value.toMutableMap().apply { put("count_threshold", it) }
                                },
                                label = { Text("Count Threshold") }
                            )
                            OutlinedTextField(
                                value = conditionState.value["time_window_hours"]?.toString() ?: "24",
                                onValueChange = {
                                    conditionState.value = conditionState.value.toMutableMap().apply { put("time_window_hours", it) }
                                },
                                label = { Text("Time Window (Hours)") }
                            )
                        }
                        "quiz_score_threshold" -> {
                            OutlinedTextField(
                                value = conditionState.value["quiz_name_contains"]?.toString() ?: "",
                                onValueChange = {
                                    conditionState.value = conditionState.value.toMutableMap().apply { put("quiz_name_contains", it) }
                                },
                                label = { Text("Quiz Name Contains") }
                            )
                            OutlinedTextField(
                                value = conditionState.value["operator"]?.toString() ?: "<=",
                                onValueChange = {
                                    conditionState.value = conditionState.value.toMutableMap().apply { put("operator", it) }
                                },
                                label = { Text("Operator") }
                            )
                            OutlinedTextField(
                                value = conditionState.value["score_threshold_percent"]?.toString() ?: "50.0",
                                onValueChange = {
                                    conditionState.value = conditionState.value.toMutableMap().apply { put("score_threshold_percent", it) }
                                },
                                label = { Text("Score Threshold (%)") }
                            )
                        }
                        "quiz_mark_count" -> {
                            OutlinedTextField(
                                value = conditionState.value["mark_type_id"]?.toString() ?: "",
                                onValueChange = {
                                    conditionState.value = conditionState.value.toMutableMap().apply { put("mark_type_id", it) }
                                },
                                label = { Text("Mark Type ID") }
                            )
                        }
                        "live_quiz_response" -> {
                            OutlinedTextField(
                                value = conditionState.value["quiz_response"]?.toString() ?: "Correct",
                                onValueChange = {
                                    conditionState.value = conditionState.value.toMutableMap().apply { put("quiz_response", it) }
                                },
                                label = { Text("Quiz Response") }
                            )
                        }
                        "live_homework_yes_no" -> {
                            OutlinedTextField(
                                value = conditionState.value["homework_type_id"]?.toString() ?: "",
                                onValueChange = {
                                    conditionState.value = conditionState.value.toMutableMap().apply { put("homework_type_id", it) }
                                },
                                label = { Text("Homework Type ID") }
                            )
                            OutlinedTextField(
                                value = conditionState.value["homework_response"]?.toString() ?: "yes",
                                onValueChange = {
                                    conditionState.value = conditionState.value.toMutableMap().apply { put("homework_response", it) }
                                },
                                label = { Text("Homework Response") }
                            )
                        }
                        "live_homework_select" -> {
                            OutlinedTextField(
                                value = conditionState.value["homework_option_name"]?.toString() ?: "",
                                onValueChange = {
                                    conditionState.value = conditionState.value.toMutableMap().apply { put("homework_option_name", it) }
                                },
                                label = { Text("Homework Option Name") }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Formatting", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = formatState.value["color"]?.toString() ?: "",
                        onValueChange = {
                            formatState.value = formatState.value.toMutableMap().apply { put("color", it) }
                        },
                        label = { Text("Fill Color") }
                    )
                    OutlinedTextField(
                        value = formatState.value["outline"]?.toString() ?: "",
                        onValueChange = {
                            formatState.value = formatState.value.toMutableMap().apply { put("outline", it) }
                        },
                        label = { Text("Outline Color") }
                    )
                    OutlinedTextField(
                        value = formatState.value["application_style"]?.toString() ?: "stripe",
                        onValueChange = {
                            formatState.value = formatState.value.toMutableMap().apply { put("application_style", it) }
                        },
                        label = { Text("Application Style") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = targetType,
                        onValueChange = { targetType = it },
                        label = { Text("Target Type") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = priority,
                        onValueChange = { priority = it },
                        label = { Text("Priority") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newRule = (rule ?: ConditionalFormattingRule()).copy(
                        name = name,
                        priority = priority.toIntOrNull() ?: 0,
                        type = ruleType,
                        targetType = targetType,
                        conditionJson = Gson().toJson(conditionState.value),
                        formatJson = Gson().toJson(formatState.value)
                    )
                    if (rule != null) {
                        viewModel.updateRule(newRule)
                    } else {
                        viewModel.addRule(newRule)
                    }
                    onDismiss()
                }
            ) {
                Text(if (rule != null) "Save" else "Add")
            }
        },
        dismissButton = {
            Row {
                if (rule != null) {
                    Button(
                        onClick = {
                            viewModel.deleteRule(rule)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}