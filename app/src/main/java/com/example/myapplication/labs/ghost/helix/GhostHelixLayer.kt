package com.example.myapplication.labs.ghost.helix

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import com.example.myapplication.labs.ghost.GhostConfig
import com.example.myapplication.ui.theme.GhostCyan
import com.example.myapplication.ui.theme.GhostMagenta
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * GhostHelixLayer: Renders the Neural DNA helix over a student component.
 */
@Composable
fun GhostHelixLayer(
    sequence: GhostHelixEngine.HelixSequence,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !GhostConfig.HELIX_MODE_ENABLED) {
        content()
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "helixTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(100000, easing = LinearEasing)),
        label = "time"
    )

    val trajectory = remember(sequence) { GhostHelixEngine.calculateTrajectory(sequence) }

    // Choose color based on trajectory
    val baseColor = if (trajectory > 0.6f) GhostCyan else if (trajectory < 0.4f) GhostMagenta else Color.White

    val shader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RuntimeShader(GhostHelixShader.NEURAL_HELIX)
        } else null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && shader != null) {
                    shader.setFloatUniform("iResolution", size.width, size.height)
                    shader.setFloatUniform("iTime", time)
                    shader.setFloatUniform("iStability", sequence.stability)
                    shader.setFloatUniform("iTwist", sequence.twistRate)

                    shader.setFloatUniform("iColor",
                        baseColor.red,
                        baseColor.green,
                        baseColor.blue
                    )

                    renderEffect = RenderEffect.createRuntimeShaderEffect(
                        shader, "contents"
                    ).asComposeRenderEffect()
                }
            }
    ) {
        content()
    }
}
