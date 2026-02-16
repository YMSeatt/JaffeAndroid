package com.example.myapplication.labs.ghost

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
import com.example.myapplication.data.BehaviorEvent

/**
 * GhostSpectraLayer: The interactive UI component for the "Ghost Spectra" experiment.
 *
 * This layer renders a vertical "Data Prism" that the user can drag across the screen.
 * As it passes over the classroom, it refacts the UI into a spectral visualization of
 * student data, driven by the [GhostSpectraEngine].
 *
 * @param behaviorLogs Current classroom behavioral logs used to calculate spectral properties.
 * @param modifier Standard Compose [Modifier].
 */
@Composable
fun GhostSpectraLayer(
    behaviorLogs: List<BehaviorEvent>,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !GhostConfig.SPECTRA_MODE_ENABLED) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    // Interactive state for the prism position
    var prismX by remember { mutableFloatStateOf(0.5f) } // Normalized 0.0 to 1.0

    // Engine-driven properties
    val density = remember(behaviorLogs) { GhostSpectraEngine.calculateSpectralDensity(behaviorLogs) }
    val agitation = remember(behaviorLogs) { GhostSpectraEngine.calculateAgitation(behaviorLogs) }

    val infiniteTransition = rememberInfiniteTransition(label = "spectraPulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val shader = remember { RuntimeShader(GhostSpectraShader.SPECTRA_PRISM) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Update prism position based on horizontal drag
                    val newX = (prismX + dragAmount.x / size.width).coerceIn(0f, 1f)
                    prismX = newX
                }
            }
    ) {
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        shader.setFloatUniform("iPrismX", prismX)
        shader.setFloatUniform("iDensity", density)
        shader.setFloatUniform("iAgitation", agitation)

        drawRect(brush = ShaderBrush(shader))
    }
}
