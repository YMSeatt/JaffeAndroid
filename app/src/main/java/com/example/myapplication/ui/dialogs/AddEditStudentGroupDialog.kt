package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.StudentGroup
import com.example.myapplication.viewmodel.StudentGroupsViewModel

@Composable
fun AddEditStudentGroupDialog(
    group: StudentGroup?,
    viewModel: StudentGroupsViewModel,
    onDismiss: () -> Unit
) {
    var name by remember(group) { mutableStateOf(group?.name ?: "") }
    var color by remember(group) { mutableStateOf(group?.color ?: "#FFFFFF") }
    val isEditMode = group != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditMode) "Edit Student Group" else "Add Student Group") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group Name") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Group Color")
                ColorPicker(
                    selectedColor = color,
                    onColorSelected = { color = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newGroup = StudentGroup(
                        id = group?.id ?: 0,
                        name = name,
                        color = color
                    )
                    if (isEditMode) {
                        viewModel.updateStudentGroup(newGroup)
                    } else {
                        viewModel.addStudentGroup(newGroup)
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
                        viewModel.deleteStudentGroup(group!!)
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

@Composable
fun ColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    val colors = listOf(
        "#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF", "#00FFFF",
        "#FFFFFF", "#000000", "#808080", "#C0C0C0", "#FFA500", "#800080"
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        colors.forEach { colorString ->
            val color = Color(android.graphics.Color.parseColor(colorString))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = 2.dp,
                        color = if (selectedColor == colorString) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(colorString) }
            )
        }
    }
}
