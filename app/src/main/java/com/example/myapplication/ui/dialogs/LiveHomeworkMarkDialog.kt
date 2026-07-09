package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.HomeworkMarkType
import com.example.myapplication.data.HomeworkTemplate
import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.google.gson.Gson
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveHomeworkMarkDialog(
    studentId: Long,
    viewModel: SeatingChartViewModel,
    settingsViewModel: com.example.myapplication.viewmodel.SettingsViewModel,
    onDismissRequest: () -> Unit,
    onSave: (List<HomeworkLog>) -> Unit
) {
    var student by remember { mutableStateOf<Student?>(null) }
    val liveHomeworkSessionMode by settingsViewModel.liveHomeworkSessionMode.collectAsState()
    val liveHomeworkSelectOptions by settingsViewModel.liveHomeworkSelectOptions.collectAsState()
    val allTemplates by viewModel.allHomeworkTemplates.observeAsState(emptyList())
    val customHomeworkTypes by settingsViewModel.customHomeworkTypes.observeAsState(initial = emptyList())
    
    // State for session tracking
    val yesNoStates = remember { mutableStateMapOf<String, String>() } // TypeName -> "yes"/"no"/"pending"
    val selectStates = remember { mutableStateMapOf<String, Boolean>() } // OptionName -> selected

    // Initialize states from current session data if available
    LaunchedEffect(studentId, viewModel.liveHomeworkScores.value) {
        student = viewModel.getStudentForEditing(studentId)
        val currentScores = viewModel.liveHomeworkScores.value?.get(studentId) ?: emptyMap()

        if (liveHomeworkSessionMode == "Select") {
            val selected = currentScores["selected_options"] as? List<*> ?: emptyList()
            liveHomeworkSelectOptions.split(",").forEach { option ->
                selectStates[option.trim()] = selected.contains(option.trim())
            }
        } else {
            customHomeworkTypes.forEach { type ->
                yesNoStates[type.name] = currentScores[type.name]?.toString() ?: "pending"
            }
        }
    }

    // Template Mode state
    var selectedTemplate by remember { mutableStateOf<HomeworkTemplate?>(null) }
    var templateExpanded by remember { mutableStateOf(false) }
    val markValues = remember { mutableStateMapOf<String, Any>() }

    LaunchedEffect(selectedTemplate) {
        markValues.clear()
        selectedTemplate?.getSteps()?.forEach { step ->
             val key = step.label
            when (step.type) {
                HomeworkMarkType.CHECKBOX -> markValues[key] = false
                HomeworkMarkType.SCORE -> markValues[key] = 0
                HomeworkMarkType.COMMENT -> markValues[key] = ""
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(student?.let { "Mark Homework for ${it.firstName} ${it.lastName}" } ?: "Mark Homework") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Template Selector
                if (allTemplates.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = templateExpanded,
                        onExpandedChange = { templateExpanded = !templateExpanded },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            readOnly = true,
                            value = selectedTemplate?.name ?: "No Template (Quick Mark)",
                            onValueChange = {},
                            label = { Text("Use Template (Optional)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = templateExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        )
                        ExposedDropdownMenu(
                            expanded = templateExpanded,
                            onDismissRequest = { templateExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("No Template (Quick Mark)") },
                                onClick = {
                                    selectedTemplate = null
                                    templateExpanded = false
                                }
                            )
                            allTemplates.forEach { template ->
                                DropdownMenuItem(
                                    text = { Text(template.name) },
                                    onClick = {
                                        selectedTemplate = template
                                        templateExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (selectedTemplate != null) {
                    val steps = selectedTemplate!!.getSteps()
                    steps.forEach { step ->
                        Text(step.label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top=8.dp))
                        val key = step.label
                        when (step.type) {
                            HomeworkMarkType.CHECKBOX -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = (markValues[key] as? Boolean) == true,
                                        onCheckedChange = { markValues[key] = it }
                                    )
                                    Text(if ((markValues[key] as? Boolean) == true) "Yes" else "No")
                                }
                            }
                            HomeworkMarkType.SCORE -> {
                                var score by remember(markValues[key]) { mutableStateOf(((markValues[key] as? Number)?.toFloat() ?: 0f)) }
                                Column {
                                    Slider(
                                        value = score,
                                        onValueChange = { 
                                            score = it
                                            markValues[key] = it.roundToInt() 
                                        },
                                        valueRange = 0f..step.maxValue.toFloat(),
                                        steps = if (step.maxValue > 1) step.maxValue - 1 else 0
                                    )
                                    Text("Score: ${score.roundToInt()} / ${step.maxValue}")
                                }
                            }
                            HomeworkMarkType.COMMENT -> {
                                OutlinedTextField(
                                    value = (markValues[key] as? String) ?: "",
                                    onValueChange = { markValues[key] = it },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                } else {
                    // Quick Mark Mode
                    if (liveHomeworkSessionMode == "Select") {
                        val options = liveHomeworkSelectOptions.split(",").filter { it.isNotBlank() }
                        options.forEach { option ->
                            val trimmed = option.trim()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(trimmed)
                                Checkbox(
                                    checked = selectStates[trimmed] ?: false,
                                    onCheckedChange = { selectStates[trimmed] = it }
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    } else {
                        // Yes/No Mode
                        if (customHomeworkTypes.isEmpty()) {
                            Text("No homework types configured for 'Yes/No' mode.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                        }
                        customHomeworkTypes.forEach { type ->
                            val state = yesNoStates[type.name] ?: "pending"
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(type.name, modifier = Modifier.weight(1f))
                                Row {
                                    IconButton(
                                        onClick = { yesNoStates[type.name] = if (state == "yes") "pending" else "yes" },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = if (state == "yes") Color(0xFF4CAF50) else Color.Transparent,
                                            contentColor = if (state == "yes") Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Yes")
                                    }
                                    IconButton(
                                        onClick = { yesNoStates[type.name] = if (state == "no") "pending" else "no" },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = if (state == "no") Color(0xFFF44336) else Color.Transparent,
                                            contentColor = if (state == "no") Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "No")
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val logs = mutableListOf<HomeworkLog>()
                    if (selectedTemplate != null) {
                        logs.add(HomeworkLog(
                            studentId = studentId,
                            assignmentName = selectedTemplate!!.name,
                            status = "Completed",
                            loggedAt = System.currentTimeMillis(),
                            marksData = Gson().toJson(markValues),
                            isComplete = true
                        ))
                    } else {
                        if (liveHomeworkSessionMode == "Select") {
                            val selected = selectStates.filter { it.value }.keys.toList()
                            logs.add(HomeworkLog(
                                studentId = studentId,
                                assignmentName = "selected_options", // Key for ConditionalFormattingEngine
                                status = "Session Update",
                                loggedAt = System.currentTimeMillis(),
                                marksData = Gson().toJson(mapOf("selected_options" to selected))
                            ))
                        } else {
                            // Yes/No mode: return multiple logs or one aggregated?
                            // To match ConditionalFormattingEngine expectations, we aggregate.
                            val aggregated = yesNoStates.filter { it.value != "pending" }
                            if (aggregated.isNotEmpty()) {
                                logs.add(HomeworkLog(
                                    studentId = studentId,
                                    assignmentName = "Yes/No Update",
                                    status = "Session Update",
                                    loggedAt = System.currentTimeMillis(),
                                    marksData = Gson().toJson(aggregated)
                                ))
                            }
                        }
                    }
                    onSave(logs)
                    onDismissRequest()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
