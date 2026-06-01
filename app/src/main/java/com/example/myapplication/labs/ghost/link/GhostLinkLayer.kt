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
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp

/**
 * GhostLinkLayer: Renders proximity-based neural strands between students.
 *
 * This layer consumes pre-calculated [GhostLinkEngine.NeuralLink] data and renders
 * them using the [GhostLinkShader]. It is BOLT-optimized to minimize allocations
 * during high-frequency drawing and correctly accounts for canvas transformation state.
 */
@Composable
fun GhostLinkLayer(
    links: List<GhostLinkEngine.NeuralLink>,
    canvasScale: Float,
    canvasOffset: Offset,
    isVisible: Boolean
) {
    if (!isVisible || links.isEmpty()) return

    val infiniteTransition = rememberInfiniteTransition(label = "ghost_link_time")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shaderPool = remember { mutableMapOf<String, RuntimeShader>() }

        Canvas(modifier = Modifier.fillMaxSize()) {
            withTransform({
                translate(canvasOffset.x, canvasOffset.y)
                scale(canvasScale, canvasScale, pivot = Offset.Zero)
            }) {
                links.forEach { link ->
                    // Stable key for shader pooling based on student IDs
                    val key = "${link.studentA}_${link.studentB}"
                    val shader = shaderPool.getOrPut(key) { RuntimeShader(GhostLinkShader.NEURAL_STRAND) }

                    shader.setFloatUniform("iResolution", size.width, size.height)
                    shader.setFloatUniform("iTime", time)
                    shader.setFloatUniform("iPointA", link.ax, link.ay)
                    shader.setFloatUniform("iPointB", link.bx, link.by)
                    shader.setFloatUniform("iStrength", link.synergy)

                    val brush = ShaderBrush(shader)

                    // Draw the link. Use a rectangle covering the bounds of the two points
                    // plus padding (scaled) to allow for the shader's glow/warp.
                    val padding = 50f
                    val minX = minOf(link.ax, link.bx) - padding
                    val minY = minOf(link.ay, link.by) - padding
                    val maxX = maxOf(link.ax, link.bx) + padding
                    val maxY = maxOf(link.ay, link.by) + padding

                    drawRect(
                        brush = brush,
                        topLeft = Offset(minX, minY),
                        size = androidx.compose.ui.geometry.Size(maxX - minX, maxY - minY)
                    )
                }
            }
        }
    }
}
