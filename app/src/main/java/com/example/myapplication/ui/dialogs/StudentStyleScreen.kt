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
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.preferences.DEFAULT_STUDENT_FONT_SIZE_SP
import com.example.myapplication.ui.components.ColorPickerField
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.StudentStyleViewModel
import com.example.myapplication.utils.getAvailableFontFamilies
import com.example.myapplication.utils.safeParseColor
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
    val defaultStyle by viewModel.defaultStudentStyle.collectAsState()

    // Declare state variables
    var customBackgroundColor by remember { mutableStateOf("") }
    var customOutlineColor by remember { mutableStateOf("") }
    var customTextColor by remember { mutableStateOf("") }
    var customWidth by remember { mutableStateOf("") }
    var customHeight by remember { mutableStateOf("") }
    var customOutlineThickness by remember { mutableStateOf("") }
    var customFontFamily by remember { mutableStateOf("") }
    var customFontSize by remember { mutableStateOf("") }
    var customFontColor by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val showColorPicker = remember { mutableStateOf(false) }
    val colorPickerVar = remember { mutableStateOf<MutableState<String>?>(null) }

    LaunchedEffect(studentId) {
        viewModel.loadStudent(studentId)
    }

    LaunchedEffect(student) {
        student?.let {
            customBackgroundColor = it.customBackgroundColor ?: ""
            customOutlineColor = it.customOutlineColor ?: ""
            customTextColor = it.customTextColor ?: ""
            customWidth = it.customWidth?.toString() ?: ""
            customHeight = it.customHeight?.toString() ?: ""
            customOutlineThickness = it.customOutlineThickness?.toString() ?: ""
            customFontFamily = it.customFontFamily ?: ""
            customFontSize = it.customFontSize?.toString() ?: ""
            customFontColor = it.customFontColor ?: ""
        }
    }

    if (student != null) {

        if (showColorPicker.value) {
            val initialColorStr = colorPickerVar.value?.value
            val initialColor = if (initialColorStr.isNullOrBlank()) Color.White else safeParseColor(initialColorStr)
            ColorPickerDialog(
                onColorSelected = { color ->
                    colorPickerVar.value?.value = color
                    showColorPicker.value = false
                },
                onDismiss = { showColorPicker.value = false },
                initialColor = initialColor
            )
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.student_style_screen_title)) },
            text = {
                Column(modifier = Modifier.padding(16.dp)) {
                    ColorPickerField(
                        label = stringResource(R.string.student_style_screen_background_color),
                        color = customBackgroundColor,
                        onColorChange = { customBackgroundColor = it },
                        onColorPickerClick = {
                            colorPickerVar.value = mutableStateOf(customBackgroundColor)
                            showColorPicker.value = true
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ColorPickerField(
                        label = stringResource(R.string.student_style_screen_outline_color),
                        color = customOutlineColor,
                        onColorChange = { customOutlineColor = it },
                        onColorPickerClick = {
                            colorPickerVar.value = mutableStateOf(customOutlineColor)
                            showColorPicker.value = true
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ColorPickerField(
                        label = stringResource(R.string.student_style_screen_text_color),
                        color = customTextColor,
                        onColorChange = { customTextColor = it },
                        onColorPickerClick = {
                            colorPickerVar.value = mutableStateOf(customTextColor)
                            showColorPicker.value = true
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = customWidth,
                        onValueChange = { customWidth = it.filter { char -> char.isDigit() } },
                        label = { Text(stringResource(R.string.student_style_screen_width)) },
                        placeholder = { Text(defaultStyle.width.toString()) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = customHeight,
                        onValueChange = { customHeight = it.filter { char -> char.isDigit() } },
                        label = { Text(stringResource(R.string.student_style_screen_height)) },
                        placeholder = { Text(defaultStyle.height.toString()) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = customOutlineThickness,
                        onValueChange = { customOutlineThickness = it.filter { char -> char.isDigit() } },
                        label = { Text(stringResource(R.string.student_style_screen_outline_thickness)) },
                        placeholder = { Text(defaultStyle.outlineThickness.toString()) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = if (customFontFamily.isBlank()) "Default (${defaultStyle.fontFamily})" else customFontFamily,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text(stringResource(R.string.student_style_screen_font_family)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Default (${defaultStyle.fontFamily})") },
                                onClick = {
                                    customFontFamily = ""
                                    expanded = false
                                }
                            )
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
                        onValueChange = { customFontSize = it.filter { char -> char.isDigit() } },
                        label = { Text(stringResource(R.string.student_style_screen_font_size)) },
                        placeholder = { Text(defaultStyle.fontSize.toString()) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ColorPickerField(
                        label = stringResource(R.string.student_style_screen_font_color),
                        color = customFontColor,
                        onColorChange = { customFontColor = it },
                        onColorPickerClick = {
                            colorPickerVar.value = mutableStateOf(customFontColor)
                            showColorPicker.value = true
                        }
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
                            customOutlineThickness = customOutlineThickness.toIntOrNull(),
                            customFontFamily = customFontFamily.ifBlank { null },
                            customFontSize = customFontSize.toIntOrNull(),
                            customFontColor = customFontColor.ifBlank { null }
                        )
                        viewModel.updateStudent(seatingChartViewModel, updatedStudent)
                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.student_style_screen_save))
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text(stringResource(R.string.student_style_screen_cancel))
                }
            }
        )
    }
}

@Composable
fun ColorPickerDialog(
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    initialColor: Color = Color.White
) {
    val controller = rememberColorPickerController()
    var color by remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.color_picker_dialog_title)) },
        text = {
            HsvColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                controller = controller,
                onColorChanged = { colorEnvelope: ColorEnvelope ->
                    color = colorEnvelope.color
                }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onColorSelected(String.format("#%08X", color.toArgb()))
                }
            ) {
                Text(stringResource(R.string.color_picker_dialog_ok))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.color_picker_dialog_cancel))
            }
        }
    )
}
