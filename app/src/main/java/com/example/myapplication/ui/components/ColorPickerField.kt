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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.myapplication.utils.safeParseColor

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
