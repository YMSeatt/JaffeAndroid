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
 * GhostEchoLayer: A Jetpack Compose background layer that visualizes classroom noise levels.
 *
 * This component acts as a "Classroom Atmosphere Monitor," using a procedural AGSL shader
 * to represent auditory energy as ambient turbulence. It is designed to be placed
 * at the bottom of the seating chart's layer stack.
 *
 * @param engine The [GhostEchoEngine] providing real-time normalized amplitude data.
 * @param modifier Modifier for the layout.
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
