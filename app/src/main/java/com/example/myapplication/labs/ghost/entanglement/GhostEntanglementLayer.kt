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
 * This layer uses the [GhostEntanglementShader] to visualize pulsating
 * connections between students who exhibit high social synchronicity.
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
                id = student.id,
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

    Canvas(modifier = modifier.fillMaxSize()) {
        entangledPairs.forEach { (studentA, studentB) ->
            val shader = RuntimeShader(GhostEntanglementShader.QUANTUM_RIPPLES)
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

            drawRect(brush = ShaderBrush(shader))
        }
    }
}
