package com.example.myapplication.labs.ghost.tectonics

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.labs.ghost.GhostConfig
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.min

/**
 * GhostTectonicLayer: Renders the social stability visualization.
 *
 * This layer uses the [GhostTectonicEngine] to calculate stress nodes and the
 * [GhostTectonicShader] to render a procedural background reflecting classroom
 * "Social Stress".
 *
 * @param students Current students to track for tectonic stress.
 * @param behaviorLogs Historical logs used to drive stress accumulation.
 * @param canvasScale The current zoom level of the seating chart.
 * @param canvasOffset The current pan position of the seating chart.
 * @param isActive Whether the layer is currently enabled in the UI.
 */
@Composable
fun GhostTectonicLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !GhostConfig.TECTONICS_MODE_ENABLED || !isActive) return

    val infiniteTransition = rememberInfiniteTransition(label = "tectonicPulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { RuntimeShader(GhostTectonicShader.SOCIAL_TECTONICS) }
        val brush = remember(shader) { ShaderBrush(shader) }

        Canvas(modifier = Modifier.fillMaxSize()) {
            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iTime", time)

            // Pass up to 20 nodes to the shader (GPU uniform limit)
            val nodeData = FloatArray(20 * 3)
            val count = min(students.size, 20)

            for (i in 0 until count) {
                val student = students[i]
                // Map logical world coordinates to screen coordinates
                val screenX = (student.xPosition.value * canvasScale) + canvasOffset.x
                val screenY = (student.yPosition.value * canvasScale) + canvasOffset.y

                nodeData[i * 3] = screenX
                nodeData[i * 3 + 1] = screenY
                nodeData[i * 3 + 2] = student.tectonicStress.value
            }

            shader.setFloatUniform("iNodes", nodeData)
            shader.setIntUniform("iNodeCount", count)

            drawRect(brush = brush)
        }
    }
}
