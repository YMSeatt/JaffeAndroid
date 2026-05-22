package com.example.myapplication.labs.ghost

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer

/**
 * GhostTraceLayer: Renders spatiotemporal student traces using AGSL.
 *
 * This layer visualizes the historical paths of students as they migrate between seats.
 * It utilizes [GhostTraceShader] to create a glowing, temporal effect.
 */
@Composable
fun GhostTraceLayer(
    traces: Map<Long, List<GhostTraceEngine.TracePoint>>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || traces.isEmpty()) return

    val infiniteTransition = rememberInfiniteTransition(label = "trace_pulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // BOLT ⚡ Optimization: Render all traces in a single full-screen Canvas to avoid
    // extreme overdraw from multiple full-screen graphicsLayers.
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = canvasScale,
                scaleY = canvasScale,
                translationX = canvasOffset.x,
                translationY = canvasOffset.y
            )
    ) {
        // Shader pooling to avoid "Uniform Overwrite" bug during the single-pass draw loop.
        // Traces are drawn sequentially.
        traces.forEach { (studentId, points) ->
            if (points.size < 2) return@forEach

            // key(studentId) equivalent in the draw loop isn't needed as we are in the draw phase,
            // but we use student-specific shader instances for state isolation if required.
            // However, since we are drawing sequentially in one Canvas pass, we can re-use
            // a single shader if we update uniforms before each draw call.

            val shader = RuntimeShader(GhostTraceShader.TRACE_PATH)

            // Prepare uniforms
            val pointArray = FloatArray(minOf(points.size, 100) * 2)
            val ageArray = FloatArray(minOf(points.size, 100))
            val maxId = points.last().timestamp.toFloat()
            val minId = points.first().timestamp.toFloat()

            for (i in 0 until minOf(points.size, 100)) {
                pointArray[i * 2] = points[i].position.x
                pointArray[i * 2 + 1] = points[i].position.y
                ageArray[i] = if (maxId == minId) 1.0f else (points[i].timestamp - minId) / (maxId - minId)
            }

            shader.setFloatUniform("iResolution", 4000f, 4000f)
            shader.setFloatUniform("iTime", time)
            shader.setFloatUniform("iPointCount", minOf(points.size, 100).toFloat())
            shader.setFloatUniform("iPoints", pointArray)
            shader.setFloatUniform("iAges", ageArray)

            drawRect(
                brush = ShaderBrush(shader),
                size = androidx.compose.ui.geometry.Size(4000f, 4000f)
            )
        }
    }
}
