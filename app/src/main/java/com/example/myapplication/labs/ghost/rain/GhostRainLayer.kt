package com.example.myapplication.labs.ghost.rain

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.labs.ghost.GhostConfig
import com.example.myapplication.ui.model.StudentUiItem
import kotlinx.coroutines.delay

/**
 * GhostRainLayer: A data-driven atmospheric visualization layer.
 *
 * This layer renders falling "Data Droplets" that splash against student icons,
 * creating an immersive classroom climate visualization.
 */
@Composable
fun GhostRainLayer(
    engine: GhostRainEngine,
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "rainTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(100000, easing = LinearEasing)),
        label = "time"
    )

    // Physics Update Loop (60fps)
    LaunchedEffect(isActive, students, behaviorLogs) {
        // BOLT: Offload physics update to Default dispatcher
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            while (isActive) {
                engine.update(students, behaviorLogs)
                delay(16)
            }
        }
    }

    val shader = remember { RuntimeShader(GhostRainShader.NEURAL_DROPLETS) }
    val brush = remember(shader) { ShaderBrush(shader) }

    // Buffers for Shader Uniforms (Top 20 droplets for performance)
    val positions = remember { FloatArray(40) }
    val splashes = remember { FloatArray(20) }

    Canvas(modifier = modifier.fillMaxSize()) {
        var activeCount = 0
        for (i in 0 until GhostRainEngine.MAX_DROPLETS) {
            if (activeCount >= 20) break
            if (engine.dropActive[i]) {
                // Map logical 4000x4000 to screen space
                val sx = (engine.dropX[i] * canvasScale) + canvasOffset.x
                val sy = (engine.dropY[i] * canvasScale) + canvasOffset.y

                positions[activeCount * 2] = sx
                positions[activeCount * 2 + 1] = sy
                splashes[activeCount] = engine.splashTime[i]
                activeCount++
            }
        }

        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        shader.setFloatUniform("iDropPos", positions)
        shader.setFloatUniform("iSplashTime", splashes)
        shader.setIntUniform("iDropCount", activeCount)

        drawRect(brush = brush)
    }
}
