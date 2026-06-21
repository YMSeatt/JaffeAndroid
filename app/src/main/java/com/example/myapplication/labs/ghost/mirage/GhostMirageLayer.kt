package com.example.myapplication.labs.ghost.mirage

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize

/**
 * GhostMirageLayer: Compose Neural Focus Heatmap Layer.
 *
 * This layer renders the [GhostMirageShader.MIRAGE_HEATMAP] using the focus intensity
 * grid provided by the engine.
 *
 * Performance (Bolt ⚡):
 * - Hoists [RuntimeShader] and [ShaderBrush] to prevent per-frame object allocation.
 * - Uses [remember] to cache the grid array passed to the shader.
 */
@Composable
fun GhostMirageLayer(
    heatmap: FloatArray,
    isActive: Boolean
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val infiniteTransition = rememberInfiniteTransition(label = "MiragePulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(50000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    var size by remember { mutableStateOf(IntSize(0, 0)) }
    val shader = remember { RuntimeShader(GhostMirageShader.MIRAGE_HEATMAP) }
    val brush = remember(shader) { ShaderBrush(shader) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
    ) {
        if (size.width > 0 && size.height > 0) {
            shader.setFloatUniform("iResolution", size.width.toFloat(), size.height.toFloat())
            shader.setFloatUniform("iTime", time)
            shader.setFloatUniform("iFocusGrid", heatmap)
            shader.setFloatUniform("iGridSize", GhostMirageEngine.GRID_SIZE.toFloat())

            drawRect(brush)
        }
    }
}
