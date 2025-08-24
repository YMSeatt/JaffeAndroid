package com.example.myapplication.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.ui.dialogs.ColorPickerDialog
import com.example.myapplication.viewmodel.SettingsViewModel


@Composable
fun DisplaySettingsTab(
    settingsViewModel: SettingsViewModel
) {
    var showColorPicker by remember { mutableStateOf(false) }
    var colorPickerTarget by remember { mutableStateOf("") }
    val recentLogsLimit by settingsViewModel.recentLogsLimit.collectAsStateWithLifecycle()
    val recentBehaviorIncidentsLimit by settingsViewModel.recentBehaviorIncidentsLimit.collectAsState()
    val useInitialsForBehavior by settingsViewModel.useInitialsForBehavior.collectAsState()
    val useFullNameForStudent by settingsViewModel.useFullNameForStudent.collectAsState()
    val showRecentBehavior by settingsViewModel.showRecentBehavior.collectAsState()

    val defaultWidth by settingsViewModel.defaultStudentBoxWidth.collectAsState()
    val defaultHeight by settingsViewModel.defaultStudentBoxHeight.collectAsState()
    val defaultBgColor by settingsViewModel.defaultStudentBoxBackgroundColor.collectAsState()
    val defaultOutlineColor by settingsViewModel.defaultStudentBoxOutlineColor.collectAsState()
    val defaultTextColor by settingsViewModel.defaultStudentBoxTextColor.collectAsState()
    val defaultOutlineThickness by settingsViewModel.defaultStudentBoxOutlineThickness.collectAsState()
    val defaultCornerRadius by settingsViewModel.defaultStudentBoxCornerRadius.collectAsState()
    val defaultPadding by settingsViewModel.defaultStudentBoxPadding.collectAsState()

    var defaultWidthInput by remember(defaultWidth) { mutableStateOf(defaultWidth.toString()) }
    var defaultHeightInput by remember(defaultHeight) { mutableStateOf(defaultHeight.toString()) }
    var defaultBgColorInput by remember(defaultBgColor) { mutableStateOf(defaultBgColor) }
    var defaultOutlineColorInput by remember(defaultOutlineColor) { mutableStateOf(defaultOutlineColor) }
    var defaultTextColorInput by remember(defaultTextColor) { mutableStateOf(defaultTextColor) }
    var defaultOutlineThicknessInput by remember(defaultOutlineThickness) { mutableStateOf(defaultOutlineThickness.toString()) }
    var defaultCornerRadiusInput by remember(defaultCornerRadius) { mutableStateOf(defaultCornerRadius.toString()) }
    var defaultPaddingInput by remember(defaultPadding) { mutableStateOf(defaultPadding.toString()) }

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Number of recent homework logs to display:", style = MaterialTheme.typography.titleMedium)
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    value = recentLogsLimit.toFloat(),
                    onValueChange = { settingsViewModel.updateRecentLogsLimit(it.toInt()) },
                    valueRange = 1f..50f,
                    steps = 48,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(recentLogsLimit.toString())
            }
        }
        item {
            HorizontalDivider()
        }

        item {
            Text("Number of recent behavior incidents to display:", style = MaterialTheme.typography.titleMedium)
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    value = recentBehaviorIncidentsLimit.toFloat(),
                    onValueChange = { settingsViewModel.updateRecentBehaviorIncidentsLimit(it.toInt()) },
                    valueRange = 1f..50f,
                    steps = 48,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(recentBehaviorIncidentsLimit.toString())
            }
        }
        item {
            HorizontalDivider()
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Use initials for behavior logs (e.g., OOS)", modifier = Modifier.weight(1f))
                Switch(
                    checked = useInitialsForBehavior,
                    onCheckedChange = { settingsViewModel.updateUseInitialsForBehavior(it) }
                )
            }
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Use full name for student boxes", modifier = Modifier.weight(1f))
                Switch(
                    checked = useFullNameForStudent,
                    onCheckedChange = { settingsViewModel.updateUseFullNameForStudent(it) }
                )
            }
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show recent behavior on student boxes", modifier = Modifier.weight(1f))
                Switch(
                    checked = showRecentBehavior,
                    onCheckedChange = { settingsViewModel.updateShowRecentBehavior(it) }
                )
            }
        }
        item {
            HorizontalDivider()
        }

        item {
            Text("Default Student Box Appearance", style = MaterialTheme.typography.titleMedium)
        }
        item {
            Spacer(Modifier.height(8.dp))
        }

        item {
            OutlinedTextField(
                value = defaultWidthInput,
                onValueChange = {
                    defaultWidthInput = it
                    it.toIntOrNull()?.let { value -> settingsViewModel.updateDefaultStudentBoxWidth(value) }
                },
                label = { Text("Default Box Width (dp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Spacer(Modifier.height(8.dp))
        }
        item {
            OutlinedTextField(
                value = defaultHeightInput,
                onValueChange = {
                    defaultHeightInput = it
                    it.toIntOrNull()?.let { value -> settingsViewModel.updateDefaultStudentBoxHeight(value) }
                },
                label = { Text("Default Box Height (dp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Spacer(Modifier.height(8.dp))
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = defaultBgColorInput,
                    onValueChange = {
                        defaultBgColorInput = it
                        try {
                            // Attempt to parse to validate the color format
                            android.graphics.Color.parseColor(it)
                            settingsViewModel.updateDefaultStudentBoxBackgroundColor(it)
                        } catch (e: IllegalArgumentException) {
                            // Optionally handle invalid color input, e.g., show an error
                        }
                    },
                    label = { Text("Default Background Color") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    colorPickerTarget = "bg"
                    showColorPicker = true
                }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Choose Color")
                }
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = defaultOutlineColorInput,
                    onValueChange = {
                        defaultOutlineColorInput = it
                        try {
                            // Attempt to parse to validate the color format
                            android.graphics.Color.parseColor(it)
                            settingsViewModel.updateDefaultStudentBoxOutlineColor(it)
                        } catch (e: IllegalArgumentException) {
                            // Optionally handle invalid color input, e.g., show an error
                        }
                    },
                    label = { Text("Default Outline Color") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    colorPickerTarget = "outline"
                    showColorPicker = true
                }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Choose Color")
                }
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = defaultTextColorInput,
                    onValueChange = {
                        defaultTextColorInput = it
                        try {
                            // Attempt to parse to validate the color format
                            android.graphics.Color.parseColor(it)
                            settingsViewModel.updateDefaultStudentBoxTextColor(it)
                        } catch (e: IllegalArgumentException) {
                            // Optionally handle invalid color input, e.g., show an error
                        }
                    },
                    label = { Text("Default Text Color") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    colorPickerTarget = "text"
                    showColorPicker = true
                }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Choose Color")
                }
            }
        }

        if (showColorPicker) {
            item {
                ColorPickerDialog(
                    onDismissRequest = { showColorPicker = false },
                    onColorSelected = { color ->
                        val hexColor = String.format("#%06X", (0xFFFFFF and color.toArgb()))
                        when (colorPickerTarget) {
                            "bg" -> {
                                defaultBgColorInput = hexColor
                                settingsViewModel.updateDefaultStudentBoxBackgroundColor(hexColor)
                            }
                            "outline" -> {
                                defaultOutlineColorInput = hexColor
                                settingsViewModel.updateDefaultStudentBoxOutlineColor(hexColor)
                            }
                            "text" -> {
                                defaultTextColorInput = hexColor
                                settingsViewModel.updateDefaultStudentBoxTextColor(hexColor)
                            }
                        }
                        showColorPicker = false
                    }
                )
            }
        }
        item {
            OutlinedTextField(
                value = defaultOutlineThicknessInput,
                onValueChange = {
                    defaultOutlineThicknessInput = it
                    it.toIntOrNull()?.let { value ->
                        settingsViewModel.updateDefaultStudentBoxOutlineThickness(
                            value.coerceIn(0, 10)
                        )
                    }
                },
                label = { Text("Default Box Outline Thickness (dp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Spacer(Modifier.height(8.dp))
        }
        item {
            OutlinedTextField(
                value = defaultCornerRadiusInput,
                onValueChange = {
                    defaultCornerRadiusInput = it
                    it.toIntOrNull()?.let { value ->
                        settingsViewModel.updateDefaultStudentBoxCornerRadius(
                            value.coerceIn(0, 50)
                        )
                    }
                },
                label = { Text("Default Box Corner Radius (dp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Spacer(Modifier.height(8.dp))
        }
        item {
            OutlinedTextField(
                value = defaultPaddingInput,
                onValueChange = {
                    defaultPaddingInput = it
                    it.toIntOrNull()?.let { value ->
                        settingsViewModel.updateDefaultStudentBoxPadding(
                            value.coerceIn(0, 50)
                        )
                    }
                },
                label = { Text("Default Box Padding (dp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            HorizontalDivider(Modifier.padding(top = 16.dp))
        }

        item {
            Text("Canvas Settings", style = MaterialTheme.typography.titleMedium)
        }
        item {
            val showGrid by settingsViewModel.showGrid.collectAsState()
    val autoExpandStudentBoxes by settingsViewModel.autoExpandStudentBoxes.collectAsState()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show Grid", modifier = Modifier.weight(1f))
                Switch(
                    checked = showGrid,
                    onCheckedChange = { settingsViewModel.updateShowGrid(it) }
                )
            }
        }
        item {
            val gridSnapEnabled by settingsViewModel.gridSnapEnabled.collectAsState()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Snap to Grid", modifier = Modifier.weight(1f))
                Switch(
                    checked = gridSnapEnabled,
                    onCheckedChange = { settingsViewModel.updateGridSnapEnabled(it) }
                )
            }
        }
        item {
            val showRulers by settingsViewModel.showRulers.collectAsState()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show Rulers", modifier = Modifier.weight(1f))
                Switch(
                    checked = showRulers,
                    onCheckedChange = { settingsViewModel.updateShowRulers(it) }
                )
            }
        }
    }
}
