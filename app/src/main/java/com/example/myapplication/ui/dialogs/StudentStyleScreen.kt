package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.StudentStyleViewModel
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@Composable
fun StudentStyleScreen(
    studentId: Long,
    viewModel: StudentStyleViewModel = viewModel(),
    seatingChartViewModel: SeatingChartViewModel,
    onDismiss: () -> Unit
) {
    val student by viewModel.student.collectAsState()

    LaunchedEffect(studentId) {
        viewModel.loadStudent(studentId)
    }

    if (student != null) {
        var customBackgroundColor by remember { mutableStateOf(student!!.customBackgroundColor ?: "") }
        var customOutlineColor by remember { mutableStateOf(student!!.customOutlineColor ?: "") }
        var customTextColor by remember { mutableStateOf(student!!.customTextColor ?: "") }
        var customWidth by remember { mutableStateOf(student!!.customWidth?.toString() ?: "") }
        var customHeight by remember { mutableStateOf(student!!.customHeight?.toString() ?: "") }
        var customOutlineThickness by remember { mutableStateOf(student!!.customOutlineThickness?.toString() ?: "") }
        val showColorPicker = remember { mutableStateOf(false) }
        val colorPickerVar = remember { mutableStateOf<MutableState<String>?>(null) }

        if (showColorPicker.value) {
            ColorPickerDialog(
                onColorSelected = { color ->
                    colorPickerVar.value?.value = color
                    showColorPicker.value = false
                },
                onDismiss = { showColorPicker.value = false }
            )
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Customize Style") },
            text = {
                Column(modifier = Modifier.padding(16.dp)) {
                    TextField(
                        value = customBackgroundColor,
                        onValueChange = { customBackgroundColor = it },
                        label = { Text("Background Color") },
                        trailingIcon = {
                            Button(onClick = {
                                colorPickerVar.value = mutableStateOf(customBackgroundColor)
                                showColorPicker.value = true
                            }) {
                                Text("...")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = customOutlineColor,
                        onValueChange = { customOutlineColor = it },
                        label = { Text("Outline Color") },
                        trailingIcon = {
                            Button(onClick = {
                                colorPickerVar.value = mutableStateOf(customOutlineColor)
                                showColorPicker.value = true
                            }) {
                                Text("...")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = customTextColor,
                        onValueChange = { customTextColor = it },
                        label = { Text("Text Color") },
                        trailingIcon = {
                            Button(onClick = {
                                colorPickerVar.value = mutableStateOf(customTextColor)
                                showColorPicker.value = true
                            }) {
                                Text("...")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = customWidth,
                        onValueChange = { customWidth = it },
                        label = { Text("Width (dp)") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = customHeight,
                        onValueChange = { customHeight = it },
                        label = { Text("Height (dp)") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = customOutlineThickness,
                        onValueChange = { customOutlineThickness = it },
                        label = { Text("Outline Thickness (dp)") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updatedStudent = student!!.copy(
                            customBackgroundColor = customBackgroundColor.ifBlank { null },
                            customOutlineColor = customOutlineColor.ifBlank { null },
                            customTextColor = customTextColor.ifBlank { null },
                            customWidth = customWidth.toIntOrNull(),
                            customHeight = customHeight.toIntOrNull(),
                            customOutlineThickness = customOutlineThickness.toIntOrNull()
                        )
                        viewModel.updateStudent(seatingChartViewModel, updatedStudent)
                        onDismiss()
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ColorPickerDialog(
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val controller = rememberColorPickerController()
    var hexCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Color") },
        text = {
            HsvColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                controller = controller,
                onColorChanged = { colorEnvelope: ColorEnvelope ->
                    hexCode = colorEnvelope.hexCode
                }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onColorSelected("#$hexCode")
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}