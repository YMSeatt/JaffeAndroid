package com.example.myapplication.labs.ghost.glitch

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush

/**
 * GhostGlitchLayer: Renders the neural feedback glitch effect.
 *
 * This layer sits above the seating chart and becomes visible when
 * spatial conflicts (overlaps) are detected.
 */
@Composable
fun GhostGlitchLayer(
    intensity: Float,
    isActive: Boolean
) {
    if (!isActive || intensity <= 0.01f) return

    val infiniteTransition = rememberInfiniteTransition(label = "glitchTime")
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
        val shader = remember { RuntimeShader(GhostGlitchShader.NEURAL_GLITCH) }

        Canvas(modifier = Modifier.fillMaxSize()) {
            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iTime", time)
            shader.setFloatUniform("iIntensity", intensity)

            drawRect(brush = ShaderBrush(shader))
        }
    }
}
