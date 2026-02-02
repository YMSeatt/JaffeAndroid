package com.example.myapplication.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Guide
import com.example.myapplication.data.GuideType
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun GridAndRulers(
    settingsViewModel: SettingsViewModel,
    seatingChartViewModel: SeatingChartViewModel,
    scale: Float,
    offset: Offset,
    canvasSize: androidx.compose.ui.geometry.Size
) {
    val showGrid by settingsViewModel.showGrid.collectAsState()
    val gridSize by settingsViewModel.gridSize.collectAsState()
    val showRulers by settingsViewModel.showRulers.collectAsState()
    val guides by seatingChartViewModel.allGuides.collectAsState()
    var draggedGuide by remember { mutableStateOf<Guide?>(null) }

    val canvasBackgroundColor by settingsViewModel.canvasBackgroundColor.collectAsState()
    val guidesStayWhenRulersHidden by settingsViewModel.guidesStayWhenRulersHidden.collectAsState()

    Canvas(modifier = Modifier
        .fillMaxSize()
        .pointerInput(guides, scale, offset) {
            detectDragGestures(
                onDragStart = { startOffset ->
                    val worldStartOffset = (startOffset - offset) / scale
                    val touchSlop = 16.dp.toPx() / scale
                    draggedGuide = guides.minByOrNull { guide ->
                        if (guide.type == GuideType.HORIZONTAL) {
                            kotlin.math.abs(guide.position - worldStartOffset.y)
                        } else {
                            kotlin.math.abs(guide.position - worldStartOffset.x)
                        }
                    }?.takeIf { guide ->
                        if (guide.type == GuideType.HORIZONTAL) {
                            kotlin.math.abs(guide.position - worldStartOffset.y) < touchSlop
                        } else {
                            kotlin.math.abs(guide.position - worldStartOffset.x) < touchSlop
                        }
                    }
                },
                onDrag = { change, dragAmount ->
                    draggedGuide?.let { guide ->
                        val newPosition = if (guide.type == GuideType.HORIZONTAL) {
                            guide.position + dragAmount.y / scale
                        } else {
                            guide.position + dragAmount.x / scale
                        }
                        draggedGuide = guide.copy(position = newPosition)
                        change.consume()
                    }
                },
                onDragEnd = {
                    draggedGuide?.let {
                        // Find the original guide to get its old position for the command
                        val originalGuide = guides.find { g -> g.id == it.id }
                        if (originalGuide != null) {
                            seatingChartViewModel.updateGuidePosition(originalGuide, it.position)
                        }
                    }
                    draggedGuide = null
                }
            )
        }) {
        // Draw Background
        drawRect(color = try { Color(android.graphics.Color.parseColor(canvasBackgroundColor)) } catch (e: Exception) { Color.White })

        // Grid (drawn within the transformed canvas)
        withTransform({
            translate(left = offset.x, top = offset.y)
            scale(scale, scale)
        }) {
            if (showGrid && gridSize > 0) {
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

            // Draw guides
            if (showRulers || guidesStayWhenRulersHidden) {
                val worldXStart = -offset.x / scale
                val worldYStart = -offset.y / scale
                val worldXEnd = worldXStart + canvasSize.width / scale
                val worldYEnd = worldYStart + canvasSize.height / scale
                guides.forEach { guide ->
                    if (guide.type == GuideType.HORIZONTAL) {
                        drawLine(
                            start = Offset(worldXStart, guide.position),
                            end = Offset(worldXEnd, guide.position),
                            color = Color.Red,
                            strokeWidth = 2f / scale
                        )
                    } else { // vertical
                        drawLine(
                            start = Offset(guide.position, worldYStart),
                            end = Offset(guide.position, worldYEnd),
                            color = Color.Red,
                            strokeWidth = 2f / scale
                        )
                    }
                }
            }
        }

        // Rulers (drawn outside the transform, in screen space)
        if (showRulers && gridSize > 0) {
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
