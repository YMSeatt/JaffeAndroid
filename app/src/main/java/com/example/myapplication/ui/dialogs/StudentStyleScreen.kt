package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.preferences.DEFAULT_STUDENT_FONT_SIZE_SP
import com.example.myapplication.utils.getAvailableFontFamilies
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.StudentStyleViewModel
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentStyleScreen(
    studentId: Long,
    viewModel: StudentStyleViewModel = hiltViewModel(),
    seatingChartViewModel: SeatingChartViewModel,
    onDismiss: () -> Unit
) {
    val student by viewModel.student.collectAsState()

    // Declare state variables
    val customBackgroundColor = remember { mutableStateOf("") }
    val customOutlineColor = remember { mutableStateOf("") }
    val customTextColor = remember { mutableStateOf("") }
    var customWidth by remember { mutableStateOf("") }
    var customHeight by remember { mutableStateOf("") }
    var customCornerRadius by remember { mutableStateOf("") }
    var customOutlineThickness by remember { mutableStateOf("") }
    var customPadding by remember { mutableStateOf("") }
    var customFontFamily by remember { mutableStateOf("") }
    var customFontSize by remember { mutableStateOf("") }
    val customFontColor = remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val showColorPicker = remember { mutableStateOf(false) }
    val colorPickerVar = remember { mutableStateOf<MutableState<String>?>(null) }

    LaunchedEffect(studentId) {
        viewModel.loadStudent(studentId)
    }

    LaunchedEffect(student) {
        student?.let {
            customBackgroundColor.value = it.customBackgroundColor ?: ""
            customOutlineColor.value = it.customOutlineColor ?: ""
            customTextColor.value = it.customTextColor ?: ""
            customWidth = it.customWidth?.toString() ?: ""
            customHeight = it.customHeight?.toString() ?: ""
            customCornerRadius = it.customCornerRadius?.toString() ?: ""
            customOutlineThickness = it.customOutlineThickness?.toString() ?: ""
            customPadding = it.customPadding?.toString() ?: ""
            customFontFamily = it.customFontFamily ?: ""
            customFontSize = it.customFontSize?.toString() ?: ""
            customFontColor.value = it.customFontColor ?: ""
        }
    }

    if (student != null) {

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
                        value = customBackgroundColor.value,
                        onValueChange = { customBackgroundColor.value = it },
                        label = { Text("Background Color") },
                        trailingIcon = {
                            Button(onClick = {
                                colorPickerVar.value = customBackgroundColor
                                showColorPicker.value = true
                            }) {
                                Text("...")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = customOutlineColor.value,
                        onValueChange = { customOutlineColor.value = it },
                        label = { Text("Outline Color") },
                        trailingIcon = {
                            Button(onClick = {
                                colorPickerVar.value = customOutlineColor
                                showColorPicker.value = true
                            }) {
                                Text("...")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = customTextColor.value,
                        onValueChange = { customTextColor.value = it },
                        label = { Text("Text Color") },
                        trailingIcon = {
                            Button(onClick = {
                                colorPickerVar.value = customTextColor
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
                        value = customCornerRadius,
                        onValueChange = { customCornerRadius = it },
                        label = { Text("Corner Radius (dp)") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = customOutlineThickness,
                        onValueChange = { customOutlineThickness = it },
                        label = { Text("Outline Thickness (dp)") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = customPadding,
                        onValueChange = { customPadding = it },
                        label = { Text("Padding (dp)") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = customFontFamily,
                            onValueChange = { customFontFamily = it },
                            readOnly = true,
                            label = { Text("Font Family") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            getAvailableFontFamilies().forEach { font ->
                                DropdownMenuItem(
                                    text = { Text(font) },
                                    onClick = {
                                        customFontFamily = font
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = customFontSize,
                        onValueChange = { customFontSize = it },
                        label = { Text("Font Size (sp)") },
                        placeholder = { Text("Default: ${DEFAULT_STUDENT_FONT_SIZE_SP}") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = customFontColor.value,
                        onValueChange = { customFontColor.value = it },
                        label = { Text("Font Color") },
                        trailingIcon = {
                            Button(onClick = {
                                colorPickerVar.value = customFontColor
                                showColorPicker.value = true
                            }) {
                                Text("...")
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updatedStudent = student!!.copy(
                            customBackgroundColor = customBackgroundColor.value.ifBlank { null },
                            customOutlineColor = customOutlineColor.value.ifBlank { null },
                            customTextColor = customTextColor.value.ifBlank { null },
                            customWidth = customWidth.toIntOrNull(),
                            customHeight = customHeight.toIntOrNull(),
                            customCornerRadius = customCornerRadius.toIntOrNull(),
                            customOutlineThickness = customOutlineThickness.toIntOrNull(),
                            customPadding = customPadding.toIntOrNull(),
                            customFontFamily = customFontFamily.ifBlank { null },
                            customFontSize = customFontSize.toIntOrNull(),
                            customFontColor = customFontColor.value.ifBlank { null }
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