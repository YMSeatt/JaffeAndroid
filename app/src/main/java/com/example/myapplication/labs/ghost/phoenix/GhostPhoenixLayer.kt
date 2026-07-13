package com.example.myapplication.labs.ghost.phoenix

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
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostPhoenixLayer: Renders "Phoenix Rising" effects for resilient students.
 *
 * This layer identifies students with high resilience scores (via [GhostPhoenixEngine])
 * and renders an AGSL-powered fire aura and ember effect around their icons.
 *
 * ### BOLT ⚡ Optimization:
 * - **Shader Pooling**: To avoid the "Uniform Overwrite" bug (where multiple draw calls sharing the
 *   same native shader object overwrite each other's uniforms before the GPU executes), this layer
 *   maintains a `shaderPool`. Each active Phoenix is assigned its own [RuntimeShader] instance.
 * - **Single-Pass Rendering**: Iterates through students once per frame in the [Canvas] block,
 *   drawing only those who exceed the [GhostPhoenixEngine.PHOENIX_THRESHOLD].
 * - **Zero-Allocation Coordinates**: Student positions are mapped from the logical 4000x4000
 *   canvas to screen pixels just-in-time, avoiding per-frame [Offset] object churn.
 */
@Composable
fun GhostPhoenixLayer(
    students: List<StudentUiItem>,
    resilienceScores: Map<Long, Float>,
    canvasScale: Float,
    canvasOffset: Offset,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || !isVisible) return

    val infiniteTransition = rememberInfiniteTransition(label = "phoenixAnimation")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // BOLT: Shader pool to prevent uniform overwrites across multiple students
    // We pre-calculate the required pool size to avoid state mutations during draw.
    val activePhoenixCount = remember(resilienceScores) {
        resilienceScores.values.count { it >= GhostPhoenixEngine.PHOENIX_THRESHOLD }
    }
    val shaderPool = remember(activePhoenixCount) {
        List(activePhoenixCount) { RuntimeShader(GhostPhoenixShader.PHOENIX_RISING) }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                // The layer itself doesn't move, we position individual elements inside
            }
    ) {
        var effectCount = 0

        for (i in 0 until students.size) {
            val student = students[i]
            val score = resilienceScores[student.id.toLong()] ?: 0f

            if (score >= GhostPhoenixEngine.PHOENIX_THRESHOLD && effectCount < shaderPool.size) {
                val shader = shaderPool[effectCount]
                val brush = ShaderBrush(shader)

                val sizePx = 200f * canvasScale // Logical radius 100f
                val sx = student.xPosition.value * canvasScale + canvasOffset.x
                val sy = student.yPosition.value * canvasScale + canvasOffset.y

                shader.setFloatUniform("iResolution", sizePx, sizePx)
                shader.setFloatUniform("iTime", time)
                shader.setFloatUniform("iResilience", score)
                shader.setFloatUniform("iColor", 1.0f, 0.4f, 0.0f) // Phoenix Orange

                drawCircle(
                    brush = brush,
                    radius = sizePx / 2f,
                    center = Offset(sx, sy)
                )

                effectCount++
            }
        }
    }
}
