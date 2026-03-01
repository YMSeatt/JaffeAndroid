package com.example.myapplication.labs.ghost

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostSparkLayer: Visual integration for the Neural Particle System.
 *
 * This layer renders the [NEURAL_SPARK] shader, driving its uniforms from the
 * [GhostSparkEngine]'s state-backed particle list. It provides high-performance
 * visualization by batching up to 100 particles per shader pass.
 */
@Composable
fun GhostSparkLayer(
    engine: GhostSparkEngine,
    students: List<StudentUiItem>,
    canvasScale: Float,
    canvasOffset: Offset,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !GhostConfig.SPARK_MODE_ENABLED) {
        return
    }

    // Infinite time loop for shader animation
    val infiniteTransition = rememberInfiniteTransition(label = "sparkTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 100f,
        animationSpec = infiniteRepeatable(tween(100000, easing = LinearEasing)),
        label = "time"
    )

    // Physics Update Loop (Internal tick)
    LaunchedEffect(students) {
        while (true) {
            engine.update(students, deltaTime = 1.0f)
            kotlinx.coroutines.delay(16) // Target ~60fps for physics
        }
    }

    // AGSL Shader Setup
    val sparkShader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RuntimeShader(GhostSparkShader.NEURAL_SPARK)
        } else null
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && sparkShader != null) {
        val activeSparks = engine.sparks.take(100) // Pass up to 100 sparks to the shader

        Canvas(modifier = modifier.fillMaxSize()) {
            sparkShader.setFloatUniform("iResolution", size.width, size.height)
            sparkShader.setFloatUniform("iTime", time)
            sparkShader.setIntUniform("iNumParticles", activeSparks.size)

            val positions = FloatArray(activeSparks.size * 2)
            val colors = FloatArray(activeSparks.size * 3)
            val lives = FloatArray(activeSparks.size)
            val sizes = FloatArray(activeSparks.size)

            activeSparks.forEachIndexed { index, spark ->
                // Transform logical coordinates to screen space for the shader
                positions[index * 2] = (spark.x * canvasScale) + canvasOffset.x
                positions[index * 2 + 1] = (spark.y * canvasScale) + canvasOffset.y

                // Map color types to RGB (Cyan, Magenta, Purple)
                when (spark.colorType) {
                    0 -> { // Positive: Cyan
                        colors[index * 3] = 0.0f
                        colors[index * 3 + 1] = 0.8f
                        colors[index * 3 + 2] = 0.9f
                    }
                    1 -> { // Negative: Magenta
                        colors[index * 3] = 0.9f
                        colors[index * 3 + 1] = 0.1f
                        colors[index * 3 + 2] = 0.6f
                    }
                    else -> { // Academic: Purple
                        colors[index * 3] = 0.6f
                        colors[index * 3 + 1] = 0.4f
                        colors[index * 3 + 2] = 0.9f
                    }
                }

                lives[index] = spark.life
                sizes[index] = spark.size
            }

            // Pad the arrays to meet the shader's uniform size (100)
            val paddedPositions = FloatArray(200).apply {
                positions.copyInto(this)
            }
            val paddedColors = FloatArray(300).apply {
                colors.copyInto(this)
            }
            val paddedLives = FloatArray(100).apply {
                lives.copyInto(this)
            }
            val paddedSizes = FloatArray(100).apply {
                sizes.copyInto(this)
            }

            sparkShader.setFloatUniform("iPoints", paddedPositions)
            sparkShader.setFloatUniform("iColors", paddedColors)
            sparkShader.setFloatUniform("iLives", paddedLives)
            sparkShader.setFloatUniform("iSizes", paddedSizes)

            drawRect(brush = ShaderBrush(sparkShader))
        }
    }
}
