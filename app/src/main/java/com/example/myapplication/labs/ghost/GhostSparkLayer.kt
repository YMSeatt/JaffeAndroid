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
    val sparkBrush = remember(sparkShader) { sparkShader?.let { ShaderBrush(it) } }

    // BOLT: Pre-allocate and remember the FloatArrays used for shader uniforms
    // to eliminate per-frame GC pressure.
    val uniformPositions = remember { FloatArray(200) }
    val uniformColors = remember { FloatArray(300) }
    val uniformLives = remember { FloatArray(100) }
    val uniformSizes = remember { FloatArray(100) }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && sparkShader != null && sparkBrush != null) {
        val activeSparks = engine.sparks

        Canvas(modifier = modifier.fillMaxSize()) {
            val numParticles = activeSparks.size.coerceAtMost(100)
            sparkShader.setFloatUniform("iResolution", size.width, size.height)
            sparkShader.setFloatUniform("iTime", time)
            sparkShader.setIntUniform("iNumParticles", numParticles)

            // BOLT: Replace take(100) list allocation with direct index-based loop.
            for (i in 0 until numParticles) {
                val spark = activeSparks[i]

                // Transform logical coordinates to screen space for the shader
                uniformPositions[i * 2] = (spark.x * canvasScale) + canvasOffset.x
                uniformPositions[i * 2 + 1] = (spark.y * canvasScale) + canvasOffset.y

                // Map color types to RGB (Cyan, Magenta, Purple)
                when (spark.colorType) {
                    0 -> { // Positive: Cyan
                        uniformColors[i * 3] = 0.0f
                        uniformColors[i * 3 + 1] = 0.8f
                        uniformColors[i * 3 + 2] = 0.9f
                    }
                    1 -> { // Negative: Magenta
                        uniformColors[i * 3] = 0.9f
                        uniformColors[i * 3 + 1] = 0.1f
                        uniformColors[i * 3 + 2] = 0.6f
                    }
                    else -> { // Academic: Purple
                        uniformColors[i * 3] = 0.6f
                        uniformColors[i * 3 + 1] = 0.4f
                        uniformColors[i * 3 + 2] = 0.9f
                    }
                }

                uniformLives[i] = spark.life
                uniformSizes[i] = spark.size
            }

            sparkShader.setFloatUniform("iPoints", uniformPositions)
            sparkShader.setFloatUniform("iColors", uniformColors)
            sparkShader.setFloatUniform("iLives", uniformLives)
            sparkShader.setFloatUniform("iSizes", uniformSizes)

            drawRect(brush = sparkBrush)
        }
    }
}
