package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.ConditionalFormattingRule
import com.example.myapplication.ui.components.MultiSelectDropdown
import com.example.myapplication.util.ActiveTime
import com.example.myapplication.util.Condition
import com.example.myapplication.viewmodel.ConditionalFormattingRuleViewModel
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkEditConditionalRulesDialog(
    selectedRules: List<ConditionalFormattingRule>,
    viewModel: ConditionalFormattingRuleViewModel,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Enabled", "Active Times", "Active Modes")

    // Enabled state
    var enabledAction by remember { mutableStateOf("no_change") }

    // Active Times state
    var timesAction by remember { mutableStateOf("no_change") }
    var activeTimeStart by remember { mutableStateOf("") }
    var activeTimeEnd by remember { mutableStateOf("") }
    var activeTimeDays by remember { mutableStateOf(emptyList<String>()) }
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    // Active Modes state
    var modesAction by remember { mutableStateOf("no_change") }
    val availableModes = listOf("behavior", "quiz", "homework")
    var selectedModesForBulk by remember { mutableStateOf(emptyList<String>()) }

    val json = remember { Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bulk Edit ${selectedRules.size} Rules") },
        text = {
            Column {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> { // Enabled
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = enabledAction == "no_change", onClick = { enabledAction = "no_change" })
                                Text("No change")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = enabledAction == "set_enabled", onClick = { enabledAction = "set_enabled" })
                                Text("Set ALL to Enabled")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = enabledAction == "set_disabled", onClick = { enabledAction = "set_disabled" })
                                Text("Set ALL to Disabled")
                            }
                        }
                    }
                    1 -> { // Active Times
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = timesAction == "no_change", onClick = { timesAction = "no_change" })
                                Text("No change")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = timesAction == "replace", onClick = { timesAction = "replace" })
                                Text("REPLACE with new range")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = timesAction == "add", onClick = { timesAction = "add" })
                                Text("ADD new range")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = timesAction == "clear", onClick = { timesAction = "clear" })
                                Text("CLEAR all active times")
                            }

                            if (timesAction == "replace" || timesAction == "add") {
                                Spacer(modifier = Modifier.height(8.dp))
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
                                MultiSelectDropdown(
                                    options = daysOfWeek,
                                    selectedOptions = activeTimeDays,
                                    onSelectionChanged = { activeTimeDays = it },
                                    label = "Days of Week"
                                )
                            }
                        }
                    }
                    2 -> { // Active Modes
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = modesAction == "no_change", onClick = { modesAction = "no_change" })
                                Text("No change")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = modesAction == "replace", onClick = { modesAction = "replace" })
                                Text("REPLACE active modes")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = modesAction == "add_selected", onClick = { modesAction = "add_selected" })
                                Text("ADD selected modes")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = modesAction == "remove_selected", onClick = { modesAction = "remove_selected" })
                                Text("REMOVE selected modes")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = modesAction == "clear", onClick = { modesAction = "clear" })
                                Text("CLEAR all active modes")
                            }

                            if (modesAction == "replace" || modesAction == "add_selected" || modesAction == "remove_selected") {
                                MultiSelectDropdown(
                                    options = availableModes,
                                    selectedOptions = selectedModesForBulk,
                                    onSelectionChanged = { selectedModesForBulk = it },
                                    label = "Select Modes"
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedRules = selectedRules.map { rule ->
                        var updatedRule = rule

                        // Apply Enabled change
                        if (enabledAction == "set_enabled") {
                            updatedRule = updatedRule.copy(enabled = true)
                        } else if (enabledAction == "set_disabled") {
                            updatedRule = updatedRule.copy(enabled = false)
                        }

                        // Parse current condition
                        val condition = try {
                            json.decodeFromString<Condition>(updatedRule.conditionJson)
                        } catch (e: Exception) {
                            Condition(type = updatedRule.type)
                        }

                        var updatedCondition = condition

                        // Apply Times change
                        if (timesAction != "no_change") {
                            val newActiveTime = if (activeTimeStart.isNotBlank() && activeTimeEnd.isNotBlank()) {
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
                                ActiveTime(activeTimeStart, activeTimeEnd, days)
                            } else null

                            val currentTimes = condition.activeTimes ?: emptyList()
                            val newTimes = when (timesAction) {
                                "replace" -> if (newActiveTime != null) listOf(newActiveTime) else emptyList()
                                "add" -> if (newActiveTime != null) currentTimes + newActiveTime else currentTimes
                                "clear" -> emptyList()
                                else -> currentTimes
                            }
                            updatedCondition = updatedCondition.copy(activeTimes = newTimes)
                        }

                        // Apply Modes change
                        if (modesAction != "no_change") {
                            val currentModes = condition.activeModes ?: emptyList()
                            val newModes = when (modesAction) {
                                "replace" -> selectedModesForBulk
                                "add_selected" -> (currentModes + selectedModesForBulk).distinct()
                                "remove_selected" -> currentModes.filterNot { it in selectedModesForBulk }
                                "clear" -> emptyList()
                                else -> currentModes
                            }
                            updatedCondition = updatedCondition.copy(activeModes = newModes)
                        }

                        updatedRule.copy(conditionJson = json.encodeToString(updatedCondition))
                    }

                    viewModel.bulkUpdateRules(updatedRules)
                    onDismiss()
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
