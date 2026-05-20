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
    var currentStrokePoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

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

    // BOLT: Shader Pooling (limit to 16 concurrent shaders for performance)
    val shaderPool = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            List(16) { RuntimeShader(GhostInkShader.NEURAL_INK_SHADER) }
        } else emptyList()
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
                        currentStrokePoints = listOf(logicalPoint)
                        engine.startStroke(logicalPoint)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val pos = change.position
                        val logicalX = (pos.x - canvasOffset.x) / canvasScale
                        val logicalY = (pos.y - canvasOffset.y) / canvasScale
                        val logicalPoint = Offset(logicalX, logicalY)
                        currentStrokePoints = currentStrokePoints + logicalPoint
                        engine.continueStroke(logicalPoint)
                    },
                    onDragEnd = {
                        engine.finishStroke()
                        currentStrokePoints = emptyList()
                    }
                )
            }
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            withTransform({
                translate(canvasOffset.x, canvasOffset.y)
                scale(canvasScale, canvasScale, Offset.Zero)
            }) {
                // Render persistent strokes
                val allStrokes = if (currentStrokePoints.size > 1) {
                    strokes + GhostInkEngine.Stroke(currentStrokePoints)
                } else strokes

                allStrokes.takeLast(16).forEachIndexed { index, stroke ->
                    val shader = shaderPool[index]
                    val points = stroke.points.take(64)
                    val pointsArray = FloatArray(128)
                    points.forEachIndexed { pIdx, point ->
                        pointsArray[pIdx * 2] = point.x
                        pointsArray[pIdx * 2 + 1] = point.y
                    }

                    shader.setFloatUniform("iResolution", size.width, size.height)
                    shader.setFloatUniform("iTime", time)
                    shader.setFloatUniform("iPoints", pointsArray)
                    shader.setIntUniform("iPointCount", points.size)
                    val c = Color(stroke.color)
                    shader.setColorUniform("iColor", android.graphics.Color.valueOf(c.red, c.green, c.blue, c.alpha))
                    shader.setFloatUniform("iIntensity", 8.0f)

                    drawRect(brush = ShaderBrush(shader))
                }
            }
        } else {
            // Fallback for API < 33: Simple Path drawing
            withTransform({
                translate(canvasOffset.x, canvasOffset.y)
                scale(canvasScale, canvasScale, Offset.Zero)
            }) {
                val allStrokes = if (currentStrokePoints.size > 1) {
                    strokes + GhostInkEngine.Stroke(currentStrokePoints)
                } else strokes

                allStrokes.forEach { stroke ->
                    if (stroke.points.size > 1) {
                        for (i in 0 until stroke.points.size - 1) {
                            drawLine(
                                color = Color(stroke.color).copy(alpha = 0.6f),
                                start = stroke.points[i],
                                end = stroke.points[i + 1],
                                strokeWidth = 4f / canvasScale
                            )
                        }
                    }
                }
            }
        }
    }
}
