package com.example.myapplication.labs.ghost.comet

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostCometLayer: Renders the Ghost Comet activity visualization.
 *
 * This layer uses [GhostCometEngine] for physics and [GhostCometShader] for
 * high-performance AGSL rendering of trailing streaks.
 */
@Composable
fun GhostCometLayer(
    engine: GhostCometEngine,
    students: List<StudentUiItem>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean = true
) {
    if (!isActive) return

    val infiniteTransition = rememberInfiniteTransition(label = "comet_loop")
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

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shaders = remember { mutableStateListOf<RuntimeShader>() }

        // Dynamic shader management based on comet count
        LaunchedEffect(engine.comets.size) {
            while (shaders.size < engine.comets.size) {
                shaders.add(RuntimeShader(GhostCometShader.COMET_TRAIL))
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            withTransform({
                translate(canvasOffset.x, canvasOffset.y)
                scale(canvasScale, canvasScale, Offset.Zero)
            }) {
                for (i in engine.comets.indices) {
                    if (i >= shaders.size) break
                    val comet = engine.comets[i]
                    val shader = shaders[i]

                    val color = when (comet.colorType) {
                        0 -> Color.Cyan
                        1 -> Color.Magenta
                        else -> Color(0xFFBB86FC) // Purple
                    }

                    // Pack trail points into a flat FloatArray for the uniform
                    val points = FloatArray(GhostCometEngine.MAX_TRAIL_POINTS * 2)
                    for (p in 0 until GhostCometEngine.MAX_TRAIL_POINTS) {
                        // Reorder trail points so the current head is at the end (for tapering)
                        val idx = (comet.trailIndex + p) % GhostCometEngine.MAX_TRAIL_POINTS
                        points[p * 2] = comet.trailX[idx]
                        points[p * 2 + 1] = comet.trailY[idx]
                    }

                    shader.setFloatUniform("iResolution", size.width, size.height)
                    shader.setFloatUniform("iTime", time)
                    shader.setFloatUniform("iPoints", points)
                    shader.setFloatUniform("iLife", comet.life)
                    shader.setFloatUniform("iColor", color.red, color.green, color.blue, color.alpha)

                    drawRect(brush = ShaderBrush(shader))
                }
            }
        }
    }
}
