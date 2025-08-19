package com.example.myapplication.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.preferences.AppTheme
import com.example.myapplication.viewmodel.SettingsViewModel
import java.util.Locale

@Composable
fun GeneralSettingsTab(
    settingsViewModel: SettingsViewModel,
    onShowSetPasswordDialog: () -> Unit,
    onShowChangePasswordDialog: () -> Unit
) {
    val appTheme by settingsViewModel.appTheme.collectAsStateWithLifecycle()
    val passwordEnabled by settingsViewModel.passwordEnabled.collectAsState()
    val noAnimations by settingsViewModel.noAnimations.collectAsState()
    val autosaveInterval by settingsViewModel.autosaveInterval.collectAsState()
    val gridSnapEnabled by settingsViewModel.gridSnapEnabled.collectAsState()
    val gridSize by settingsViewModel.gridSize.collectAsState()
    val showRulers by settingsViewModel.showRulers.collectAsState()
    val showGrid by settingsViewModel.showGrid.collectAsState()

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Enable Password Protection", modifier = Modifier.weight(1f))
            Switch(checked = passwordEnabled, onCheckedChange = {
                if (it) {
                    onShowSetPasswordDialog()
                } else {
                    settingsViewModel.updatePasswordEnabled(false)
                }
            })
        }
        Button(onClick = onShowChangePasswordDialog, enabled = passwordEnabled) {
            Text("Change Password")
        }
        HorizontalDivider()

        Text("Autosave Interval (seconds):", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = (autosaveInterval / 1000).toFloat(),
                onValueChange = { settingsViewModel.updateAutosaveInterval(it.toInt() * 1000) },
                valueRange = 10f..300f,
                steps = 28,
                modifier = Modifier.weight(1f)
            )
            Text((autosaveInterval / 1000).toString())
        }
        HorizontalDivider()

        Text("App Theme:", style = MaterialTheme.typography.titleMedium)
        AppTheme.entries.forEach { theme ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = theme.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }, modifier = Modifier.padding(start = 8.dp))
                RadioButton(
                    selected = (appTheme == theme),
                    onClick = { settingsViewModel.updateAppTheme(theme) }
                )
            }
        }
        HorizontalDivider()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Disable animations", modifier = Modifier.weight(1f))
            Switch(
                checked = noAnimations,
                onCheckedChange = { settingsViewModel.updateNoAnimations(it) }
            )
        }
        HorizontalDivider()

        Text("Canvas:", style = MaterialTheme.typography.titleMedium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enable Snap to Grid", modifier = Modifier.weight(1f))
            Switch(
                checked = gridSnapEnabled,
                onCheckedChange = { settingsViewModel.updateGridSnapEnabled(it) }
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Grid Size (pixels):", modifier = Modifier.weight(1f))
            Slider(
                value = gridSize.toFloat(),
                onValueChange = { settingsViewModel.updateGridSize(it.toInt()) },
                valueRange = 5f..100f,
                steps = 18,
                modifier = Modifier.weight(1f)
            )
            Text(gridSize.toString())
        }
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
}
