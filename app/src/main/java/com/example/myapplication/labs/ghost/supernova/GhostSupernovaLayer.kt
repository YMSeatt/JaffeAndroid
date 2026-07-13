package com.example.myapplication.labs.ghost.supernova

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import com.example.myapplication.labs.ghost.supernova.GhostSupernovaEngine.SupernovaStage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * GhostSupernovaLayer: A high-performance AGSL-driven visualization layer.
 *
 * This layer renders the four stages of a "Classroom Supernova" using procedural shaders.
 * It acts as the bridge between the [GhostSupernovaEngine]'s logical state and the
 * GPU-accelerated drawing commands.
 *
 * ### Architectural Design:
 * - **State-to-Animation Orchestration**: Uses Compose `LaunchedEffect` and `Animatable`
 *   to drive the temporal uniforms (`iProgress`, `iTime`) of the AGSL shaders.
 * - **Conditional Rendering**: Swaps between different AGSL programs ([GhostSupernovaShader.CORE_PRESSURE]
 *   and [GhostSupernovaShader.SUPERNOVA_EXPLOSION]) based on the current lifecycle [SupernovaStage].
 * - **Hardware Acceleration**: Leverages `graphicsLayer` transformations for global UI
 *   effects like the "Explosion Screen Shake."
 */
@Composable
fun GhostSupernovaLayer(
    engine: GhostSupernovaEngine,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    // AGSL RuntimeShader requires Android 13 (API 33) or higher.
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val stage by engine.stage.collectAsState()
    val pressure by engine.pressure.collectAsState()

    // Drives the progress of the current stage animation (0.0 to 1.0)
    val progress = remember { Animatable(0f) }

    // Continuous time clock for procedural noise in the shaders
    val time = remember { Animatable(0f) }

    // 🕒 Infinite Clock Animation
    LaunchedEffect(Unit) {
        time.animateTo(
            targetValue = 10000f,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = tween(10000, easing = LinearEasing)
            )
        )
    }

    // 🎬 Stage-Based Animation Sequencing
    // When the engine's stage changes, this effect executes the corresponding animation timeline.
    LaunchedEffect(stage) {
        when (stage) {
            SupernovaStage.CONTRACTION -> {
                progress.snapTo(0f)
                // 1.5s blue-shifted implosion
                progress.animateTo(1.0f, tween(1500, easing = LinearEasing))
                engine.nextStage()
            }
            SupernovaStage.EXPLOSION -> {
                progress.snapTo(0f)
                // 1.0s magenta shockwave
                progress.animateTo(1.0f, tween(1000, easing = LinearEasing))
                engine.nextStage()
            }
            SupernovaStage.NEBULA -> {
                progress.snapTo(0f)
                // 5.0s cooling gas transition
                progress.animateTo(1.0f, tween(5000, easing = LinearEasing))
                engine.nextStage()
            }
            SupernovaStage.IDLE -> {
                progress.snapTo(0f)
            }
        }
    }

    // 🌡️ PHASE 1: Core Pressure Layer
    // Renders the heatmap distortion and core glow while in the monitoring (IDLE) state.
    if (stage == SupernovaStage.IDLE) {
        val pressureShader = remember { RuntimeShader(GhostSupernovaShader.CORE_PRESSURE) }
        Canvas(modifier = modifier.fillMaxSize()) {
            pressureShader.setFloatUniform("iResolution", size.width, size.height)
            pressureShader.setFloatUniform("iTime", time.value)
            pressureShader.setFloatUniform("iPressure", pressure)
            pressureShader.setFloatUniform("iColor", 0.0f, 0.8f, 1.0f) // Cyan theme

            drawRect(brush = ShaderBrush(pressureShader))
        }
    }

    // 💥 PHASE 2: High-Energy Sequence Layer
    // Renders the Contraction, Explosion, and Nebula effects using a specialized multi-stage shader.
    if (stage != SupernovaStage.IDLE) {
        val supernovaShader = remember { RuntimeShader(GhostSupernovaShader.SUPERNOVA_EXPLOSION) }
        Canvas(modifier = modifier.fillMaxSize().graphicsLayer {
            /**
             * 🫨 Screen Shake Logic:
             * During the EXPLOSION phase, we apply procedural translation jitter to the entire
             * layer. The intensity of the shake decays as the explosion progress approaches 1.0.
             */
            if (stage == SupernovaStage.EXPLOSION) {
                val shakeIntensity = (1.0f - progress.value) * 10f
                translationX = (Math.random().toFloat() * 2f - 1f) * shakeIntensity
                translationY = (Math.random().toFloat() * 2f - 1f) * shakeIntensity
            }
        }) {
            supernovaShader.setFloatUniform("iResolution", size.width, size.height)
            supernovaShader.setFloatUniform("iTime", time.value)
            supernovaShader.setFloatUniform("iProgress", progress.value)
            supernovaShader.setIntUniform("iStage", when (stage) {
                SupernovaStage.CONTRACTION -> 1
                SupernovaStage.EXPLOSION -> 2
                SupernovaStage.NEBULA -> 3
                else -> 0
            })
            supernovaShader.setFloatUniform("iColor", 1.0f, 0.2f, 0.8f) // Magenta theme

            drawRect(brush = ShaderBrush(supernovaShader))
        }
    }
}
