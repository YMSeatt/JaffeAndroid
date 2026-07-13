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
 * GhostTectonicLayer: Renders the social stability visualization using an AGSL background.
 *
 * This layer bridges the logical analysis from [GhostTectonicEngine] with the
 * procedural rendering in [GhostTectonicShader]. It visualizes localized "Social Stress"
 * as a dynamic, magma-like field beneath the seating chart icons.
 *
 * ### Performance Optimizations (BOLT):
 * 1. **Array Pooling**: Reuses a `FloatArray` (`nodeData`) for uniform passing to the shader
 *    to avoid garbage collection pressure during frequent recompositions.
 * 2. **Logical Mapping**: Efficiently translates 4000x4000 world-space coordinates into
 *    screen-space coordinates before passing them to the GPU.
 * 3. **GPU Constraints**: Limits the visualization to the top 20 students to stay within
 *    standard AGSL uniform array limits.
 *
 * @param students Current students to track for tectonic stress; used to drive position uniforms.
 * @param behaviorLogs Historical logs used to calculate individual stress levels.
 * @param canvasScale The current zoom level (0.1f to 5.0f) used for coordinate mapping.
 * @param canvasOffset The current pan offset (in pixels) for coordinate translation.
 * @param isActive A toggle to enable/disable the layer's rendering pass.
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
        // BOLT: Pre-allocate and reuse the node data array to avoid per-frame allocations.
        val nodeData = remember { FloatArray(20 * 3) }

        Canvas(modifier = Modifier.fillMaxSize()) {
            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iTime", time)

            // BOLT: Clear the reused array to avoid stale data artifacts if student count decreases.
            nodeData.fill(0f)

            // Pass up to 20 nodes to the shader (GPU uniform limit)
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
