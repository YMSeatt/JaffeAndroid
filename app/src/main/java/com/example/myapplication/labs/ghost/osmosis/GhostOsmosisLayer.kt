package com.example.myapplication.labs.ghost.osmosis

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.example.myapplication.labs.ghost.GhostConfig

/**
 * GhostOsmosisLayer: Renders the fluid diffusion field on the seating chart.
 */
@Composable
fun GhostOsmosisLayer(
    students: List<GhostOsmosisEngine.OsmoticNode>,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !GhostConfig.OSMOSIS_MODE_ENABLED) return

    val infiniteTransition = rememberInfiniteTransition(label = "osmosis")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val gradients = remember(students) {
        GhostOsmosisEngine.calculateOsmosis(students)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val shader = RuntimeShader(GhostOsmosisShader.DIFFUSION_FIELD)

            gradients.forEach { gradient ->
                shader.setFloatUniform("iResolution", size.width, size.height)
                shader.setFloatUniform("iTime", time)
                shader.setFloatUniform("iColor", gradient.color.first, gradient.color.second, gradient.color.third)
                shader.setFloatUniform("iPressure", gradient.potential)

                // Draw a localized diffusion patch for each gradient point
                // Note: In a full implementation, we might use a mesh or a single full-screen shader
                // with more uniforms, but for this PoC, we draw points to demonstrate the field.
                val radius = 200f // Patch size
                drawCircle(
                    brush = ShaderBrush(shader),
                    radius = radius,
                    center = androidx.compose.ui.geometry.Offset(
                        gradient.x / 4000f * size.width,
                        gradient.y / 4000f * size.height
                    )
                )
            }
        }
    }
}
