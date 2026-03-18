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
 * It consumes the [GhostSupernovaEngine] state and coordinates the AGSL animations.
 */
@Composable
fun GhostSupernovaLayer(
    engine: GhostSupernovaEngine,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val stage by engine.stage.collectAsState()
    val pressure by engine.pressure.collectAsState()
    val progress = remember { Animatable(0f) }
    val time = remember { Animatable(0f) }

    // Base Time Animation (Clock for procedural noise)
    LaunchedEffect(Unit) {
        time.animateTo(
            targetValue = 10000f,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = tween(10000, easing = LinearEasing)
            )
        )
    }

    // Stage Management & Sequence Execution
    LaunchedEffect(stage) {
        when (stage) {
            SupernovaStage.CONTRACTION -> {
                progress.snapTo(0f)
                progress.animateTo(1.0f, tween(1500, easing = LinearEasing))
                engine.nextStage()
            }
            SupernovaStage.EXPLOSION -> {
                progress.snapTo(0f)
                progress.animateTo(1.0f, tween(1000, easing = LinearEasing))
                engine.nextStage()
            }
            SupernovaStage.NEBULA -> {
                progress.snapTo(0f)
                progress.animateTo(1.0f, tween(5000, easing = LinearEasing))
                engine.nextStage()
            }
            SupernovaStage.IDLE -> {
                progress.snapTo(0f)
            }
        }
    }

    // Core Pressure Layer (Always present if active)
    if (stage == SupernovaStage.IDLE) {
        val pressureShader = remember { RuntimeShader(GhostSupernovaShader.CORE_PRESSURE) }
        Canvas(modifier = modifier.fillMaxSize()) {
            pressureShader.setFloatUniform("iResolution", size.width, size.height)
            pressureShader.setFloatUniform("iTime", time.value)
            pressureShader.setFloatUniform("iPressure", pressure)
            pressureShader.setFloatUniform("iColor", 0.0f, 0.8f, 1.0f) // Cyan

            drawRect(brush = ShaderBrush(pressureShader))
        }
    }

    // High-Energy Stage Layer
    if (stage != SupernovaStage.IDLE) {
        val supernovaShader = remember { RuntimeShader(GhostSupernovaShader.SUPERNOVA_EXPLOSION) }
        Canvas(modifier = modifier.fillMaxSize().graphicsLayer {
            // Add a subtle screen shake during explosion
            if (stage == SupernovaStage.EXPLOSION) {
                val shake = (1.0f - progress.value) * 10f
                translationX = (Math.random().toFloat() * 2f - 1f) * shake
                translationY = (Math.random().toFloat() * 2f - 1f) * shake
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
            supernovaShader.setFloatUniform("iColor", 1.0f, 0.2f, 0.8f) // Magenta

            drawRect(brush = ShaderBrush(supernovaShader))
        }
    }
}
