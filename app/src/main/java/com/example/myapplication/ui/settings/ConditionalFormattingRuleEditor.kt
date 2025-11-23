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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.ConditionalFormattingRule
import com.example.myapplication.ui.components.ColorPickerField
import com.example.myapplication.ui.components.MultiSelectDropdown
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
    val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }
    val showColorPicker = remember { mutableStateOf(false) }
    var colorPickerTarget by remember { mutableStateOf<String?>(null) }
    val customBehaviors by viewModel.customBehaviors.observeAsState(initial = emptyList())
    val systemBehaviors by viewModel.systemBehaviors.observeAsState(initial = emptyList())

    var condition by remember(rule) {
        mutableStateOf(
            if (rule?.conditionJson != null) {
                try {
                    json.decodeFromString<com.example.myapplication.util.Condition>(rule.conditionJson)
                } catch (e: Exception) {
                    com.example.myapplication.util.Condition(type = ruleType)
                }
            } else {
                com.example.myapplication.util.Condition(type = ruleType)
            }
        )
    }

    var format by remember(rule) {
        mutableStateOf(
            if (rule?.formatJson != null) {
                try {
                    json.decodeFromString<com.example.myapplication.util.Format>(rule.formatJson)
                } catch (e: Exception) {
                    com.example.myapplication.util.Format()
                }
            } else {
                com.example.myapplication.util.Format()
            }
        )
    }

    // Helper state for Active Time (Single range support for UI)
    var activeTimeStart by remember(condition) { mutableStateOf(condition.activeTimes?.firstOrNull()?.startTime ?: "") }
    var activeTimeEnd by remember(condition) { mutableStateOf(condition.activeTimes?.firstOrNull()?.endTime ?: "") }
    var activeTimeDays by remember(condition) {
        mutableStateOf(
            condition.activeTimes?.firstOrNull()?.daysOfWeek?.map { dayInt ->
                when (dayInt) {
                    0 -> "Monday"
                    1 -> "Tuesday"
                    2 -> "Wednesday"
                    3 -> "Thursday"
                    4 -> "Friday"
                    5 -> "Saturday"
                    6 -> "Sunday"
                    else -> ""
                }
            }?.filter { it.isNotEmpty() } ?: emptyList()
        )
    }

    if (showColorPicker.value) {
        val initialColorStr = if (colorPickerTarget == "color") format.color else format.outline
        val initialColor = if (initialColorStr.isNullOrBlank()) Color.White else safeParseColor(initialColorStr)
        ColorPickerDialog(
            onColorSelected = { color ->
                if (colorPickerTarget == "color") {
                    format = format.copy(color = color)
                } else if (colorPickerTarget == "outline") {
                    format = format.copy(outline = color)
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
                                value = condition.groupId?.toString() ?: "",
                                onValueChange = {
                                    condition = condition.copy(groupId = it.toLongOrNull())
                                },
                                label = { Text("Group ID") }
                            )
                        }
                        "behavior_count" -> {
                            val allBehaviors =
                                customBehaviors.map { it.name } + systemBehaviors.map { it.name }
                            MultiSelectDropdown(
                                options = allBehaviors,
                                selectedOptions = condition.behaviorNames?.split(",") ?: emptyList(),
                                onSelectionChanged = {
                                    condition = condition.copy(behaviorNames = it.joinToString(","))
                                },
                                label = "Behaviors"
                            )
                            OutlinedTextField(
                                value = condition.countThreshold?.toString() ?: "1",
                                onValueChange = {
                                    condition = condition.copy(countThreshold = it.toIntOrNull())
                                },
                                label = { Text("Count Threshold") }
                            )
                            OutlinedTextField(
                                value = condition.timeWindowHours?.toString() ?: "24",
                                onValueChange = {
                                    condition = condition.copy(timeWindowHours = it.toIntOrNull())
                                },
                                label = { Text("Time Window (Hours)") }
                            )
                        }
                        "quiz_score_threshold" -> {
                            OutlinedTextField(
                                value = condition.quizNameContains ?: "",
                                onValueChange = {
                                    condition = condition.copy(quizNameContains = it)
                                },
                                label = { Text("Quiz Name Contains") }
                            )
                            OutlinedTextField(
                                value = condition.operator ?: "<=",
                                onValueChange = {
                                    condition = condition.copy(operator = it)
                                },
                                label = { Text("Operator") }
                            )
                            OutlinedTextField(
                                value = condition.scoreThresholdPercent?.toString() ?: "50.0",
                                onValueChange = {
                                    condition = condition.copy(scoreThresholdPercent = it.toDoubleOrNull())
                                },
                                label = { Text("Score Threshold (%)") }
                            )
                        }
                        "quiz_mark_count" -> {
                            OutlinedTextField(
                                value = condition.markTypeId ?: "",
                                onValueChange = {
                                    condition = condition.copy(markTypeId = it)
                                },
                                label = { Text("Mark Type ID") }
                            )
                        }
                        "live_quiz_response" -> {
                            OutlinedTextField(
                                value = condition.quizResponse ?: "Correct",
                                onValueChange = {
                                    condition = condition.copy(quizResponse = it)
                                },
                                label = { Text("Quiz Response") }
                            )
                        }
                        "live_homework_yes_no" -> {
                            OutlinedTextField(
                                value = condition.homeworkTypeId ?: "",
                                onValueChange = {
                                    condition = condition.copy(homeworkTypeId = it)
                                },
                                label = { Text("Homework Type ID") }
                            )
                            OutlinedTextField(
                                value = condition.homeworkResponse ?: "yes",
                                onValueChange = {
                                    condition = condition.copy(homeworkResponse = it)
                                },
                                label = { Text("Homework Response") }
                            )
                        }
                        "live_homework_select" -> {
                            OutlinedTextField(
                                value = condition.homeworkOptionName ?: "",
                                onValueChange = {
                                    condition = condition.copy(homeworkOptionName = it)
                                },
                                label = { Text("Homework Option Name") }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Active Modes
                    Text("Active Modes", style = MaterialTheme.typography.titleSmall)
                    val allModes = listOf("behavior", "quiz", "homework")
                    MultiSelectDropdown(
                        options = allModes,
                        selectedOptions = condition.activeModes ?: emptyList(),
                        onSelectionChanged = {
                            condition = condition.copy(activeModes = it)
                        },
                        label = "Active Modes"
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Active Times
                    Text("Active Times", style = MaterialTheme.typography.titleSmall)
                    Row {
                        OutlinedTextField(
                            value = activeTimeStart,
                            onValueChange = { activeTimeStart = it },
                            label = { Text("Start (HH:MM)") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = activeTimeEnd,
                            onValueChange = { activeTimeEnd = it },
                            label = { Text("End (HH:MM)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                    MultiSelectDropdown(
                        options = daysOfWeek,
                        selectedOptions = activeTimeDays,
                        onSelectionChanged = { activeTimeDays = it },
                        label = "Days of Week"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Formatting", style = MaterialTheme.typography.titleMedium)
                    ColorPickerField(
                        label = "Fill Color",
                        color = format.color ?: "",
                        onColorChange = { format = format.copy(color = it) },
                        onColorPickerClick = {
                            colorPickerTarget = "color"
                            showColorPicker.value = true
                        }
                    )
                    ColorPickerField(
                        label = "Outline Color",
                        color = format.outline ?: "",
                        onColorChange = { format = format.copy(outline = it) },
                        onColorPickerClick = {
                            colorPickerTarget = "outline"
                            showColorPicker.value = true
                        }
                    )
                    // Application style is not in Format data class?
                    // Let's check Format data class again.
                    // It has color and outline. No application_style.
                    // But the previous code had it.
                    // Maybe I missed it in the Format class definition?
                    // Step 177 showed: data class Format(val color: String? = null, val outline: String? = null)
                    // So application_style is NOT supported by the engine currently?
                    // Or maybe it's supported in Python but not fully in Android engine?
                    // I'll omit it for now to match the data class.

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
                    val newActiveTimes = if (activeTimeStart.isNotBlank() && activeTimeEnd.isNotBlank()) {
                        val days = activeTimeDays.map { dayName ->
                            when (dayName) {
                                "Monday" -> 0
                                "Tuesday" -> 1
                                "Wednesday" -> 2
                                "Thursday" -> 3
                                "Friday" -> 4
                                "Saturday" -> 5
                                "Sunday" -> 6
                                else -> -1
                            }
                        }.filter { it != -1 }
                        listOf(com.example.myapplication.util.ActiveTime(activeTimeStart, activeTimeEnd, days))
                    } else {
                        null
                    }

                    val finalCondition = condition.copy(
                        type = ruleType,
                        activeTimes = newActiveTimes
                    )
                    
                    val newRule = (rule ?: ConditionalFormattingRule()).copy(
                        name = name,
                        priority = priority.toIntOrNull() ?: 0,
                        type = ruleType,
                        targetType = targetType,
                        conditionJson = json.encodeToString(finalCondition),
                        formatJson = json.encodeToString(format)
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
