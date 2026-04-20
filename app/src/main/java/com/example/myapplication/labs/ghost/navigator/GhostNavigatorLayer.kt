package com.example.myapplication.labs.ghost.navigator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostNavigatorLayer: High-performance mini-map for classroom spatial orientation.
 *
 * Renders a stylized, glassmorphic mini-map overlay showing student locations
 * and a "viewfinder" rectangle representing the current viewport.
 *
 * BOLT ⚡ Optimization:
 * 1. Uses a dedicated [Canvas] for drawing all points in a single pass.
 * 2. Implements "Tap-to-Teleport" via [detectTapGestures] on the mini-map area.
 * 3. Minimizes state reads in the draw loop by pre-calculating viewport rects.
 *
 * @param students The current list of students to visualize on the map.
 * @param scale The current zoom level of the main seating chart.
 * @param offset The current pan offset of the main seating chart.
 * @param containerSize The size of the screen/viewport in pixels.
 * @param onTeleport Callback for updating the main seating chart's pan offset.
 * @param isActive Whether the navigator overlay is visible.
 */
@Composable
fun GhostNavigatorLayer(
    students: List<StudentUiItem>,
    scale: Float,
    offset: Offset,
    containerSize: IntSize,
    onTeleport: (Offset) -> Unit,
    isActive: Boolean = false
) {
    if (!isActive || containerSize.width == 0 || containerSize.height == 0) return

    val miniMapSize = 160.dp
    val studentColor = Color.Cyan.copy(alpha = 0.8f)
    val viewportColor = Color.White.copy(alpha = 0.6f)
    val backgroundColor = Color.Black.copy(alpha = 0.4f)
    val borderColor = Color.Cyan.copy(alpha = 0.3f)

    // Calculate normalized viewport
    val normalizedViewport = remember(scale, offset, containerSize) {
        GhostNavigatorEngine.calculateNormalizedViewport(scale, offset, containerSize)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .size(miniMapSize)
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .pointerInput(scale, containerSize) {
                    detectTapGestures { tapOffset ->
                        // Normalize the tap offset (0..1) relative to mini-map size
                        val normalizedTap = Offset(
                            x = tapOffset.x / size.width,
                            y = tapOffset.y / size.height
                        )
                        val newOffset = GhostNavigatorEngine.calculateOffsetForTap(
                            normalizedTap = normalizedTap,
                            currentScale = scale,
                            containerSize = containerSize
                        )
                        onTeleport(newOffset)
                    }
                }
        ) {
            val dotRadius = 2.dp
            Canvas(modifier = Modifier.fillMaxSize()) {
                val mapWidth = size.width
                val mapHeight = size.height
                val dotRadiusPx = dotRadius.toPx()
                val invLogicalSize = 1f / GhostNavigatorEngine.LOGICAL_CANVAS_SIZE
                val scaleX = mapWidth * invLogicalSize
                val scaleY = mapHeight * invLogicalSize

                // 1. Draw Students as tiny dots
                // BOLT: Manual index loop to eliminate iterator allocations in 60fps path
                for (i in students.indices) {
                    val student = students[i]
                    val normX = student.xPosition.value * scaleX
                    val normY = student.yPosition.value * scaleY

                    drawCircle(
                        color = studentColor,
                        radius = dotRadiusPx,
                        center = Offset(normX, normY)
                    )
                }

                // 2. Draw Viewfinder (current viewport)
                val rectX = normalizedViewport.left * mapWidth
                val rectY = normalizedViewport.top * mapHeight
                val rectW = (normalizedViewport.right - normalizedViewport.left) * mapWidth
                val rectH = (normalizedViewport.bottom - normalizedViewport.top) * mapHeight

                drawRect(
                    color = viewportColor,
                    topLeft = Offset(rectX, rectY),
                    size = Size(rectW, rectH),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Fill the viewport slightly to make it stand out
                drawRect(
                    color = viewportColor.copy(alpha = 0.1f),
                    topLeft = Offset(rectX, rectY),
                    size = Size(rectW, rectH)
                )
            }
        }
    }
}
