package com.example.myapplication.labs.ghost.link

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush
import com.example.myapplication.labs.ghost.GhostConfig
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostLinkLayer: Visualizes the "Neural Links" between high-synergy student pairs.
 *
 * BOLT: Optimized with shader pooling and zero-allocation draw loops.
 */
@Composable
fun GhostLinkLayer(
    students: List<StudentUiItem>,
    neuralLinks: List<GhostLinkEngine.NeuralLink>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !isActive) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val infiniteTransition = rememberInfiniteTransition(label = "linkTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(50000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // BOLT: Shader pool for top 10 links
    val maxLinks = 10
    val shaderPool = remember {
        List(maxLinks) { RuntimeShader(GhostLinkShader.NEURAL_STRAND) }
    }
    val brushPool = remember(shaderPool) {
        shaderPool.map { ShaderBrush(it) }
    }
    val studentMap = remember(students) { students.associateBy { it.id.toLong() } }

    Canvas(modifier = modifier.fillMaxSize()) {
        val numToDraw = neuralLinks.size.coerceAtMost(maxLinks)
        for (index in 0 until numToDraw) {
            val link = neuralLinks[index]
            val sA = studentMap[link.studentA]
            val sB = studentMap[link.studentB]
            if (sA == null || sB == null) continue

            val shader = shaderPool[index]
            val brush = brushPool[index]

            // Calculate center positions on the logical 4000x4000 canvas mapped to screen pixels
            val posAx = (sA.xPosition.value * canvasScale) + canvasOffset.x + (sA.displayWidth.value.toPx() * canvasScale / 2f)
            val posAy = (sA.yPosition.value * canvasScale) + canvasOffset.y + (sA.displayHeight.value.toPx() * canvasScale / 2f)
            val posBx = (sB.xPosition.value * canvasScale) + canvasOffset.x + (sB.displayWidth.value.toPx() * canvasScale / 2f)
            val posBy = (sB.yPosition.value * canvasScale) + canvasOffset.y + (sB.displayHeight.value.toPx() * canvasScale / 2f)

            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iTime", time)
            shader.setFloatUniform("iPosA", posAx, posAy)
            shader.setFloatUniform("iPosB", posBx, posBy)
            shader.setFloatUniform("iStrength", link.strength)
            shader.setFloatUniform("iColor", 0.7f, 0.2f, 1.0f) // Electric Purple Neural Link

            drawRect(brush = brush)
        }
    }
}
