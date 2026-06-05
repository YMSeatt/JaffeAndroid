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
    val time by infiniteTransition.animateFloat(
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
