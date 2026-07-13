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

    val studentsToDisplay = remember(students) { students.take(40) }

    // Pre-allocated arrays for spatial density and shader uniforms
    val studentX = remember { FloatArray(40) }
    val studentY = remember { FloatArray(40) }
    val points = remember { FloatArray(80) }
    val weights = remember { FloatArray(40) }

    // Calculate Global Engagement/Agitation for Haptics using enhanced logic
    val flowMetrics = remember(studentsToDisplay, behaviorLogs) {
        for (i in studentsToDisplay.indices) {
            studentX[i] = studentsToDisplay[i].xPosition.value
            studentY[i] = studentsToDisplay[i].yPosition.value
        }
        val density = GhostFluxEngine.calculateSpatialDensity(
            studentX,
            studentY,
            studentsToDisplay.size
        )
        val intensity = GhostFluxEngine.calculateFlowIntensity(
            studentsToDisplay.size,
            behaviorLogs.size,
            density
        )
        Pair(intensity, density)
    }

    val globalIntensity = flowMetrics.first

    /**
     * Neural Haptic Pulse:
     * Triggers periodic haptic feedback that matches the visual rhythm of the flux flow.
     * Uses Android 15's high-fidelity VibrationEffect.Composition via [GhostFluxEngine].
     */
    LaunchedEffect(globalIntensity) {
        while (true) {
            fluxEngine.triggerFlowPulse(globalIntensity)
            // Pulse frequency scales inversely with intensity (faster pulses for higher energy)
            val delayMillis = (4000 - (globalIntensity * 3600)).toLong().coerceAtLeast(300)
            delay(delayMillis)
        }
    }

    val shader = remember { RuntimeShader(GhostFluxShader.NEURAL_FLOW) }
    val brush = remember(shader) { ShaderBrush(shader) }

    // Pre-calculate log counts to avoid O(S * L) in the Canvas draw loop
    val logCountsByStudent = remember(behaviorLogs) {
        behaviorLogs.groupingBy { it.studentId }.eachCount()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        shader.setIntUniform("iNumPoints", studentsToDisplay.size)

        // Reset arrays for reuse
        points.fill(0f)
        weights.fill(0f)

        for (i in studentsToDisplay.indices) {
            val student = studentsToDisplay[i]
            // Map logical student coordinates to screen space for the shader
            val centerX = (student.xPosition.value * canvasScale) + canvasOffset.x + (student.displayWidth.value.toPx() * canvasScale / 2f)
            val centerY = (student.yPosition.value * canvasScale) + canvasOffset.y + (student.displayHeight.value.toPx() * canvasScale / 2f)

            points[i * 2] = centerX
            points[i * 2 + 1] = centerY

            // Weighting student influence based on log density (O(1) lookup)
            val logCount = logCountsByStudent[student.id.toLong()] ?: 0
            weights[i] = (logCount.toFloat() / 5f).coerceAtMost(2.0f)
        }

        shader.setFloatUniform("iPoints", points)
        shader.setFloatUniform("iWeights", weights)

        drawRect(brush = brush)
    }
}
