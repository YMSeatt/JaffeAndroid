package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.util.safeParseColor

/**
 * A specialized text field for entering and previewing Hex color strings.
 *
 * This component provides a text input for direct Hex code entry and a companion
 * color swatch that displays the current color. Tapping the swatch triggers an
 * external color picker dialog.
 *
 * @param label The label to display for the text field.
 * @param color The current Hex color string (e.g., "#RRGGBB").
 * @param onColorChange Callback triggered when the text input changes.
 * @param onColorPickerClick Callback triggered when the color swatch is tapped.
 */
@Composable
fun ColorPickerField(
    label: String,
    color: String,
    onColorChange: (String) -> Unit,
    onColorPickerClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = color,
            onValueChange = onColorChange,
            label = { Text(label) },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(safeParseColor(color.ifBlank { "#FFFFFFFF" }))
                .border(1.dp, MaterialTheme.colorScheme.onSurface)
                .clickable { onColorPickerClick() }
        )
    }
}
