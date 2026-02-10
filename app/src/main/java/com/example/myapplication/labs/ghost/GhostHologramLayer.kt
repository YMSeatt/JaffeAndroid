package com.example.myapplication.labs.ghost

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer

/**
 * GhostHologramLayer: A Composable that applies a 3D parallax effect and holographic
 * shader overlay to its content.
 *
 * @param engine The [GhostHologramEngine] providing sensor data.
 * @param content The UI content to be "hologramized".
 */
@Composable
fun GhostHologramLayer(
    engine: GhostHologramEngine,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tilt by engine.tilt.collectAsState()

    // Smooth the tilt transitions
    val animatedPitch by animateFloatAsState(
        targetValue = tilt.pitch,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "pitch"
    )
    val animatedRoll by animateFloatAsState(
        targetValue = tilt.roll,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "roll"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "hologramPulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                // Apply 3D rotation based on tilt
                // Note: rotationX is tilt around the horizontal axis (pitch)
                // rotationY is tilt around the vertical axis (roll)
                rotationX = -animatedPitch
                rotationY = animatedRoll
                cameraDistance = 12f * density
            }
            .drawWithCache {
                onDrawWithContent {
                    drawContent()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val shader = RuntimeShader(GhostHologramShader.HOLOGRAM_GLASS)
                        shader.setFloatUniform("iResolution", size.width, size.height)
                        shader.setFloatUniform("iTime", time)
                        shader.setFloatUniform("iTilt", animatedRoll, animatedPitch)
                        shader.setFloatUniform("iColor", 0.0f, 0.8f, 1.0f) // Cyan
                        shader.setFloatUniform("iFlicker", tilt.flicker)

                        drawRect(brush = ShaderBrush(shader))
                    }
                }
            }
    ) {
        content()
    }
}
