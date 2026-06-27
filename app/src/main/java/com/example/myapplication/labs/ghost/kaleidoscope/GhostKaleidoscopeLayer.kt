package com.example.myapplication.labs.ghost.kaleidoscope

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush
import com.example.myapplication.labs.ghost.GhostConfig

/**
 * GhostKaleidoscopeLayer: UI implementation of the neural symmetry visualization.
 */
@Composable
fun GhostKaleidoscopeLayer(
    fragments: List<GhostKaleidoscopeEngine.NeuralFragment>,
    harmony: Float,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !isActive) return

    val infiniteTransition = rememberInfiniteTransition(label = "kaleidoscopeTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { RuntimeShader(GhostKaleidoscopeShader.KALEIDOSCOPE_FIELD) }
        val brush = remember(shader) { ShaderBrush(shader) }
        val fragmentsArray = remember { FloatArray(12 * 4) }

        Canvas(modifier = modifier.fillMaxSize()) {
            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iTime", time)
            shader.setFloatUniform("iHarmony", harmony)

            fragmentsArray.fill(0f)
            var activeCount = 0
            for (i in fragments.indices) {
                if (activeCount >= 12) break
                val f = fragments[i]

                // Map logical 4000x4000 to screen pixels
                val screenX = f.x * canvasScale + canvasOffset.x
                val screenY = f.y * canvasScale + canvasOffset.y

                fragmentsArray[activeCount * 4 + 0] = screenX
                fragmentsArray[activeCount * 4 + 1] = screenY
                fragmentsArray[activeCount * 4 + 2] = f.polarity
                fragmentsArray[activeCount * 4 + 3] = f.intensity
                activeCount++
            }

            shader.setFloatUniform("iFragments", fragmentsArray)
            shader.setIntUniform("iFragmentCount", activeCount)

            drawRect(brush = brush)
        }
    }
}
