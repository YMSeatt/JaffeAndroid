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
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostPulseLayer: A visualization layer for "Neural Resonance" pulses.
 *
 * This layer uses AGSL Shaders to render expanding ripples around students
 * who have recently interacted with the system.
 */
@Composable
fun GhostPulseLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    canvasScale: Float,
    canvasOffset: Offset,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val infiniteTransition = rememberInfiniteTransition(label = "pulseTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(120000, easing = LinearEasing), // Slow, continuous time
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Update resonances every 500ms instead of every frame to save CPU
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            kotlinx.coroutines.delay(500)
        }
    }

    val resonances = remember(behaviorLogs, currentTime) {
        GhostPulseEngine.calculateResonance(behaviorLogs, currentTime)
    }

    if (resonances.isEmpty()) return

    val studentMap = remember(students) { students.associateBy { it.id.toLong() } }

    val shader = remember { RuntimeShader(GhostPulseShader.NEURAL_PULSE) }
    val brush = remember(shader) { ShaderBrush(shader) }

    // BOLT: Pre-allocate and reuse FloatArray buffers to eliminate per-frame object churn.
    val centers = remember { FloatArray(40) }
    val colors = remember { FloatArray(60) }
    val intensities = remember { FloatArray(20) }
    val radii = remember { FloatArray(20) }

    Canvas(modifier = modifier.fillMaxSize()) {
        // BOLT: Move current timestamp outside of the loop to avoid repeated system calls.
        val timestamp = System.currentTimeMillis()
        val pulseCount = minOf(resonances.size, 20)

        // Reset buffers
        centers.fill(0f)
        colors.fill(0f)
        intensities.fill(0f)
        radii.fill(0f)

        for (i in 0 until pulseCount) {
            val resonance = resonances[i]
            val student = studentMap[resonance.studentId] ?: continue

            // Map student coordinates to screen space
            val centerX = (student.xPosition.value * canvasScale) + canvasOffset.x + (student.displayWidth.value.toPx() * canvasScale / 2f)
            val centerY = (student.yPosition.value * canvasScale) + canvasOffset.y + (student.displayHeight.value.toPx() * canvasScale / 2f)

            centers[i * 2] = centerX
            centers[i * 2 + 1] = centerY

            colors[i * 3] = resonance.color.first
            colors[i * 3 + 1] = resonance.color.second
            colors[i * 3 + 2] = resonance.color.third

            intensities[i] = resonance.intensity

            val age = (timestamp - resonance.startTime).toFloat()
            radii[i] = age * 0.8f * canvasScale
        }

        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        shader.setFloatUniform("iCenters", centers)
        shader.setFloatUniform("iColors", colors)
        shader.setFloatUniform("iIntensities", intensities)
        shader.setFloatUniform("iRadii", radii)
        shader.setIntUniform("iNumPulses", pulseCount)

        drawRect(brush = brush)
    }
}
