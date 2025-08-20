package com.example.myapplication.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodel.SettingsViewModel

@Composable
fun GridAndRulers(
    settingsViewModel: SettingsViewModel,
    scale: Float,
    offsetX: Float,
    offsetY: Float
) {
    val showGrid by settingsViewModel.showGrid.collectAsState()
    val gridSize by settingsViewModel.gridSize.collectAsState()
    val showRulers by settingsViewModel.showRulers.collectAsState()

    if (showGrid) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val gridSizePx = gridSize.dp.toPx() * scale

            val left = -offsetX / scale
            val right = left + canvasWidth / scale
            val top = -offsetY / scale
            val bottom = top + canvasHeight / scale

            var x = floor(left / gridSizePx) * gridSizePx
            while (x < right) {
                drawLine(
                    start = Offset(x, top),
                    end = Offset(x, bottom),
                    color = Color.LightGray,
                    strokeWidth = 1f / scale
                )
                x += gridSizePx
            }

            var y = floor(top / gridSizePx) * gridSizePx
            while (y < bottom) {
                drawLine(
                    start = Offset(left, y),
                    end = Offset(right, y),
                    color = Color.LightGray,
                    strokeWidth = 1f / scale
                )
                y += gridSizePx
            }
        }
    }

    if (showRulers) {
        // TODO: Implement rulers
    }
}
