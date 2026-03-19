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
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostVortexLayer: A Compose layer that renders the Ghost Vortex visualization.
 *
 * This layer integrates [GhostVortexEngine] for analysis and [GhostVortexShader]
 * for spatial distortion effects. It uses high-performance AGSL shaders (API 33+).
 */
@Composable
fun GhostVortexLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val vortices = remember(students, behaviorLogs) {
        GhostVortexEngine.identifyVortices(students, behaviorLogs)
    }

    if (vortices.isEmpty()) return

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

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        vortices.forEach { vortex ->
            // Map student coordinates to screen pixels
            val screenX = vortex.x * canvasScale + canvasOffset.x
            val screenY = vortex.y * canvasScale + canvasOffset.y

            val shader = RuntimeShader(GhostVortexShader.SOCIAL_WHIRLPOOL)
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
