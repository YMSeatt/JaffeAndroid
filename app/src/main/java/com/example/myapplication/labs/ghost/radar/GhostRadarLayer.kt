package com.example.myapplication.labs.ghost.radar

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostRadarLayer: Renders a localized behavioral radar.
 *
 * This layer uses an AGSL shader to visualize recent behavioral activity around
 * a specific target coordinate. It is rendered as a circular overlay that
 * tracks with the seating chart's pan and zoom states.
 *
 * ### BOLT Optimization:
 * - **Shader State Hoisting**: Uses `remember` to pre-allocate the [RuntimeShader]
 *   and [ShaderBrush], avoiding per-frame object churn during the radar rotation.
 * - **GraphicsLayer Isolation**: Deploys the radar within a `graphicsLayer` block
 *   to minimize recomposition overhead during panning and zooming.
 *
 * @param targetOffset The logical (4000x4000) center of the radar.
 * @param intensity The calculated resonance from [GhostRadarEngine] (0.0 to 1.0).
 * @param canvasScale The current zoom level of the seating chart.
 * @param canvasOffset The current pan offset of the seating chart.
 * @param isVisible Controls whether the radar overlay is rendered.
 */
@Composable
fun GhostRadarLayer(
    targetOffset: Offset,
    intensity: Float,
    canvasScale: Float,
    canvasOffset: Offset,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || !isVisible) return

    val infiniteTransition = rememberInfiniteTransition(label = "radarRotation")
    val timeState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val shader = remember { RuntimeShader(GhostRadarShader.RADAR_SWEEP) }
    val brush = remember(shader) { ShaderBrush(shader) }

    // The radar is 1000f in logical space (radius 500f)
    val radarSizePx = 1000f * canvasScale

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                // Position the radar center on the target, accounting for canvas pan/zoom
                translationX = targetOffset.x * canvasScale + canvasOffset.x - (radarSizePx / 2f)
                translationY = targetOffset.y * canvasScale + canvasOffset.y - (radarSizePx / 2f)
            }
            .size((1000.dp / 1.0f)) // Placeholder size, actual rendering controlled by graphicsLayer & drawRect
    ) {
        val time = timeState.value
        shader.setFloatUniform("iResolution", radarSizePx, radarSizePx)
        shader.setFloatUniform("iTime", time)
        shader.setFloatUniform("iIntensity", intensity)
        shader.setFloatUniform("iColor", 0.0f, 1.0f, 0.8f) // Cyber-Cyan

        drawRect(
            brush = brush,
            size = androidx.compose.ui.geometry.Size(radarSizePx, radarSizePx)
        )
    }
}
