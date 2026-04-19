package com.example.myapplication.labs.ghost.silhouette

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import com.example.myapplication.labs.ghost.GhostConfig

/**
 * SilhouetteData: Represents the captured state of a student being dragged.
 */
data class SilhouetteData(
    val id: Int,
    val originalX: Float,
    val originalY: Float,
    val width: Float,
    val height: Float,
    val color: Color = Color.Cyan
)

/**
 * GhostSilhouetteLayer: Renders ghostly placeholders at the original positions of
 * students currently being dragged.
 *
 * This provides visual continuity, showing the "ghost" of where the student
 * was before they were moved.
 */
@Composable
fun GhostSilhouetteLayer(
    silhouettes: List<SilhouetteData>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean
) {
    if (!isActive || !GhostConfig.SILHOUETTE_MODE_ENABLED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    if (silhouettes.isEmpty()) return

    val infiniteTransition = rememberInfiniteTransition(label = "silhouetteTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val shaderPool = remember { mutableListOf<RuntimeShader>() }
    val brushPool = remember { mutableListOf<ShaderBrush>() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        silhouettes.forEachIndexed { index, data ->
            if (index >= shaderPool.size) {
                val s = RuntimeShader(GhostSilhouetteShader.SILHOUETTE)
                shaderPool.add(s)
                brushPool.add(ShaderBrush(s))
            }

            val shader = shaderPool[index]
            val brush = brushPool[index]

            val screenX = (data.originalX * canvasScale) + canvasOffset.x
            val screenY = (data.originalY * canvasScale) + canvasOffset.y
            val screenW = data.width * canvasScale
            val screenH = data.height * canvasScale

            // Draw slightly larger to accommodate the glow
            val glowMargin = 20f * canvasScale
            val drawWidth = screenW + glowMargin * 2
            val drawHeight = screenH + glowMargin * 2

            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iTime", time)
            shader.setFloatUniform("iCenter", screenX + screenW / 2, screenY + screenH / 2)
            shader.setFloatUniform("iSize", screenW, screenH)
            shader.setFloatUniform("iColor", data.color.red, data.color.green, data.color.blue)

            drawRect(
                brush = brush,
                topLeft = Offset(screenX - glowMargin, screenY - glowMargin),
                size = Size(drawWidth, drawHeight)
            )
        }
    }
}
