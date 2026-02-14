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
 * GhostHologramLayer: A high-fidelity UI wrapper that applies a 3D parallax effect and
 * holographic AGSL shader overlay to its child content.
 *
 * **Experimental Features:**
 * - **Motion Parallax**: Uses the device's [Rotation Vector Sensor](https://developer.android.com/develop/sensors-proactive/motion#sensors-motion-rotate)
 *   to apply real-time 3D rotation (pitch/roll) to the Composable's [graphicsLayer].
 * - **Holographic Overlay**: Renders a "Scanning Line" and procedural flicker effect using
 *   the HOLOGRAM_GLASS AGSL shader, simulating a futuristic translucent display.
 *
 * @param engine The [GhostHologramEngine] providing raw and smoothed tilt data from the device sensors.
 * @param modifier Standard Compose [Modifier].
 * @param content The @Composable content to be wrapped in the holographic environment.
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
                // Parallax Calculation:
                // Map the device pitch (X-axis tilt) to rotationX.
                // Map the device roll (Y-axis tilt) to rotationY.
                // Negative pitch is used to align the UI tilt with physical device movement.
                rotationX = -animatedPitch
                rotationY = animatedRoll
                // Increased camera distance reduces perspective distortion (flattening the effect).
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
