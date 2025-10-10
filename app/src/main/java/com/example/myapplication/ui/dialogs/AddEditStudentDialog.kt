package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import com.example.myapplication.viewmodel.StudentGroupsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditStudentDialog(
    studentToEdit: Student?,
    viewModel: SeatingChartViewModel,
    studentGroupsViewModel: StudentGroupsViewModel, // Unused in this snippet but kept for consistency.
    settingsViewModel: SettingsViewModel, // Unused in this snippet but kept for consistency.
    onDismiss: () -> Unit
) {
    var firstName by remember { mutableStateOf(studentToEdit?.firstName ?: "") }
    var lastName by remember { mutableStateOf(studentToEdit?.lastName ?: "") }
    var nickname by remember { mutableStateOf(studentToEdit?.nickname ?: "") }
    var showError by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Student") },
            text = { Text("Are you sure you want to delete this student? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        studentToEdit?.let {
                            viewModel.deleteStudent(it)
                        }
                        showDeleteConfirmation = false
                        onDismiss()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (studentToEdit == null) "Add Student" else "Edit Student") },
        text = {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (showError) {
                    Text("A student with this name already exists.", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (firstName.isNotBlank() && lastName.isNotBlank()) {
                            val studentExists = viewModel.studentExists(firstName, lastName)
                            if (studentExists && (studentToEdit == null || (studentToEdit.firstName != firstName || studentToEdit.lastName != lastName))) {
                                showError = true
                            } else {
                                if (studentToEdit != null) {
                                    val updatedStudent = studentToEdit.copy(
                                        firstName = firstName,
                                        lastName = lastName,
                                        nickname = nickname
                                    )
                                    viewModel.updateStudent(studentToEdit, updatedStudent)
                                } else {
                                    val newStudent = Student(
                                        firstName = firstName,
                                        lastName = lastName,
                                        nickname = nickname
                                    )
                                    viewModel.addStudent(newStudent)
                                }
                                onDismiss()
                            }
                        }
                    }
                },
                enabled = !showError
            ) {
                Text(if (studentToEdit == null) "Add" else "Save")
            }
        },
        dismissButton = {
            Row {
                if (studentToEdit != null) {
                    Button(
                        onClick = { showDeleteConfirmation = true }
                    ) {
                        Text("Delete")
                    }
                }
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}