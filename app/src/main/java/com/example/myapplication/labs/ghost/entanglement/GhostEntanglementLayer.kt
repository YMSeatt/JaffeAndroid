package com.example.myapplication.labs.ghost.entanglement

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.myapplication.labs.ghost.GhostConfig
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostEntanglementLayer: Renders the interactive "Quantum Social Sync" overlay.
 *
 * This component visualizes the "spooky action at a distance" between students who
 * exhibit high social coherence. It utilizes high-performance AGSL shaders to render
 * interfering ripples and glowing connectivity bridges.
 *
 * ### Rendering Logic:
 * 1. **Coherence Analysis**: Identifies the top 3 most coherent student pairs in real-time.
 * 2. **Coordinate Normalization**: Translates 4000x4000 logical seating chart units into
 *    screen pixel coordinates for the shader.
 * 3. **Shader Execution**: For each pair, it triggers the [GhostEntanglementShader.QUANTUM_RIPPLES]
 *    effect, passing coherence and time-based uniforms.
 *
 * ### BOLT ⚡ Optimization:
 * To maintain 60fps interaction (especially during student dragging), this layer uses
 * a **Shader & Brush Pool**. By pre-allocating and reusing `RuntimeShader` instances,
 * it avoids expensive JNI calls and object allocations within the high-frequency [Canvas]
 * draw loop.
 *
 * @param students The current list of students on the chart.
 * @param isEntanglementActive Whether the visual overlay is enabled.
 */
@Composable
fun GhostEntanglementLayer(
    students: List<StudentUiItem>,
    isEntanglementActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !isEntanglementActive) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenWidth = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { config.screenHeightDp.dp.toPx() }

    // Animate the quantum time
    val infiniteTransition = rememberInfiniteTransition(label = "quantumTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Identify Entangled Pairs (Simplified for PoC: Top X most coherent pairs)
    val entangledPairs = remember(students, time) {
        val pairs = mutableListOf<Pair<StudentUiItem, StudentUiItem>>()
        if (students.size < 2) return@remember pairs

        // Convert StudentUiItem to EntangledNode for calculation
        val nodes = students.map { student ->
            GhostEntanglementEngine.EntangledNode(
                id = student.id.toLong(),
                x = student.xPosition.value,
                y = student.yPosition.value,
                behaviorSync = 0.7f, // Mock: In real app, calculate from logs
                academicParity = 0.8f // Mock: In real app, calculate from logs
            )
        }

        // Find pairs with high coherence
        for (i in nodes.indices) {
            for (j in i + 1 until nodes.size) {
                val nodeA = nodes[i]
                val nodeB = nodes[j]
                val coherence = GhostEntanglementEngine.calculateCoherence(nodeA, nodeB)
                if (coherence > 0.8f) {
                    pairs.add(students[i] to students[j])
                }
            }
        }
        pairs.take(3) // Limit to top 3 pairs for visual clarity
    }

    // BOLT: Pool shaders and brushes to avoid O(P) allocations in the draw loop
    // while ensuring unique instances for correct uniform capturing per draw call.
    val entanglementShaderPool = remember { mutableListOf<RuntimeShader>() }
    val entanglementBrushPool = remember { mutableListOf<ShaderBrush>() }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            var pairIdx = 0
            entangledPairs.forEach { (studentA, studentB) ->
                if (pairIdx >= entanglementShaderPool.size) {
                    val s = RuntimeShader(GhostEntanglementShader.QUANTUM_RIPPLES)
                    entanglementShaderPool.add(s)
                    entanglementBrushPool.add(ShaderBrush(s))
                }
                val shader = entanglementShaderPool[pairIdx]
                val brush = entanglementBrushPool[pairIdx]
                pairIdx++

                shader.setFloatUniform("iResolution", screenWidth, screenHeight)
                shader.setFloatUniform("iTime", time)

                // Map logical 4000x4000 coordinates to screen pixels (Simplified)
                // In a real integration, use canvasScale and canvasOffset from ViewModel
                val scaleX = screenWidth / 4000f
                val scaleY = screenHeight / 4000f

                val posA_x = studentA.xPosition.value * scaleX
                val posA_y = studentA.yPosition.value * scaleY
                val posB_x = studentB.xPosition.value * scaleX
                val posB_y = studentB.yPosition.value * scaleY

                shader.setFloatUniform("iEntangledPosA", posA_x, posA_y)
                shader.setFloatUniform("iEntangledPosB", posB_x, posB_y)
                shader.setFloatUniform("iCoherence", 0.9f)

                // Entanglement Color: Cyan for positive-leaning synchronicity
                shader.setFloatUniform("iColor", 0.0f, 1.0f, 1.0f)

                drawRect(brush = brush)
            }
        }
    }
}
