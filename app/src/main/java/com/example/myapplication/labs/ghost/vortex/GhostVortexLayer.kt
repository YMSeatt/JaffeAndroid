package com.example.myapplication.labs.ghost.vortex

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush

/**
 * GhostVortexLayer: A Compose layer that renders the Ghost Vortex visualization.
 *
 * BOLT: Optimized to use pre-calculated vortices from the ViewModel and reuse
 * RuntimeShader instances to eliminate per-frame allocations.
 */
@Composable
fun GhostVortexLayer(
    vortices: List<GhostVortexEngine.VortexPoint>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || vortices.isEmpty()) return

    val infiniteTransition = rememberInfiniteTransition(label = "vortexRotation")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // BOLT: Reuse shader instances. Since we cap vortices at 5, we can pre-allocate a small pool.
    val shaderPool = remember {
        List(5) { RuntimeShader(GhostVortexShader.SOCIAL_WHIRLPOOL) }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        vortices.forEachIndexed { index, vortex ->
            if (index >= shaderPool.size) return@forEachIndexed

            val shader = shaderPool[index]

            // Map student coordinates to screen pixels
            val screenX = vortex.x * canvasScale + canvasOffset.x
            val screenY = vortex.y * canvasScale + canvasOffset.y

            shader.setFloatUniform("iResolution", width, height)
            shader.setFloatUniform("iTime", time)
            shader.setFloatUniform("iVortexPos", screenX, screenY)
            shader.setFloatUniform("iRadius", vortex.radius * canvasScale)
            shader.setFloatUniform("iMomentum", vortex.momentum)
            shader.setFloatUniform("iPolarity", vortex.polarity)

            drawRect(
                brush = ShaderBrush(shader),
                topLeft = Offset(
                    screenX - (vortex.radius * 3f * canvasScale),
                    screenY - (vortex.radius * 3f * canvasScale)
                ),
                size = androidx.compose.ui.geometry.Size(
                    vortex.radius * 6f * canvasScale,
                    vortex.radius * 6f * canvasScale
                )
            )
        }
    }
}
