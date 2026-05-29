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
 * GhostHorizonLayer: Renders the context-aware "Neural Horizon" background.
 */
@Composable
fun GhostHorizonLayer(
    engine: GhostHorizonEngine,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !isActive) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val lightLevelState = engine.lightLevel.collectAsState()
    val pressureLevelState = engine.pressureLevel.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "horizonTime")
    val timeState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(120000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val shader = remember { RuntimeShader(GhostHorizonShader.NEURAL_HORIZON) }
    val brush = remember(shader) { ShaderBrush(shader) }

    Canvas(modifier = modifier.fillMaxSize()) {
        // BOLT: Access sensor-driven and animation state inside the draw block.
        // We use engine helpers that read the latest value directly from state.
        val time = timeState.value
        val lightLevel = lightLevelState.value
        val pressureLevel = pressureLevelState.value

        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        shader.setFloatUniform("iLight", engine.getAtmosphericFactor())
        shader.setFloatUniform("iPressure", engine.getVerticality())

        drawRect(brush = brush)
    }
}
