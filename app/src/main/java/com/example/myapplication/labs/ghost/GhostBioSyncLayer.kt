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

/**
 * GhostBioSyncLayer: A high-performance atmospheric layer for biological state visualization.
 *
 * This layer renders the [GhostBioSyncShader] and updates its uniforms based on
 * real-time vitality metrics and classroom harmony.
 */
@Composable
fun GhostBioSyncLayer(
    vitalityPoints: List<BioSyncPoint>,
    harmony: Float,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val infiniteTransition = rememberInfiniteTransition(label = "biosync_time")
    val timeState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val shader = remember { RuntimeShader(GhostBioSyncShader.BIOSYNC_PULSE) }
    val brush = remember(shader) { ShaderBrush(shader) }

    // BOLT: Hoist point buffer to avoid per-frame allocation
    val pointBuffer = remember { FloatArray(160) } // 40 points * 4 components

    Canvas(modifier = Modifier.fillMaxSize()) {
        val time = timeState.value
        // Clear buffer
        for (i in pointBuffer.indices) pointBuffer[i] = 0f

        val maxPoints = 40
        val count = vitalityPoints.size.coerceAtMost(maxPoints)

        // Stage 1: Update point buffer with transformed student coordinates
        for (i in 0 until count) {
            val point = vitalityPoints[i]
            val idx = i * 4

            // Map logical coordinate (4000x4000) to screen pixels
            val screenX = point.x * canvasScale + canvasOffset.x
            val screenY = point.y * canvasScale + canvasOffset.y

            pointBuffer[idx] = screenX
            pointBuffer[idx + 1] = screenY
            pointBuffer[idx + 2] = point.vitality
            pointBuffer[idx + 3] = point.stress
        }

        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        shader.setFloatUniform("iHarmony", harmony)
        shader.setFloatUniform("iPointCount", count.toFloat())
        shader.setFloatUniform("iVitalityPoints", pointBuffer)

        drawRect(brush = brush)
    }
}

/**
 * Data class representing a student's biological data point for the shader.
 */
data class BioSyncPoint(
    val x: Float,
    val y: Float,
    val vitality: Float,
    val stress: Float
)
