package com.example.myapplication.labs.ghost.entanglement

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
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
    entangledLinks: List<GhostEntanglementEngine.EntanglementLink>,
    canvasScale: Float,
    canvasOffset: Offset,
    isEntanglementActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !isEntanglementActive) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

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

    // BOLT: Pool shaders and brushes to avoid O(P) allocations in the draw loop.
    val entanglementShaderPool = remember { mutableListOf<RuntimeShader>() }
    val entanglementBrushPool = remember { mutableListOf<ShaderBrush>() }
    val studentMap = remember(students) { students.associateBy { it.id.toLong() } }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            var linkIdx = 0
            entangledLinks.forEach { link ->
                val studentA = studentMap[link.studentA]
                val studentB = studentMap[link.studentB]
                if (studentA == null || studentB == null) return@forEach

                if (linkIdx >= entanglementShaderPool.size) {
                    val s = RuntimeShader(GhostEntanglementShader.QUANTUM_RIPPLES)
                    entanglementShaderPool.add(s)
                    entanglementBrushPool.add(ShaderBrush(s))
                }
                val shader = entanglementShaderPool[linkIdx]
                val brush = entanglementBrushPool[linkIdx]
                linkIdx++

                shader.setFloatUniform("iResolution", size.width, size.height)
                shader.setFloatUniform("iTime", time)

                // Map logical 4000x4000 coordinates to screen pixels using ViewModel scale/offset
                val posA_x = (studentA.xPosition.value * canvasScale) + canvasOffset.x + (studentA.displayWidth.value.toPx() * canvasScale / 2f)
                val posA_y = (studentA.yPosition.value * canvasScale) + canvasOffset.y + (studentA.displayHeight.value.toPx() * canvasScale / 2f)
                val posB_x = (studentB.xPosition.value * canvasScale) + canvasOffset.x + (studentB.displayWidth.value.toPx() * canvasScale / 2f)
                val posB_y = (studentB.yPosition.value * canvasScale) + canvasOffset.y + (studentB.displayHeight.value.toPx() * canvasScale / 2f)

                shader.setFloatUniform("iEntangledPosA", posA_x, posA_y)
                shader.setFloatUniform("iEntangledPosB", posB_x, posB_y)
                shader.setFloatUniform("iCoherence", link.coherence)

                // Entanglement Color: Cyan for positive-leaning synchronicity
                shader.setFloatUniform("iColor", 0.0f, 1.0f, 1.0f)

                drawRect(brush = brush)
            }
        }
    }
}
