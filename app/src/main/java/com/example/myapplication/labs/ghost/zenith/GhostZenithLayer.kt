package com.example.myapplication.labs.ghost.zenith

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import com.example.myapplication.labs.ghost.GhostConfig
import kotlinx.coroutines.delay

/**
 * GhostZenithLayer: The spatial 3D container for the Ghost Zenith experiment.
 *
 * This component provides:
 * 1. **Neural Sea Background**: An AGSL-driven background that reacts to tilt.
 * 2. **3D Parallax Wrapper**: A function to wrap student icons, applying
 *    dynamic Z-translation and X/Y rotation based on device orientation and
 *    individual student altitude.
 */
@Composable
fun GhostZenithLayer(
    engine: GhostZenithEngine,
    content: @Composable (ZenithScope) -> Unit
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !GhostConfig.ZENITH_MODE_ENABLED) {
        Box(modifier = Modifier.fillMaxSize()) {
            content(object : ZenithScope {
                override fun Modifier.studentElevation(altitude: Float): Modifier = this
            })
        }
        return
    }

    val tiltX by engine.tiltX.collectAsState()
    val tiltY by engine.tiltY.collectAsState()

    var time by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        engine.start()
        while (true) {
            time += 0.016f
            delay(16)
        }
    }

    DisposableEffect(Unit) {
        onDispose { engine.stop() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Neural Sea Background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val shader = remember { RuntimeShader(GhostZenithShader.NEURAL_SEA) }
            val brush = remember { ShaderBrush(shader) }

            Canvas(modifier = Modifier.fillMaxSize()) {
                shader.setFloatUniform("iResolution", size.width, size.height)
                shader.setFloatUniform("iTime", time)
                shader.setFloatUniform("iTilt", tiltX, tiltY)
                shader.setFloatUniform("iBaseColor", 0.1f, 0.4f, 0.6f) // Deep Cyan
                drawRect(brush)
            }
        }

        // 2. Spatial Content
        content(object : ZenithScope {
            override fun Modifier.studentElevation(altitude: Float): Modifier = this.then(
                Modifier.graphicsLayer {
                    // Map altitude to Z-depth (Elevation)
                    val zOffset = altitude * 200f

                    // Apply parallax rotation based on tilt
                    rotationX = -tiltX * 20f
                    rotationY = tiltY * 20f

                    // Apply translation based on tilt and altitude (deeper = more parallax)
                    translationX = tiltY * 100f * (1f - altitude)
                    translationY = tiltX * 100f * (1f - altitude)

                    cameraDistance = 12f * density
                    shadowElevation = altitude * 10f
                }
            )
        })
    }
}

/**
 * ZenithScope: Provides modifiers for applying 3D elevation to elements
 * within a [GhostZenithLayer].
 */
interface ZenithScope {
    /**
     * Applies 3D elevation and parallax effects to a student node.
     *
     * @param altitude The student's calculated altitude (0..1).
     */
    fun Modifier.studentElevation(altitude: Float): Modifier
}
