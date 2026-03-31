package com.example.myapplication.labs.ghost.pulsar

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.util.toPx

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

    val currentTimeMillis = System.currentTimeMillis()
    val harmonics = remember(students, behaviorLogs) {
        GhostPulsarEngine.calculateHarmonics(students, behaviorLogs, currentTimeMillis)
    }

    // BOLT: Transform harmonics list to map for O(1) lookup in the draw loop.
    val harmonicsMap = remember(harmonics) {
        harmonics.associateBy { it.studentId }
    }

    val shader = remember { RuntimeShader(GhostPulsarShader.PULSAR_WAVES) }
    val brush = remember(shader) { ShaderBrush(shader) }

    // BOLT: Pre-calculate density-dependent values outside the draw loop.
    val density = LocalDensity.current
    val studentsToDisplay = remember(students) { students.take(20) }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width <= 0 || height <= 0) return@Canvas

        val currentTime = System.currentTimeMillis()

        // Prepare uniforms
        shader.setFloatUniform("iResolution", width, height)
        shader.setFloatUniform("iTime", currentTime / 1000f)

        val maxPoints = 20
        val points = FloatArray(maxPoints * 2)
        val phases = FloatArray(maxPoints)
        val amplitudes = FloatArray(maxPoints)

        val count = studentsToDisplay.size
        studentsToDisplay.forEachIndexed { index, student ->
            // Map 4000x4000 logical coordinates to screen space
            // BOLT: Use pre-calculated density from LocalDensity.current
            val lx = with(density) { student.xPosition.value.dp.toPx() }
            val ly = with(density) { student.yPosition.value.dp.toPx() }

            val sx = lx * canvasScale + canvasOffset.x
            val sy = ly * canvasScale + canvasOffset.y

            points[index * 2] = sx
            points[index * 2 + 1] = sy

            // BOLT: O(1) lookup instead of O(N) find
            val h = harmonicsMap[student.id.toLong()]
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
