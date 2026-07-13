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
    val timeState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // BOLT ⚡ Optimization: Pre-allocate a pool of shaders to avoid per-frame allocations
    // while bypassing the "Uniform Overwrite" bug in RenderNode recording.
    val shaderPool = remember {
        List(10) { RuntimeShader(GhostTraceShader.TRACE_PATH) }
    }
    val brushPool = remember(shaderPool) {
        shaderPool.map { ShaderBrush(it) }
    }

    // BOLT ⚡ Optimization: Pre-allocate buffers and transform map to list for zero-allocation iteration.
    val pointBuffer = remember { FloatArray(200) }
    val ageBuffer = remember { FloatArray(100) }
    val traceList = remember(traces) { traces.values.toList() }

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
        // BOLT: Access time value inside the draw block to avoid whole-layer recomposition.
        val time = timeState.value

        val maxTraces = minOf(traceList.size, shaderPool.size)
        for (i in 0 until maxTraces) {
            val points = traceList[i]
            if (points.size < 2) continue

            val shader = shaderPool[i]
            val count = minOf(points.size, 100)
            val maxTs = points.last().timestamp.toFloat()
            val minTs = points.first().timestamp.toFloat()

            // BOLT: Reuse buffers and clear to avoid stale data
            pointBuffer.fill(0f)
            ageBuffer.fill(0f)

            for (j in 0 until count) {
                val pt = points[j]
                pointBuffer[j * 2] = pt.position.x
                pointBuffer[j * 2 + 1] = pt.position.y
                ageBuffer[j] = if (maxTs == minTs) 1.0f else (pt.timestamp - minTs) / (maxTs - minTs)
            }

            shader.setFloatUniform("iResolution", 4000f, 4000f)
            shader.setFloatUniform("iTime", time)
            shader.setFloatUniform("iPointCount", count.toFloat())
            shader.setFloatUniform("iPoints", pointBuffer)
            shader.setFloatUniform("iAges", ageBuffer)

            drawRect(
                brush = brushPool[i],
                size = androidx.compose.ui.geometry.Size(4000f, 4000f)
            )
        }
    }
}
