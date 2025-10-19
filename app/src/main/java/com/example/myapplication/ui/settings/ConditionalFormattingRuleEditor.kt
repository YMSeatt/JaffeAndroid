package com.example.myapplication.ui.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.myapplication.ui.components.MultiSelectDropdown
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.ConditionalFormattingRule
import com.example.myapplication.ui.components.ColorPickerField
import com.example.myapplication.utils.safeParseColor
import com.example.myapplication.viewmodel.ConditionalFormattingRuleViewModel
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
    val json = Json { ignoreUnknownKeys = true; isLenient = true }
    val showColorPicker = remember { mutableStateOf(false) }
    var colorPickerTarget by remember { mutableStateOf<String?>(null) }
    val customBehaviors by viewModel.customBehaviors.observeAsState(initial = emptyList())
    val systemBehaviors by viewModel.systemBehaviors.observeAsState(initial = emptyList())

    val conditionState = remember(rule, ruleType) {
        val initialJson = rule?.conditionJson
        val initialMap: Map<String, String> = if (!initialJson.isNullOrEmpty()) {
            json.decodeFromString<Map<String, String>>(initialJson)
        } else {
            emptyMap()
        }
        mutableStateOf(initialMap)
    }

    val formatState = remember(rule) {
        val initialJson = rule?.formatJson
        val initialMap: Map<String, String> = if (!initialJson.isNullOrEmpty()) {
            json.decodeFromString<Map<String, String>>(initialJson)
        } else {
            emptyMap()
        }
        mutableStateOf(initialMap)
    }

    if (showColorPicker.value) {
        val initialColorStr = formatState.value[colorPickerTarget] ?: ""
        val initialColor = if (initialColorStr.isBlank()) Color.White else safeParseColor(initialColorStr)
        ColorPickerDialog(
            onColorSelected = { color ->
                colorPickerTarget?.let { target ->
                    formatState.value = formatState.value + (target to color)
                }
                showColorPicker.value = false
            },
            onDismiss = { showColorPicker.value = false },
            initialColor = initialColor
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
                                .menuAnchor(MenuAnchorType.PrimaryEditable, true)
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
                                value = conditionState.value["group_id"] ?: "",
                                onValueChange = {
                                    conditionState.value = conditionState.value + ("group_id" to it)
                                },
                                label = { Text("Group ID") }
                            )
                        }
                        "behavior_count" -> {
                            val allBehaviors =
                                customBehaviors.map { it.name } + systemBehaviors.map { it.name }
                            MultiSelectDropdown(
                                options = allBehaviors,
                                selectedOptions = conditionState.value["behavior_names"]?.split(",")
                                    ?: emptyList(),
                                onSelectionChanged = {
                                    conditionState.value =
                                        conditionState.value + ("behavior_names" to it.joinToString(","))
                                },
                                label = "Behaviors"
                            )
                            OutlinedTextField(
                                value = conditionState.value["count_threshold"] ?: "1",
                                onValueChange = {
                                    conditionState.value = conditionState.value + ("count_threshold" to it)
                                },
                                label = { Text("Count Threshold") }
                            )
                            OutlinedTextField(
                                value = conditionState.value["time_window_hours"] ?: "24",
                                onValueChange = {
                                    conditionState.value = conditionState.value + ("time_window_hours" to it)
                                },
                                label = { Text("Time Window (Hours)") }
                            )
                        }
                        "quiz_score_threshold" -> {
                            OutlinedTextField(
                                value = conditionState.value["quiz_name_contains"] ?: "",
                                onValueChange = {
                                    conditionState.value = conditionState.value + ("quiz_name_contains" to it)
                                },
                                label = { Text("Quiz Name Contains") }
                            )
                            OutlinedTextField(
                                value = conditionState.value["operator"] ?: "<=",
                                onValueChange = {
                                    conditionState.value = conditionState.value + ("operator" to it)
                                },
                                label = { Text("Operator") }
                            )
                            OutlinedTextField(
                                value = conditionState.value["score_threshold_percent"] ?: "50.0",
                                onValueChange = {
                                    conditionState.value = conditionState.value + ("score_threshold_percent" to it)
                                },
                                label = { Text("Score Threshold (%)") }
                            )
                        }
                        "quiz_mark_count" -> {
                            OutlinedTextField(
                                value = conditionState.value["mark_type_id"] ?: "",
                                onValueChange = {
                                    conditionState.value = conditionState.value + ("mark_type_id" to it)
                                },
                                label = { Text("Mark Type ID") }
                            )
                        }
                        "live_quiz_response" -> {
                            OutlinedTextField(
                                value = conditionState.value["quiz_response"] ?: "Correct",
                                onValueChange = {
                                    conditionState.value = conditionState.value + ("quiz_response" to it)
                                },
                                label = { Text("Quiz Response") }
                            )
                        }
                        "live_homework_yes_no" -> {
                            OutlinedTextField(
                                value = conditionState.value["homework_type_id"] ?: "",
                                onValueChange = {
                                    conditionState.value = conditionState.value + ("homework_type_id" to it)
                                },
                                label = { Text("Homework Type ID") }
                            )
                            OutlinedTextField(
                                value = conditionState.value["homework_response"] ?: "yes",
                                onValueChange = {
                                    conditionState.value = conditionState.value + ("homework_response" to it)
                                },
                                label = { Text("Homework Response") }
                            )
                        }
                        "live_homework_select" -> {
                            OutlinedTextField(
                                value = conditionState.value["homework_option_name"] ?: "",
                                onValueChange = {
                                    conditionState.value = conditionState.value + ("homework_option_name" to it)
                                },
                                label = { Text("Homework Option Name") }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Formatting", style = MaterialTheme.typography.titleMedium)
                    ColorPickerField(
                        label = "Fill Color",
                        color = formatState.value["color"] ?: "",
                        onColorChange = { formatState.value = formatState.value + ("color" to it) },
                        onColorPickerClick = {
                            colorPickerTarget = "color"
                            showColorPicker.value = true
                        }
                    )
                    ColorPickerField(
                        label = "Outline Color",
                        color = formatState.value["outline"] ?: "",
                        onColorChange = { formatState.value = formatState.value + ("outline" to it) },
                        onColorPickerClick = {
                            colorPickerTarget = "outline"
                            showColorPicker.value = true
                        }
                    )
                    OutlinedTextField(
                        value = formatState.value["application_style"] ?: "stripe",
                        onValueChange = {
                            formatState.value = formatState.value + ("application_style" to it)
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
                        conditionJson = json.encodeToString(conditionState.value),
                        formatJson = json.encodeToString(formatState.value)
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


@Composable
fun ColorPickerDialog(
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    initialColor: Color = Color.White
) {
    val controller = rememberColorPickerController()
    var color by remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.color_picker_dialog_title)) },
        text = {
            HsvColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                controller = controller,
                onColorChanged = { colorEnvelope: ColorEnvelope ->
                    color = colorEnvelope.color
                }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onColorSelected(String.format("#%08X", color.toArgb()))
                }
            ) {
                Text(stringResource(R.string.color_picker_dialog_ok))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.color_picker_dialog_cancel))
            }
        }
    )
}
