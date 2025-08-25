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

    Canvas(modifier = Modifier.fillMaxSize()
    ) {
        withTransform({
            translate(left = offset.x, top = offset.y)
            scale(scale, scale)
        }) {
            if (showGrid) {
                val gridSizePx = gridSize.dp.toPx()

                val verticalLines = (canvasSize.width / (gridSizePx * scale)).toInt()
                val horizontalLines = (canvasSize.height / (gridSizePx * scale)).toInt()

                for (i in 0..verticalLines) {
                    val x = i * gridSizePx
                    drawLine(
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(x.toFloat(), canvasSize.height / scale),
                        color = Color.LightGray,
                        strokeWidth = 1f / scale
                    )
                }

                for (i in 0..horizontalLines) {
                    val y = i * gridSizePx
                    drawLine(
                        start = Offset(0f, y.toFloat()),
                        end = Offset(canvasSize.width / scale, y.toFloat()),
                        color = Color.LightGray,
                        strokeWidth = 1f / scale
                    )
                }
            }
        if (showRulers) {
                val rulerThickness = 30.dp.toPx()
                val textSize = 12.dp.toPx()

                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textAlign = android.graphics.Paint.Align.CENTER
                        this.textSize = textSize
                    }

                    // Horizontal Ruler
                    val horizontalRulerY = offset.y
                    for (i in 0..((canvasSize.width / scale).toInt() / gridSize)) {
                        val x = i * gridSize.dp.toPx()
                        drawLine(
                            start = Offset(x, horizontalRulerY),
                            end = Offset(x, horizontalRulerY + rulerThickness / 2),
                            color = Color.Black,
                            strokeWidth = 1f
                        )
                        canvas.nativeCanvas.drawText(
                            (i * gridSize).toString(),
                            x,
                            horizontalRulerY + rulerThickness,
                            paint
                        )
                    }

                    // Vertical Ruler
                    val verticalRulerX = offset.x
                    for (i in 0..((canvasSize.height / scale).toInt() / gridSize)) {
                        val y = i * gridSize.dp.toPx()
                        drawLine(
                            start = Offset(verticalRulerX, y),
                            end = Offset(verticalRulerX + rulerThickness / 2, y),
                            color = Color.Black,
                            strokeWidth = 1f
                        )
                        canvas.nativeCanvas.save()
                        canvas.nativeCanvas.rotate(-90f, verticalRulerX + rulerThickness, y)
                        canvas.nativeCanvas.drawText(
                            (i * gridSize).toString(),
                            verticalRulerX + rulerThickness,
                            y,
                            paint
                        )
                        canvas.nativeCanvas.restore()
                    }
                }
            }
        }
    }
}

