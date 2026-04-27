package com.example.myapplication.labs.ghost.sync

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
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostSyncLayer: Visualizes real-time collaboration bridges.
 *
 * BOLT: Optimized with shader pooling and pre-indexed student lookups.
 */
@Composable
fun GhostSyncLayer(
    students: List<StudentUiItem>,
    syncLinks: List<GhostSyncEngine.SyncLink>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !isActive) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val infiniteTransition = rememberInfiniteTransition(label = "syncTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // BOLT: Pre-allocate a pool of shaders and brushes outside the draw loop
    // to avoid jank on the first frame a sync link is detected.
    val maxLinks = 5
    val shaderPool = remember {
        List(maxLinks) { RuntimeShader(GhostSyncShader.NEURAL_BRIDGE) }
    }
    val brushPool = remember(shaderPool) {
        shaderPool.map { ShaderBrush(it) }
    }
    val studentMap = remember(students) { students.associateBy { it.id.toLong() } }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val numToDraw = syncLinks.size.coerceAtMost(maxLinks)
            for (index in 0 until numToDraw) {
                val link = syncLinks[index]
                val sA = studentMap[link.studentA]
                val sB = studentMap[link.studentB]
                if (sA == null || sB == null) continue

                val shader = shaderPool[index]
                val brush = brushPool[index]

                shader.setFloatUniform("iResolution", size.width, size.height)
                shader.setFloatUniform("iTime", time)

                val posAx = (sA.xPosition.value * canvasScale) + canvasOffset.x + (sA.displayWidth.value.toPx() * canvasScale / 2f)
                val posAy = (sA.yPosition.value * canvasScale) + canvasOffset.y + (sA.displayHeight.value.toPx() * canvasScale / 2f)
                val posBx = (sB.xPosition.value * canvasScale) + canvasOffset.x + (sB.displayWidth.value.toPx() * canvasScale / 2f)
                val posBy = (sB.yPosition.value * canvasScale) + canvasOffset.y + (sB.displayHeight.value.toPx() * canvasScale / 2f)

                shader.setFloatUniform("iPosA", posAx, posAy)
                shader.setFloatUniform("iPosB", posBx, posBy)
                shader.setFloatUniform("iStrength", link.strength)
                shader.setFloatUniform("iColor", 0.0f, 0.8f, 1.0f) // Cyan Neural Bridge

                drawRect(brush = brush)
            }
        }
    }
}
