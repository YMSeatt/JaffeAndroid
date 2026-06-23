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
 * them using the [GhostLinkShader]. It is BOLT-optimized to maintain 60fps even
 * with dozens of active links.
 *
 * ### Performance Optimizations
 * - **Shader Pooling**: To avoid the "Uniform Overwrite" bug where multiple draw calls
 *   sharing the same `RuntimeShader` instance overwrite each other's uniforms before
 *   the GPU executes, this layer maintains a `shaderPool` keyed by student IDs.
 * - **Transformation Awareness**: Correctlies applies `canvasScale` and `canvasOffset`
 *   to ensure strands remain anchored to student icons during pan/zoom.
 * - **Bounds Clipping**: Draws each link within a calculated `drawRect` covering the
 *   two connected nodes, minimizing GPU overdraw compared to a full-screen canvas.
 *
 * @param links The list of active neural links to render.
 * @param canvasScale The current zoom level of the seating chart.
 * @param canvasOffset The current pan offset of the seating chart.
 * @param isVisible Whether the layer is currently enabled.
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
        // BOLT: Pre-allocate a pool of shaders and brushes to avoid O(N) allocations per frame
        // and prevent the "Uniform Overwrite" bug.
        val maxLinks = 20
        val shaderPool = remember {
            List(maxLinks) { RuntimeShader(GhostLinkShader.NEURAL_STRAND) }
        }
        val brushPool = remember(shaderPool) {
            shaderPool.map { ShaderBrush(it) }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            withTransform({
                translate(canvasOffset.x, canvasOffset.y)
                scale(canvasScale, canvasScale, pivot = Offset.Zero)
            }) {
                val numToDraw = links.size.coerceAtMost(maxLinks)

                // BOLT: Manual index-based loop to eliminate Iterator churn
                for (i in 0 until numToDraw) {
                    val link = links[i]
                    val shader = shaderPool[i]
                    val brush = brushPool[i]

                    shader.setFloatUniform("iResolution", size.width, size.height)
                    shader.setFloatUniform("iTime", time)
                    shader.setFloatUniform("iPointA", link.ax, link.ay)
                    shader.setFloatUniform("iPointB", link.bx, link.by)
                    shader.setFloatUniform("iStrength", link.synergy)

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
