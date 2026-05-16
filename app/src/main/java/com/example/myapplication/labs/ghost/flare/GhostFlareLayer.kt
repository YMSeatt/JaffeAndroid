package com.example.myapplication.labs.ghost.flare

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.myapplication.labs.ghost.GhostConfig

/**
 * GhostFlareLayer: Renders high-intensity behavioral flares over the seating chart.
 */
@Composable
fun GhostFlareLayer(
    engine: GhostFlareEngine,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean = GhostConfig.FLARE_MODE_ENABLED
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val infiniteTransition = rememberInfiniteTransition(label = "flare_time")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Update engine frame-by-frame
    var lastTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(time) {
        val now = System.currentTimeMillis()
        val dt = (now - lastTime) / 1000f
        engine.update(dt)
        lastTime = now
    }

    if (engine.flares.isEmpty()) return

    val shader = remember { RuntimeShader(GhostFlareShader.FLARE) }
    val brush = remember(shader) { ShaderBrush(shader) }

    Canvas(modifier = Modifier.fillMaxSize()) {
        withTransform({
            translate(canvasOffset.x, canvasOffset.y)
            scale(canvasScale, canvasScale, Offset.Zero)
        }) {
            for (flare in engine.flares) {
                val color = when (flare.type) {
                    0 -> Color(0xFFFFD700) // Gold
                    else -> Color.Cyan
                }

                shader.setFloatUniform("iResolution", size.width, size.height)
                shader.setFloatUniform("iCenter", flare.x, flare.y)
                shader.setFloatUniform("iIntensity", flare.intensity)
                shader.setFloatUniform("iLife", flare.life)
                shader.setFloatUniform("iTime", time)
                shader.setColorUniform("iColor", color.red, color.green, color.blue, color.alpha)

                drawRect(brush = brush)
            }
        }
    }
}
