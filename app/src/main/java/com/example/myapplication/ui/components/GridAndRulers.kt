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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import android.util.LongSparseArray
import androidx.compose.ui.graphics.graphicsLayer
import com.example.myapplication.data.Guide
import com.example.myapplication.data.GuideType
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Renders the background grid, coordinate rulers, and draggable alignment guides.
 */
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

    val density = LocalDensity.current
    val gridSizePx = remember(gridSize, density) { with(density) { gridSize.dp.toPx() } }
    val rulerThickness = remember(density) { with(density) { 30.dp.toPx() } }
    val textSize = remember(density) { with(density) { 12.dp.toPx() } }
    val parsedBackgroundColor = remember(canvasBackgroundColor) {
        try { Color(android.graphics.Color.parseColor(canvasBackgroundColor)) } catch (e: Exception) { Color.White }
    }
    val rulerPaint = remember(textSize) {
        android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textAlign = android.graphics.Paint.Align.CENTER
            this.textSize = textSize
        }
    }
    val labelCache = remember { LongSparseArray<String>() }

    val gridPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.LTGRAY
            strokeWidth = 1f
            style = android.graphics.Paint.Style.STROKE
        }
    }
    val tickPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            strokeWidth = 1f
            style = android.graphics.Paint.Style.STROKE
        }
    }
    val guidePaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.RED
            strokeWidth = 2f
            style = android.graphics.Paint.Style.STROKE
        }
    }

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
                        val originalGuide = guides.find { g -> g.id == it.id }
                        if (originalGuide != null) {
                            seatingChartViewModel.updateGuidePosition(originalGuide, it.position)
                        }
                    }
                    draggedGuide = null
                }
            )
        }) {
        drawRect(color = parsedBackgroundColor)

        withTransform({
            translate(left = offset.x, top = offset.y)
            scale(scale, scale)
        }) {
            if (showGrid && gridSizePx > 0) {
                val worldXStart = -offset.x / scale
                val worldYStart = -offset.y / scale
                val worldXEnd = worldXStart + canvasSize.width / scale
                val worldYEnd = worldYStart + canvasSize.height / scale

                drawIntoCanvas { canvas ->
                    gridPaint.strokeWidth = 1f / scale
                    val firstLineX = floor(worldXStart / gridSizePx) * gridSizePx
                    var currentX = firstLineX
                    while (currentX < worldXEnd) {
                        canvas.nativeCanvas.drawLine(currentX, worldYStart, currentX, worldYEnd, gridPaint)
                        currentX += gridSizePx
                    }

                    val firstLineY = floor(worldYStart / gridSizePx) * gridSizePx
                    var currentY = firstLineY
                    while (currentY < worldYEnd) {
                        canvas.nativeCanvas.drawLine(worldXStart, currentY, worldXEnd, currentY, gridPaint)
                        currentY += gridSizePx
                    }
                }
            }

            if (showRulers || guidesStayWhenRulersHidden) {
                val worldXStart = -offset.x / scale
                val worldYStart = -offset.y / scale
                val worldXEnd = worldXStart + canvasSize.width / scale
                val worldYEnd = worldYStart + canvasSize.height / scale
                drawIntoCanvas { canvas ->
                    guidePaint.strokeWidth = 2f / scale
                    for (i in guides.indices) {
                        val guide = guides[i]
                        if (guide.type == GuideType.HORIZONTAL) {
                            canvas.nativeCanvas.drawLine(worldXStart, guide.position, worldXEnd, guide.position, guidePaint)
                        } else {
                            canvas.nativeCanvas.drawLine(guide.position, worldYStart, guide.position, worldYEnd, guidePaint)
                        }
                    }
                }
            }
        }

        if (showRulers && gridSizePx > 0) {
            drawIntoCanvas { canvas ->
                val worldXStart = -offset.x / scale
                val worldXEnd = (size.width - offset.x) / scale
                val firstTickX = ceil(worldXStart / gridSizePx).toInt()
                val lastTickX = floor(worldXEnd / gridSizePx).toInt()

                for (i in firstTickX..lastTickX) {
                    val worldX = i * gridSizePx
                    val screenX = (worldX * scale) + offset.x
                    if (screenX >= 0 && screenX <= size.width) {
                        canvas.nativeCanvas.drawLine(screenX, 0f, screenX, rulerThickness / 2, tickPaint)

                        val worldLong = worldX.toLong()
                        val label = labelCache.get(worldLong) ?: worldLong.toString().also { labelCache.put(worldLong, it) }

                        canvas.nativeCanvas.drawText(
                            label,
                            screenX, rulerThickness, rulerPaint
                        )
                    }
                }

                val worldYStart = -offset.y / scale
                val worldYEnd = (size.height - offset.y) / scale
                val firstTickY = ceil(worldYStart / gridSizePx).toInt()
                val lastTickY = floor(worldYEnd / gridSizePx).toInt()

                for (i in firstTickY..lastTickY) {
                    val worldY = i * gridSizePx
                    val screenY = (worldY * scale) + offset.y
                    if (screenY >= 0 && screenY <= size.height) {
                        canvas.nativeCanvas.drawLine(0f, screenY, rulerThickness / 2, screenY, tickPaint)

                        val worldLong = worldY.toLong()
                        val label = labelCache.get(worldLong) ?: worldLong.toString().also { labelCache.put(worldLong, it) }

                        canvas.nativeCanvas.save()
                        canvas.nativeCanvas.translate(rulerThickness, screenY)
                        canvas.nativeCanvas.rotate(-90f)
                        canvas.nativeCanvas.drawText(
                            label,
                            0f, 0f, rulerPaint
                        )
                        canvas.nativeCanvas.restore()
                    }
                }
            }
        }
    }
}
