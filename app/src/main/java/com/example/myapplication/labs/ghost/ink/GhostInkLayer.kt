package com.example.myapplication.labs.ghost.ink

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput

/**
 * GhostInkLayer: The interactive surface for neural annotations.
 *
 * This layer intercepts touch events to record [Stroke]s when active. It correctly
 * transforms screen coordinates into the 4000x4000 logical coordinate space of
 * the seating chart.
 *
 * BOLT ⚡ Optimization: Uses a pool of [RuntimeShader] instances on API 33+ to
 * efficiently render multiple strokes in a single pass without redundant JNI calls.
 */
@Composable
fun GhostInkLayer(
    engine: GhostInkEngine,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val strokes by engine.strokes.collectAsState()
    val currentStrokePoints = remember { mutableStateListOf<Offset>() }

    val infiniteTransition = rememberInfiniteTransition(label = "ink_pulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // BOLT: Shader & Brush Pooling (limit to 16 concurrent strokes for performance)
    // Pre-allocate FloatArray buffer to eliminate per-frame/per-stroke allocations.
    val (shaderPool, brushPool, pointsBuffer) = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val shaders = List(16) { RuntimeShader(GhostInkShader.NEURAL_INK_SHADER) }
            val brushes = shaders.map { ShaderBrush(it) }
            Triple(shaders, brushes, FloatArray(128))
        } else Triple(emptyList(), emptyList(), FloatArray(0))
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(isActive) {
                detectDragGestures(
                    onDragStart = { pos ->
                        val logicalX = (pos.x - canvasOffset.x) / canvasScale
                        val logicalY = (pos.y - canvasOffset.y) / canvasScale
                        val logicalPoint = Offset(logicalX, logicalY)
                        currentStrokePoints.clear()
                        currentStrokePoints.add(logicalPoint)
                        engine.startStroke(logicalPoint)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val pos = change.position
                        val logicalX = (pos.x - canvasOffset.x) / canvasScale
                        val logicalY = (pos.y - canvasOffset.y) / canvasScale
                        val logicalPoint = Offset(logicalX, logicalY)

                        // BOLT: Manual thinning here as well to match engine and reduce UI state churn
                        if (currentStrokePoints.isNotEmpty()) {
                            val last = currentStrokePoints.last()
                            val distSq = (logicalPoint.x - last.x) * (logicalPoint.x - last.x) +
                                         (logicalPoint.y - last.y) * (logicalPoint.y - last.y)
                            if (distSq > 25f) {
                                currentStrokePoints.add(logicalPoint)
                            }
                        } else {
                            currentStrokePoints.add(logicalPoint)
                        }

                        engine.continueStroke(logicalPoint)
                    },
                    onDragEnd = {
                        engine.finishStroke()
                        currentStrokePoints.clear()
                    }
                )
            }
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            withTransform({
                translate(canvasOffset.x, canvasOffset.y)
                scale(canvasScale, canvasScale, Offset.Zero)
            }) {
                // Render persistent strokes (up to 15) plus the 1 active stroke
                val persistentStrokes = strokes
                val hasActive = currentStrokePoints.size > 1
                val totalAvailable = if (hasActive) persistentStrokes.size + 1 else persistentStrokes.size
                val drawCount = minOf(totalAvailable, 16)

                for (i in 0 until drawCount) {
                    val shader = shaderPool[i]
                    val brush = brushPool[i]

                    // Determine which stroke to draw (favoring most recent persistent strokes + active)
                    val points: List<Offset>
                    val colorLong: Long

                    if (hasActive && i == drawCount - 1) {
                        points = currentStrokePoints
                        colorLong = 0xFF00E5FF // Default Neon Cyan
                    } else {
                        val strokeIdx = persistentStrokes.size - (drawCount - (if (hasActive) 1 else 0)) + i
                        if (strokeIdx >= 0 && strokeIdx < persistentStrokes.size) {
                            val stroke = persistentStrokes[strokeIdx]
                            points = stroke.points
                            colorLong = stroke.color
                        } else continue
                    }

                    val pCount = minOf(points.size, 64)

                    // BOLT: Zero-allocation point population using pre-allocated buffer
                    for (pIdx in 0 until pCount) {
                        val p = points[pIdx]
                        pointsBuffer[pIdx * 2] = p.x
                        pointsBuffer[pIdx * 2 + 1] = p.y
                    }

                    shader.setFloatUniform("iResolution", size.width, size.height)
                    shader.setFloatUniform("iTime", time)
                    shader.setFloatUniform("iPoints", pointsBuffer)
                    shader.setIntUniform("iPointCount", pCount)

                    // BOLT: Direct bitwise color extraction to avoid Color object churn and JNI overhead
                    val a = ((colorLong shr 24) and 0xFF) / 255.0f
                    val r = ((colorLong shr 16) and 0xFF) / 255.0f
                    val g = ((colorLong shr 8) and 0xFF) / 255.0f
                    val b = (colorLong and 0xFF) / 255.0f
                    shader.setFloatUniform("iColor", r, g, b, a)
                    shader.setFloatUniform("iIntensity", 8.0f)

                    drawRect(brush = brush)
                }
            }
        } else {
            // Fallback for API < 33: Simple Path drawing
            withTransform({
                translate(canvasOffset.x, canvasOffset.y)
                scale(canvasScale, canvasScale, Offset.Zero)
            }) {
                val persistentStrokes = strokes
                // Draw persistent strokes
                for (sIdx in persistentStrokes.indices) {
                    val stroke = persistentStrokes[sIdx]
                    val points = stroke.points
                    if (points.size > 1) {
                        val color = Color(stroke.color).copy(alpha = 0.6f)
                        val strokeWidth = 4f / canvasScale
                        for (i in 0 until points.size - 1) {
                            drawLine(
                                color = color,
                                start = points[i],
                                end = points[i + 1],
                                strokeWidth = strokeWidth
                            )
                        }
                    }
                }

                // Draw active stroke
                if (currentStrokePoints.size > 1) {
                    val color = Color(0xFF00E5FF).copy(alpha = 0.6f)
                    val strokeWidth = 4f / canvasScale
                    for (i in 0 until currentStrokePoints.size - 1) {
                        drawLine(
                            color = color,
                            start = currentStrokePoints[i],
                            end = currentStrokePoints[i + 1],
                            strokeWidth = strokeWidth
                        )
                    }
                }
            }
        }
    }
}
