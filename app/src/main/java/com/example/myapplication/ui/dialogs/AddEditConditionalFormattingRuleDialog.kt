package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.ConditionalFormattingRule
import com.example.myapplication.viewmodel.ConditionalFormattingRuleViewModel

@Composable
fun AddEditConditionalFormattingRuleDialog(
    rule: ConditionalFormattingRule?,
    viewModel: ConditionalFormattingRuleViewModel,
    onDismiss: () -> Unit
) {
    var name by remember(rule) { mutableStateOf(rule?.name ?: "") }
    var conditionJson by remember(rule) { mutableStateOf(rule?.conditionJson ?: "") }
    var formatJson by remember(rule) { mutableStateOf(rule?.formatJson ?: "") }
    var targetType by remember(rule) { mutableStateOf(rule?.targetType ?: "") }
    var priority by remember(rule) { mutableStateOf(rule?.priority?.toString() ?: "0") }
    val isEditMode = rule != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditMode) "Edit Rule" else "Add Rule") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Rule Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = conditionJson,
                    onValueChange = { conditionJson = it },
                    label = { Text("Condition (JSON)") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = formatJson,
                    onValueChange = { formatJson = it },
                    label = { Text("Format (JSON)") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetType,
                    onValueChange = { targetType = it },
                    label = { Text("Target Type") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = priority,
                    onValueChange = { priority = it },
                    label = { Text("Priority") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newRule = ConditionalFormattingRule(
                        id = rule?.id ?: 0,
                        name = name,
                        conditionJson = conditionJson,
                        formatJson = formatJson,
                        targetType = targetType,
                        priority = priority.toIntOrNull() ?: 0
                    )
                    if (isEditMode) {
                        viewModel.updateRule(newRule)
                    } else {
                        viewModel.addRule(newRule)
                    }
                    onDismiss()
                }
            ) {
                Text(if (isEditMode) "Save" else "Add")
            }
        },
        dismissButton = {
            if (isEditMode) {
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
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
