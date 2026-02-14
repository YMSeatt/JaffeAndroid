package com.example.myapplication.labs.ghost

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush

/**
 * GhostEchoLayer: A real-time auditory atmosphere visualization layer.
 *
 * This component acts as a "Classroom Pulse Monitor," using a procedural AGSL noise shader
 * (ACOUSTIC_TURBULENCE) to transform microphone amplitude data into ambient visual energy.
 *
 * **Visual Mechanics:**
 * - **Auditory Turbulence**: Noise levels (volume) drive the 'iAmplitude' uniform of the shader,
 *   which increases the distortion and velocity of the procedural waves.
 * - **Atmospheric Calm**: In a quiet classroom, the layer settles into a stable, low-frequency
 *   ambient flow.
 *
 * @param engine The [GhostEchoEngine] responsible for processing microphone input and providing
 *   a reactive [StateFlow] of normalized auditory amplitude (0.0 to 1.0).
 * @param modifier Standard Compose [Modifier] for layout configuration.
 */
@Composable
fun GhostEchoLayer(
    engine: GhostEchoEngine,
    modifier: Modifier = Modifier
) {
    // AGSL RuntimeShader requires Android 13+ (API 33)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val amplitude by engine.amplitude.collectAsState()

    // Smoothly animate the time uniform for continuous procedural motion
    val infiniteTransition = rememberInfiniteTransition(label = "echoPulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val shader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RuntimeShader(GhostEchoShader.ACOUSTIC_TURBULENCE)
        } else {
            null
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (shader != null) {
            try {
                shader.setFloatUniform("iResolution", size.width, size.height)
                shader.setFloatUniform("iTime", time)
                shader.setFloatUniform("iAmplitude", amplitude)

                drawRect(brush = ShaderBrush(shader))
            } catch (e: Exception) {
                // Fallback: draw nothing if shader fails to compile or run
            }
        }
    }
}
