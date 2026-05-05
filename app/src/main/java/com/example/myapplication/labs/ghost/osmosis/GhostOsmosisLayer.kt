package com.example.myapplication.labs.ghost.osmosis

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import com.example.myapplication.labs.ghost.GhostConfig

/**
 * GhostOsmosisLayer: Renders the fluid diffusion field on the seating chart.
 * ⚡ BOLT Optimization: Replaced CPU-side grid sampling with a single-pass GPU shader.
 * Eliminates thousands of JNI calls and CPU calculations per frame.
 */
@Composable
fun GhostOsmosisLayer(
    students: List<GhostOsmosisEngine.OsmoticNode>,
    canvasScale: Float,
    canvasOffset: androidx.compose.ui.geometry.Offset,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !GhostConfig.OSMOSIS_MODE_ENABLED) return

    val infiniteTransition = rememberInfiniteTransition(label = "osmosis")
    val timeState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // BOLT: Performance Cap. Sample first 40 students to fit shader uniform array size.
    val activeNodes = remember(students) { students.take(40) }

    // BOLT: Pre-allocate primitive arrays to eliminate per-frame object churn.
    val points = remember { FloatArray(40 * 2) }
    val knowledge = remember { FloatArray(40) }
    val behavior = remember { FloatArray(40) }

    val shader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RuntimeShader(GhostOsmosisShader.DIFFUSION_FIELD)
        } else null
    }
    val brush = remember(shader) { shader?.let { ShaderBrush(it) } }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && shader != null && brush != null) {
            val width = size.width
            val height = size.height
            if (width <= 0f || height <= 0f) return@Canvas

            // BOLT: Clear and populate uniform arrays from active nodes.
            points.fill(0f)
            knowledge.fill(0f)
            behavior.fill(0f)

            val count = activeNodes.size
            for (i in 0 until count) {
                val node = activeNodes[i]
                // Map 4000x4000 logical coordinates to screen space
                points[i * 2] = node.x * canvasScale + canvasOffset.x
                points[i * 2 + 1] = node.y * canvasScale + canvasOffset.y
                knowledge[i] = node.knowledgePotential
                behavior[i] = node.behaviorConcentration
            }

            // BOLT: High-performance uniform updates.
            // setFloatUniform with arrays is much faster than individual calls.
            shader.setFloatUniform("iResolution", width, height)
            shader.setFloatUniform("iTime", timeState.value) // Reading time only inside draw block
            shader.setFloatUniform("iPoints", points)
            shader.setFloatUniform("iKnowledge", knowledge)
            shader.setFloatUniform("iBehavior", behavior)
            shader.setIntUniform("iNumPoints", count)

            // BOLT: Single draw call for the entire field.
            drawRect(brush = brush)
        }
    }
}
