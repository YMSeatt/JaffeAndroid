package com.example.myapplication.labs.ghost.scape

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush
import com.example.myapplication.ui.model.StudentUiItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlin.random.Random

/**
 * GhostScapeLayer: Visual resonance layer for Neural Spatial Audio.
 *
 * This layer coordinates with [GhostScapeEngine] to trigger audio pings
 * and render synchronized visual ripples on the seating chart.
 */
@Composable
fun GhostScapeLayer(
    engine: GhostScapeEngine,
    students: List<StudentUiItem>,
    negativeCounts: android.util.LongSparseArray<Int>,
    isActive: Boolean,
    canvasScale: Float,
    canvasOffset: Offset
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    // Track active visual ripples using Compose State for reactivity
    val activeRipples = remember { mutableStateListOf<RippleState>() }

    // Audio Trigger Loop
    LaunchedEffect(isActive, students) {
        if (!isActive || students.isEmpty()) return@LaunchedEffect

        while (isActive) {
            // BOLT: Weighted student selection moved to background
            val target = withContext(Dispatchers.Default) {
                val candidates = students.filter {
                    (negativeCounts.get(it.id.toLong()) ?: 0) > 0
                }.ifEmpty { students }
                candidates.random()
            }

            val negCount = negativeCounts.get(target.id.toLong()) ?: 0

            // Map state to audio parameters
            val freq = 440f + (negCount * 110f).coerceAtMost(880f)
            val intensity = 0.3f + (negCount * 0.1f).coerceAtMost(0.7f)

            engine.playPing(
                studentId = target.id.toLong(),
                logicalX = target.xPosition.value,
                logicalY = target.yPosition.value,
                frequency = freq,
                intensity = intensity
            )

            // Random delay between pings (2-5 seconds)
            delay(Random.nextLong(2000, 5000))
        }
    }

    // Sync UI Ripples with Audio Events
    LaunchedEffect(engine) {
        engine.pingEvents.collectLatest { event ->
            activeRipples.add(RippleState(event.studentId, event.x, event.y, event.intensity))
        }
    }

    // BOLT: Frame-based animation loop for smooth ripples
    LaunchedEffect(isActive) {
        if (isActive) {
            while (true) {
                withFrameMillis {
                    val iterator = activeRipples.iterator()
                    while (iterator.hasNext()) {
                        val ripple = iterator.next()
                        ripple.progress += 0.02f
                        if (ripple.progress >= 1f) {
                            iterator.remove()
                        }
                    }
                }
            }
        }
    }

    // Render Layer
    val shader = remember { RuntimeShader(GhostScapeShader.SCAPE_RIPPLE) }
    val brush = remember(shader) { ShaderBrush(shader) }

    Canvas(modifier = Modifier.fillMaxSize()) {
        // BOLT: Reuse shader and brush to avoid per-frame allocations
        for (i in activeRipples.indices) {
            val ripple = activeRipples[i]

            // Map logical to screen coordinates
            val sx = (ripple.logicalX * canvasScale) + canvasOffset.x
            val sy = (ripple.logicalY * canvasScale) + canvasOffset.y

            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iCenter", sx, sy)
            shader.setFloatUniform("iRadius", ripple.progress * 800f * canvasScale)
            shader.setFloatUniform("iIntensity", ripple.intensity * (1f - ripple.progress))
            shader.setColorUniform("iColor", android.graphics.Color.CYAN)

            drawRect(brush = brush)
        }
    }
}

class RippleState(
    val studentId: Long,
    val logicalX: Float,
    val logicalY: Float,
    val intensity: Float
) {
    // BOLT: Progress is a mutable state to ensure it triggers recomposition or is read in a loop
    var progress by mutableFloatStateOf(0f)
}
