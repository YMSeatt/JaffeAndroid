package com.example.myapplication.labs.ghost

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import kotlinx.coroutines.delay

/**
 * GhostFluxLayer: A futuristic neural flow visualization layer.
 *
 * Implements a fluid dynamics simulation using AGSL Shaders to visualize
 * "Classroom Momentum". It integrates with [GhostFluxEngine] to provide
 * tactile haptic feedback (Android 15+) synchronized with the visual flow.
 *
 * This layer uses domain-warping noise to simulate the movement of cognitive energy
 * across the classroom grid, with student icons acting as attractors or vortices
 * in the flow.
 */
@Composable
fun GhostFluxLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    canvasScale: Float,
    canvasOffset: Offset,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !GhostConfig.FLUX_MODE_ENABLED) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val fluxEngine = remember { GhostFluxEngine(context) }

    val infiniteTransition = rememberInfiniteTransition(label = "fluxTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(120000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Calculate Global Engagement/Agitation for Haptics
    val globalAgitation = remember(behaviorLogs) {
        if (behaviorLogs.isEmpty()) 0.2f
        else {
            val count = behaviorLogs.size.coerceAtMost(100)
            (count.toFloat() / 100f).coerceIn(0.2f, 1.0f)
        }
    }

    /**
     * Neural Haptic Pulse:
     * Triggers periodic haptic feedback that matches the visual rhythm of the flux flow.
     * Uses Android 15's high-fidelity VibrationEffect.Composition via [GhostFluxEngine].
     */
    LaunchedEffect(globalAgitation) {
        while (true) {
            fluxEngine.triggerFlowPulse(globalAgitation)
            // Pulse frequency scales inversely with agitation (faster pulses for higher energy)
            val delayMillis = (4000 - (globalAgitation * 3500)).toLong().coerceAtLeast(400)
            delay(delayMillis)
        }
    }

    val shader = remember { RuntimeShader(GhostFluxShader.NEURAL_FLOW) }
    val studentsToDisplay = remember(students) { students.take(20) }

    // Pre-calculate log counts to avoid O(S * L) in the Canvas draw loop
    val logCountsByStudent = remember(behaviorLogs) {
        behaviorLogs.groupingBy { it.studentId }.eachCount()
    }

    // Pre-allocated arrays to avoid GC pressure during high-frequency Canvas drawing
    val points = remember { FloatArray(40) }
    val weights = remember { FloatArray(20) }

    Canvas(modifier = modifier.fillMaxSize()) {
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        shader.setIntUniform("iNumPoints", studentsToDisplay.size)

        // Reset arrays for reuse
        points.fill(0f)
        weights.fill(0f)

        studentsToDisplay.forEachIndexed { index, student ->
            // Map logical student coordinates to screen space for the shader
            val centerX = (student.xPosition.value * canvasScale) + canvasOffset.x + (student.displayWidth.value.toPx() * canvasScale / 2f)
            val centerY = (student.yPosition.value * canvasScale) + canvasOffset.y + (student.displayHeight.value.toPx() * canvasScale / 2f)

            points[index * 2] = centerX
            points[index * 2 + 1] = centerY

            // Weighting student influence based on log density (O(1) lookup)
            val logCount = logCountsByStudent[student.id.toLong()] ?: 0
            weights[index] = (logCount.toFloat() / 5f).coerceAtMost(2.0f)
        }

        shader.setFloatUniform("iPoints", points)
        shader.setFloatUniform("iWeights", weights)

        drawRect(brush = ShaderBrush(shader))
    }
}
