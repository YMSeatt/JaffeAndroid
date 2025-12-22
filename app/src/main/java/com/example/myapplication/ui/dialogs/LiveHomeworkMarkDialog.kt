package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
    onSave: (HomeworkLog) -> Unit
) {
    var student by remember { mutableStateOf<Student?>(null) }
    val liveHomeworkSessionMode by settingsViewModel.liveHomeworkSessionMode.collectAsState()
    val liveHomeworkSelectOptions by settingsViewModel.liveHomeworkSelectOptions.collectAsState()
    val allTemplates by viewModel.allHomeworkTemplates.observeAsState(emptyList())
    
    // State for template mode
    var selectedTemplate by remember { mutableStateOf<HomeworkTemplate?>(null) }
    var templateExpanded by remember { mutableStateOf(false) }
    val markValues = remember { mutableStateMapOf<String, Any>() } // stepId -> value

    // Initialize mark values when template changes
    LaunchedEffect(selectedTemplate) {
        markValues.clear()
        selectedTemplate?.getSteps()?.forEach { step ->
             // Use label as key for readability
             val key = step.label
            when (step.type) {
                HomeworkMarkType.CHECKBOX -> markValues[key] = false
                HomeworkMarkType.SCORE -> markValues[key] = 0 // Default score
                HomeworkMarkType.COMMENT -> markValues[key] = ""
            }
        }
    }

    LaunchedEffect(studentId) {
        student = viewModel.getStudentForEditing(studentId)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(student?.let { "Mark Homework for ${it.firstName} ${it.lastName}" } ?: "Mark Homework") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(student?.let { "Mark ${it.firstName}'s homework:" } ?: "Mark this student's homework:")
                Spacer(modifier = Modifier.height(16.dp))

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
                    // Template Based Marking
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
                                        steps = if (step.maxValue > 0) step.maxValue - 1 else 0
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

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val log = HomeworkLog(
                                studentId = studentId,
                                assignmentName = selectedTemplate!!.name, // Use template name as assignment name
                                status = "Completed", // Or derive from marks
                                loggedAt = System.currentTimeMillis(),
                                marksData = Gson().toJson(markValues),
                                isComplete = true
                            )
                            onSave(log)
                            onDismissRequest()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Marks")
                    }

                } else {
                    // Legacy / Quick Mark Mode
                    if (liveHomeworkSessionMode == "Select") {
                        val options = liveHomeworkSelectOptions.split(",").filter { it.isNotBlank() }
                        options.forEach { option ->
                            Button(
                                onClick = {
                                    val log = HomeworkLog(
                                        studentId = studentId,
                                        assignmentName = "Live Homework",
                                        status = option.trim(),
                                        loggedAt = System.currentTimeMillis(),
                                        marksData = "{}"
                                    )
                                    onSave(log)
                                    onDismissRequest()
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text(option.trim())
                            }
                        }
                    } else {
                        Button(onClick = {
                            val log = HomeworkLog(
                                studentId = studentId,
                                assignmentName = "Live Homework",
                                status = "Done",
                                loggedAt = System.currentTimeMillis(),
                                marksData = "{}"
                            )
                            onSave(log)
                            onDismissRequest()
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Done")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            val log = HomeworkLog(
                                studentId = studentId,
                                assignmentName = "Live Homework",
                                status = "Not Done",
                                loggedAt = System.currentTimeMillis(),
                                marksData = "{}"
                            )
                            onSave(log)
                            onDismissRequest()
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Not Done")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
