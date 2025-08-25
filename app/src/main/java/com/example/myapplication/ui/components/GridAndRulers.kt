package com.example.myapplication.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Guide
import com.example.myapplication.data.GuideType
import com.example.myapplication.viewmodel.GuideViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun GridAndRulers(
    settingsViewModel: SettingsViewModel,
    guideViewModel: GuideViewModel,
    scale: Float,
    offset: Offset,
    canvasSize: androidx.compose.ui.geometry.Size
) {
    val showGrid by settingsViewModel.showGrid.collectAsState()
    val gridSize by settingsViewModel.gridSize.collectAsState()
    val showRulers by settingsViewModel.showRulers.collectAsState()
    val guides by guideViewModel.guides.collectAsState()
    var draggedGuide by remember { mutableStateOf<Guide?>(null) }

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Grid (drawn within the transformed canvas)
        withTransform({
            scale(scale, scale)
            translate(left = offset.x, top = offset.y)
        }) {
            if (showGrid) {
                val gridSizePx = gridSize.dp.toPx()

                // Calculate visible world coordinates
                val worldXStart = -offset.x / scale
                val worldYStart = -offset.y / scale
                val worldXEnd = worldXStart + canvasSize.width / scale
                val worldYEnd = worldYStart + canvasSize.height / scale

                // Determine the first and last grid lines to draw
                val firstLineX = floor(worldXStart / gridSizePx) * gridSizePx
                var currentX = firstLineX
                while (currentX < worldXEnd) {
                    drawLine(
                        start = Offset(currentX, worldYStart),
                        end = Offset(currentX, worldYEnd),
                        color = Color.LightGray,
                        strokeWidth = 1f / scale
                    )
                    currentX += gridSizePx
                }

                val firstLineY = floor(worldYStart / gridSizePx) * gridSizePx
                var currentY = firstLineY
                while (currentY < worldYEnd) {
                    drawLine(
                        start = Offset(worldXStart, currentY),
                        end = Offset(worldXEnd, currentY),
                        color = Color.LightGray,
                        strokeWidth = 1f / scale
                    )
                    currentY += gridSizePx
                }
            }
        }

        // Rulers (drawn outside the transform, in screen space)
        if (showRulers) {
            val gridSizePx = gridSize.dp.toPx()
            val rulerThickness = 30.dp.toPx()
            val textSize = 12.dp.toPx()

            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textAlign = android.graphics.Paint.Align.CENTER
                    this.textSize = textSize
                }

                // Horizontal Ruler
                val worldXStart = -offset.x / scale
                val worldXEnd = (size.width - offset.x) / scale
                val firstTickX = ceil(worldXStart / gridSizePx).toInt()
                val lastTickX = floor(worldXEnd / gridSizePx).toInt()

                for (i in firstTickX..lastTickX) {
                    val worldX = i * gridSizePx
                    val screenX = (worldX * scale) + offset.x
                    if (screenX >= 0 && screenX <= size.width) {
                        drawLine(
                            start = Offset(screenX, 0f),
                            end = Offset(screenX, rulerThickness / 2),
                            color = Color.Black, strokeWidth = 1f
                        )
                        canvas.nativeCanvas.drawText(
                            (worldX).toLong().toString(),
                            screenX, rulerThickness, paint
                        )
                    }
                }

                // Vertical Ruler
                val worldYStart = -offset.y / scale
                val worldYEnd = (size.height - offset.y) / scale
                val firstTickY = ceil(worldYStart / gridSizePx).toInt()
                val lastTickY = floor(worldYEnd / gridSizePx).toInt()

                for (i in firstTickY..lastTickY) {
                    val worldY = i * gridSizePx
                    val screenY = (worldY * scale) + offset.y
                    if (screenY >= 0 && screenY <= size.height) {
                        drawLine(
                            start = Offset(0f, screenY),
                            end = Offset(rulerThickness / 2, screenY),
                            color = Color.Black, strokeWidth = 1f
                        )
                        canvas.nativeCanvas.save()
                        canvas.nativeCanvas.rotate(-90f, rulerThickness, screenY)
                        canvas.nativeCanvas.drawText(
                            (worldY).toLong().toString(),
                            rulerThickness, screenY, paint
                        )
                        canvas.nativeCanvas.restore()
                    }
                }
            }
        }
    }
}

