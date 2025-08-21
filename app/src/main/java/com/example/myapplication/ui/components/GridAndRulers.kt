package com.example.myapplication.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodel.SettingsViewModel
import kotlin.math.floor

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
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val gridSizePx = gridSize.dp.toPx()

            val rulerThickness = 30.dp.toPx()
            val textColor = Color.Black
            val textSize = 12.dp.toPx() / scale

            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textAlign = android.graphics.Paint.Align.CENTER
                    this.textSize = textSize
                }

                // Horizontal Ruler
                val horizontalRulerY = -offsetY
                var x = floor(-offsetX / (gridSizePx * scale)) * gridSizePx
                while (x * scale < canvasWidth - offsetX) {
                    val realX = x * scale + offsetX
                    if (realX >= rulerThickness) {
                        drawLine(
                            start = Offset(realX, horizontalRulerY),
                            end = Offset(realX, horizontalRulerY + rulerThickness / 2),
                            color = Color.Black,
                            strokeWidth = 1f / scale
                        )
                        canvas.nativeCanvas.drawText(
                            "${x.toInt()}",
                            realX,
                            horizontalRulerY + rulerThickness * 0.75f,
                            paint
                        )
                    }
                    x += gridSizePx
                }

                // Vertical Ruler
                val verticalRulerX = -offsetX
                var y = floor(-offsetY / (gridSizePx * scale)) * gridSizePx
                while (y * scale < canvasHeight - offsetY) {
                    val realY = y * scale + offsetY
                    if (realY >= rulerThickness) {
                        drawLine(
                            start = Offset(verticalRulerX, realY),
                            end = Offset(verticalRulerX + rulerThickness / 2, realY),
                            color = Color.Black,
                            strokeWidth = 1f / scale
                        )
                        canvas.nativeCanvas.save()
                        canvas.nativeCanvas.rotate(-90f, verticalRulerX + rulerThickness * 0.75f, realY)
                        canvas.nativeCanvas.drawText(
                            "${y.toInt()}",
                            verticalRulerX + rulerThickness * 0.75f,
                            realY,
                            paint
                        )
                        canvas.nativeCanvas.restore()
                    }
                    y += gridSizePx
                }
            }
        }
    }
}
