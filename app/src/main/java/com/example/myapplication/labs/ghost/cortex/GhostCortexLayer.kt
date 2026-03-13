package com.example.myapplication.labs.ghost.cortex

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.input.pointer.pointerInput
import com.example.myapplication.labs.ghost.GhostConfig

/**
 * GhostCortexLayer: Specialized visualization for Somatic Field and Neural Intent.
 *
 * This layer reacts to touch gestures, calculating the "Neural Tension" under the
 * teacher's finger and triggering high-fidelity haptic feedback via [GhostCortexEngine].
 *
 * ### 2027 R&D Directive:
 * Multi-sensory data exploration where visual organic ripples correlate 1:1 with
 * the tactile somatic pulses.
 */
@Composable
fun GhostCortexLayer(
    engine: GhostCortexEngine,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "cortexTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(100000, easing = LinearEasing)),
        label = "time"
    )

    var touchPos by remember { mutableStateOf(Offset.Zero) }
    var tension by remember { mutableFloatStateOf(0f) }

    val shader = remember { RuntimeShader(GhostCortexShader.SOMATIC_FIELD) }
    val brush = remember(shader) { ShaderBrush(shader) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { pos ->
                        touchPos = pos
                        // Simulation: Tension increases as you "dig" into the somatic field
                        tension = (tension + 0.1f).coerceAtMost(1.0f)
                        engine.triggerSomaticPulse(tension)
                    },
                    onDrag = { change, _ ->
                        touchPos = change.position
                        // Dynamic tension based on movement velocity and position
                        // In a real scenario, this would look up student data at touchPos
                        engine.triggerSomaticPulse(tension)
                    },
                    onDragEnd = {
                        tension = 0f
                    }
                )
            }
    ) {
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        shader.setFloatUniform("iTension", tension)
        shader.setFloatUniform("iTouchPos", touchPos.x, touchPos.y)

        drawRect(brush = brush)
    }
}
