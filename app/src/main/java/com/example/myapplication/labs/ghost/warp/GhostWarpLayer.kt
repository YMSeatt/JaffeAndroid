package com.example.myapplication.labs.ghost.warp

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.labs.ghost.GhostConfig
import com.example.myapplication.ui.model.StudentUiItem
import androidx.compose.ui.geometry.Offset

/**
 * GhostWarpLayer: Applies the "Neural Spacetime Dilation" AGSL effect to the background.
 *
 * @param students Current list of students.
 * @param behaviorLogs Current list of behavior logs.
 * @param canvasScale The current zoom level of the seating chart.
 * @param canvasOffset The current pan offset of the seating chart.
 */
@Composable
fun GhostWarpLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    canvasScale: Float,
    canvasOffset: Offset,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !GhostConfig.WARP_MODE_ENABLED) return

    val infiniteTransition = rememberInfiniteTransition(label = "warpPulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Calculate gravity points based on data
    val gravityPoints = remember(students, behaviorLogs) {
        GhostWarpEngine.calculateGravityPoints(students, behaviorLogs)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { RuntimeShader(GhostWarpShader.NEURAL_WARP) }

        Canvas(modifier = modifier.fillMaxSize()) {
            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iTime", time)
            shader.setFloatUniform("iIntensity", 1.0f)

            // Convert logical coordinates to screen coordinates
            val pointCount = gravityPoints.size.coerceAtMost(10)
            val pointsArray = FloatArray(10 * 4) // float4 array

            for (i in 0 until 10) {
                if (i < pointCount) {
                    val p = gravityPoints[i]
                    // Map logical (4000x4000) to screen space
                    val screenX = p.x * canvasScale + canvasOffset.x
                    val screenY = p.y * canvasScale + canvasOffset.y

                    pointsArray[i * 4 + 0] = screenX
                    pointsArray[i * 4 + 1] = screenY
                    pointsArray[i * 4 + 2] = p.mass
                    pointsArray[i * 4 + 3] = p.radius * canvasScale
                } else {
                    pointsArray[i * 4 + 0] = 0f
                    pointsArray[i * 4 + 1] = 0f
                    pointsArray[i * 4 + 2] = 0f
                    pointsArray[i * 4 + 3] = 0f
                }
            }

            shader.setFloatUniform("iPoints", pointsArray)
            shader.setIntUniform("iPointCount", pointCount)

            drawRect(brush = ShaderBrush(shader))
        }
    }
}
