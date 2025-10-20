package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Furniture
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFurnitureDialog(
    furnitureToEdit: Furniture?,
    viewModel: SeatingChartViewModel,
    settingsViewModel: SettingsViewModel, // Although not used here, keep it for consistency if needed elsewhere
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(furnitureToEdit?.name ?: "") }
    var width by remember { mutableStateOf(furnitureToEdit?.width.toString()) }
    var height by remember { mutableStateOf(furnitureToEdit?.height.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (furnitureToEdit == null) "Add Furniture" else "Edit Furniture") },
        text = {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Furniture Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = width,
                    onValueChange = { width = it },
                    label = { Text("Width") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("Height") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedWidth: Float? = width.toFloatOrNull()
                    val parsedHeight = height.toFloatOrNull()

                    if (name.isNotBlank() && parsedWidth != null && parsedHeight != null) {
                        if (furnitureToEdit != null) {
                            val updatedFurniture = furnitureToEdit.copy(
                                name = name,
                                width = parsedWidth.toInt(),
                                height = parsedHeight.toInt()
                            )
                            viewModel.updateFurniture(furnitureToEdit, updatedFurniture)
                        } else {
                            val newFurniture = Furniture(
                                name = name,
                                type = "desk", // Default or user-selectable type
                                width = parsedWidth.toInt(),
                                height = parsedHeight.toInt()
                            )
                            viewModel.addFurniture(newFurniture)
                        }
                        onDismiss()
                    } else {
                        // Optionally show an error message
                    }
                }
            ) {
                Text(if (furnitureToEdit == null) "Add" else "Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}