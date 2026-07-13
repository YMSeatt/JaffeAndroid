package com.example.myapplication.labs.ghost.prism

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import com.example.myapplication.labs.ghost.GhostConfig

/**
 * GhostPrismLayer: Renders a shader-backed background for a student icon based on their vibe.
 *
 * This component uses API 33+ [RuntimeShader] to create dynamic, data-driven
 * aesthetic themes.
 *
 * @param vibe The current vibe of the student.
 * @param modifier Modifier for the layer.
 */
@Composable
fun GhostPrismLayer(
    vibe: GhostPrismEngine.Vibe,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val infiniteTransition = rememberInfiniteTransition(label = "prismTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 62.8f, // 10 cycles
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val shader = remember { RuntimeShader(GhostPrismShader.PRISM_BACKGROUND) }
    val brush = remember(shader) { ShaderBrush(shader) }

    Canvas(modifier = modifier.fillMaxSize()) {
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        shader.setIntUniform("iVibe", vibe.ordinal)

        drawRect(brush = brush)
    }
}
