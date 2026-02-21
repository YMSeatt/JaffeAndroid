package com.example.myapplication.labs.ghost.phasing

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import com.example.myapplication.labs.ghost.GhostConfig

/**
 * GhostPhasingLayer: The visual bridge between the classroom UI and the Neural Backstage.
 *
 * It applies the transition shader to its children and renders the [NEURAL_VOID]
 * background when the phase is active.
 */
@Composable
fun GhostPhasingLayer(
    engine: GhostPhasingEngine,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !GhostConfig.PHASING_MODE_ENABLED) {
        content()
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "phasingTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(100000, easing = LinearEasing)),
        label = "time"
    )

    val phase by engine.phaseLevel

    // Optimize by remembering shaders and effects
    val voidShader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RuntimeShader(GhostPhasingShader.NEURAL_VOID)
        } else null
    }

    val transitionShader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RuntimeShader(GhostPhasingShader.PHASE_TRANSITION)
        } else null
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Backstage Background (Neural Void)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && phase > 0.01f && voidShader != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                voidShader.setFloatUniform("iResolution", size.width, size.height)
                voidShader.setFloatUniform("iTime", time)
                voidShader.setFloatUniform("iIntensity", phase)
                drawRect(brush = androidx.compose.ui.graphics.ShaderBrush(voidShader))
            }
        }

        // Main Content with Transition Effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && phase > 0f && transitionShader != null) {
                        transitionShader.setFloatUniform("iResolution", size.width, size.height)
                        transitionShader.setFloatUniform("iTime", time)
                        transitionShader.setFloatUniform("iPhase", phase)

                        renderEffect = RenderEffect.createRuntimeShaderEffect(
                            transitionShader, "iContent"
                        ).asComposeRenderEffect()
                    }
                    alpha = (1f - phase * 0.8f).coerceIn(0.2f, 1f)
                }
        ) {
            content()
        }
    }
}
