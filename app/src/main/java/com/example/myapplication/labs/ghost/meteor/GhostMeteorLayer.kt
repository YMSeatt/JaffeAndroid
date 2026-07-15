package com.example.myapplication.labs.ghost.meteor

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostMeteorLayer: Renders high-momentum neural projectiles and impact shockwaves.
 *
 * This layer integrates with [GhostMeteorEngine] to provide real-time visual feedback
 * for high-impact classroom events using AGSL shaders.
 */
@Composable
fun GhostMeteorLayer(
    engine: GhostMeteorEngine,
    students: List<StudentUiItem>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean = true
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val infiniteTransition = rememberInfiniteTransition(label = "meteor_loop")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Update engine on every frame
    LaunchedEffect(time) {
        engine.update(students)
    }

    val meteorShaders = remember { mutableStateListOf<RuntimeShader>() }
    val impactShaders = remember { mutableStateListOf<RuntimeShader>() }

    // Ensure we have enough shaders in the pools
    LaunchedEffect(engine.meteors.size) {
        while (meteorShaders.size < engine.meteors.size) {
            meteorShaders.add(RuntimeShader(GhostMeteorShader.METEOR_STREAK))
        }
    }
    LaunchedEffect(engine.impacts.size) {
        while (impactShaders.size < engine.impacts.size) {
            impactShaders.add(RuntimeShader(GhostMeteorShader.IMPACT_SHOCKWAVE))
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        withTransform({
            translate(canvasOffset.x, canvasOffset.y)
            scale(canvasScale, canvasScale, Offset.Zero)
        }) {
            // 1. Draw Impacts (Bottom Layer)
            for (i in engine.impacts.indices) {
                if (i >= impactShaders.size) break
                val impact = engine.impacts[i]
                val shader = impactShaders[i]

                val color = when (impact.colorType) {
                    0 -> Color(0xFFBB86FC) // Academic (Purple)
                    1 -> Color.Cyan        // Positive
                    else -> Color.Magenta // Negative
                }

                shader.setFloatUniform("iResolution", size.width, size.height)
                shader.setFloatUniform("iCenter", impact.x, impact.y)
                shader.setFloatUniform("iRadius", impact.radius)
                shader.setFloatUniform("iLife", impact.life)
                shader.setFloatUniform("iColor", color.red, color.green, color.blue, color.alpha)

                drawRect(brush = ShaderBrush(shader))
            }

            // 2. Draw Meteors (Top Layer)
            for (i in engine.meteors.indices) {
                if (i >= meteorShaders.size) break
                val meteor = engine.meteors[i]
                val shader = meteorShaders[i]

                val color = when (meteor.colorType) {
                    0 -> Color(0xFFBB86FC) // Academic (Purple)
                    1 -> Color.Cyan        // Positive
                    else -> Color.Magenta // Negative
                }

                // Prepare trail points (Logical -> Screen space mapping is handled by withTransform)
                val points = FloatArray(GhostMeteorEngine.MAX_TRAIL_POINTS * 2)
                for (p in 0 until GhostMeteorEngine.MAX_TRAIL_POINTS) {
                    val idx = (meteor.trailIndex + p) % GhostMeteorEngine.MAX_TRAIL_POINTS
                    points[p * 2] = meteor.trailX[idx]
                    points[p * 2 + 1] = meteor.trailY[idx]
                }

                shader.setFloatUniform("iResolution", size.width, size.height)
                shader.setFloatUniform("iTime", time)
                shader.setFloatUniform("iPoints", points)
                shader.setFloatUniform("iLife", meteor.life)
                shader.setFloatUniform("iColor", color.red, color.green, color.blue, color.alpha)

                drawRect(brush = ShaderBrush(shader))
            }
        }
    }
}
