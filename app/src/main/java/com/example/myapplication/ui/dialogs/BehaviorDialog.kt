package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * A modal dialog for recording behavioral incidents for one or more students.
 *
 * This component is a primary data entry point for the seating chart. It allows teachers
 * to quickly apply behavior tags (e.g., "Talking", "Great Participation") to a selection
 * of students.
 *
 * ### Architectural Features:
 * 1. **Bulk Logging**: Supports logging the same event for multiple students simultaneously.
 * 2. **Task Integration**: If a student has an active `temporaryTask`, logging a behavior
 *    will automatically mark that task as complete via [SeatingChartViewModel.completeTaskForStudent].
 * 3. **Command-Backed Persistence**: Each logged event is encapsulated in a `LogBehaviorCommand`
 *    by the [viewModel], allowing the action to be undone from the main seating chart.
 *
 * @param studentIds The list of student database IDs for whom the behavior is being logged.
 * @param viewModel The primary ViewModel for triggering database updates and task completion.
 * @param behaviorTypes The list of available behavior categories (e.g., from [SeatingChartViewModel.allCustomBehaviors]).
 * @param onDismiss Callback to close the dialog.
 * @param onBehaviorLogged Callback triggered after successful logging, providing the count of events created.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BehaviorDialog(
    studentIds: List<Long>,
    viewModel: SeatingChartViewModel,
    behaviorTypes: List<String>,
    onDismiss: () -> Unit,
    onBehaviorLogged: (Int) -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var student by remember { mutableStateOf<Student?>(null) }

    LaunchedEffect(studentIds) {
        if (studentIds.size == 1) {
            student = viewModel.getStudentForEditing(studentIds.first())
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            val titleText = if (student != null) {
                "Log Behavior for ${student!!.firstName} ${student!!.lastName}"
            } else {
                "Log Behavior for ${studentIds.size} ${if (studentIds.size == 1) "student" else "students"}"
            }
            Text(titleText, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    minLines = 2
                )
                Text("Select Behavior:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    behaviorTypes.forEach { behaviorType ->
                        OutlinedButton(
                            onClick = {
                                if (studentIds.size == 1) {
                                    student?.let {
                                        if (it.temporaryTask?.isNotBlank() == true) {
                                            viewModel.completeTaskForStudent(it.id)
                                        }
                                    }
                                }
                                studentIds.forEach { studentId ->
                                    val behaviorEvent = BehaviorEvent(
                                        studentId = studentId,
                                        comment = notes,
                                        type = behaviorType,
                                        timestamp = System.currentTimeMillis()
                                    )
                                    viewModel.addBehaviorEvent(behaviorEvent)
                                }
                                onBehaviorLogged(studentIds.size)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(behaviorType)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}