package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
    studentGroupsViewModel: StudentGroupsViewModel,
    settingsViewModel: SettingsViewModel,
    onDismiss: () -> Unit,
    onEditStyle: () -> Unit = {}
) {
    var firstName by remember { mutableStateOf(studentToEdit?.firstName ?: "") }
    var lastName by remember { mutableStateOf(studentToEdit?.lastName ?: "") }
    var nickname by remember { mutableStateOf(studentToEdit?.nickname ?: "") }
    var gender by remember { mutableStateOf(studentToEdit?.gender ?: "Boy") }
    var groupId by remember { mutableStateOf(studentToEdit?.groupId) }

    val studentGroups by studentGroupsViewModel.allStudentGroups.collectAsState()
    var groupDropdownExpanded by remember { mutableStateOf(false) }

    var showError by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(firstName, lastName) {
        showError = false
    }

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
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
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

                Spacer(modifier = Modifier.height(16.dp))
                Text("Gender", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = gender == "Boy", onClick = { gender = "Boy" })
                    Text("Boy")
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = gender == "Girl", onClick = { gender = "Girl" })
                    Text("Girl")
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = gender == "Other", onClick = { gender = "Other" })
                    Text("Other")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Group", style = MaterialTheme.typography.labelLarge)
                ExposedDropdownMenuBox(
                    expanded = groupDropdownExpanded,
                    onExpandedChange = { groupDropdownExpanded = !groupDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val selectedGroupName = studentGroups.find { it.id == groupId }?.name ?: "No Group"
                    OutlinedTextField(
                        value = selectedGroupName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assign to Group") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = groupDropdownExpanded,
                        onDismissRequest = { groupDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("No Group") },
                            onClick = {
                                groupId = null
                                groupDropdownExpanded = false
                            }
                        )
                        studentGroups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name) },
                                onClick = {
                                    groupId = group.id
                                    groupDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                if (showError) {
                    Text("A student with this name already exists.", color = MaterialTheme.colorScheme.error)
                }
                if (studentToEdit != null) {
                    Button(
                        onClick = onEditStyle,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Edit Student Style")
                    }
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
                                        nickname = nickname,
                                        gender = gender,
                                        groupId = groupId
                                    )
                                    viewModel.updateStudent(studentToEdit, updatedStudent)
                                } else {
                                    val newStudent = Student(
                                        firstName = firstName,
                                        lastName = lastName,
                                        nickname = nickname,
                                        gender = gender,
                                        groupId = groupId
                                    )
                                    viewModel.addStudent(newStudent)
                                }
                                onDismiss()
                            }
                        }
                    }
                }
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