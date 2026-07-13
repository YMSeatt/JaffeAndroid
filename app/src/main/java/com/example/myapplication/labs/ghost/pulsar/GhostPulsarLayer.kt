package com.example.myapplication.labs.ghost.pulsar

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostPulsarLayer: A futuristic visualization of classroom harmonics.
 *
 * This layer uses [GhostPulsarShader] to render interference patterns between
 * students, visualizing the "rhythm" of the classroom based on behavioral logs.
 */
@Composable
fun GhostPulsarLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    canvasScale: Float,
    canvasOffset: androidx.compose.ui.geometry.Offset,
    isActive: Boolean = true
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    // BOLT: Performance Cap. We sample the first 20 students to ensure smooth rendering
    // on devices that may struggle with too many simultaneous wave sources in AGSL.
    val studentsToDisplay = remember(students) { students.take(20) }

    // BOLT: Pre-calculate the grouped behavior logs to avoid re-grouping inside the engine.
    val eventsByStudent = remember(behaviorLogs) {
        behaviorLogs.groupBy { it.studentId }
    }

    val currentTimeMillis = System.currentTimeMillis()
    // BOLT: Pass pre-grouped logs and the limited student list to the optimized engine.
    val harmonics = remember(studentsToDisplay, eventsByStudent) {
        GhostPulsarEngine.calculateHarmonics(studentsToDisplay, eventsByStudent, currentTimeMillis)
    }

    val shader = remember { RuntimeShader(GhostPulsarShader.PULSAR_WAVES) }
    val brush = remember(shader) { ShaderBrush(shader) }

    // BOLT: Use rememberInfiniteTransition for better performance and consistency.
    val infiniteTransition = rememberInfiniteTransition(label = "pulsarTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // BOLT: Pre-allocate and remember the FloatArrays to eliminate per-frame object churn.
    val points = remember { FloatArray(20 * 2) }
    val phases = remember { FloatArray(20) }
    val amplitudes = remember { FloatArray(20) }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width <= 0 || height <= 0) return@Canvas

        val currentTime = System.currentTimeMillis()

        // Prepare uniforms
        shader.setFloatUniform("iResolution", width, height)
        shader.setFloatUniform("iTime", time)

        // BOLT: Clear the buffers to avoid stale data.
        points.fill(0f)
        phases.fill(0f)
        amplitudes.fill(0f)

        val count = studentsToDisplay.size
        // BOLT: Use manual loop to avoid iterator allocation.
        for (index in 0 until count) {
            val student = studentsToDisplay[index]
            // Map 4000x4000 logical coordinates to screen space
            val sx = student.xPosition.value * canvasScale + canvasOffset.x
            val sy = student.yPosition.value * canvasScale + canvasOffset.y

            points[index * 2] = sx
            points[index * 2 + 1] = sy

            // BOLT: Direct indexing instead of harmonicsMap[id] lookup, as order is preserved.
            val h = if (index < harmonics.size) harmonics[index] else null
            val freq = h?.frequency ?: 0.1f
            // Smoothly calculate phase at 60fps based on frequency
            val phase = ((currentTime % 60_000L).toFloat() / 60_000f * freq) % 1.0f

            phases[index] = phase
            amplitudes[index] = h?.amplitude ?: 0.5f
        }

        shader.setFloatUniform("iPoints", points)
        shader.setFloatUniform("iPhases", phases)
        shader.setFloatUniform("iAmplitudes", amplitudes)
        shader.setIntUniform("iNumPoints", count)

        drawRect(brush)
    }
}
