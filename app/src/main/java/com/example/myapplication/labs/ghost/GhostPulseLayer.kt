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
        GhostPulseEngine.calculateResonance(students, behaviorLogs, currentTime)
    }

    if (resonances.isEmpty()) return

    val studentMap = remember(students) { students.associateBy { it.id.toLong() } }

    Canvas(modifier = modifier.fillMaxSize()) {
        resonances.forEach { resonance ->
            val student = studentMap[resonance.studentId] ?: return@forEach

            // Map student coordinates to screen space
            val centerX = (student.xPosition.value * canvasScale) + canvasOffset.x + (student.displayWidth.value.toPx() * canvasScale / 2f)
            val centerY = (student.yPosition.value * canvasScale) + canvasOffset.y + (student.displayHeight.value.toPx() * canvasScale / 2f)

            val shader = RuntimeShader(GhostPulseShader.NEURAL_PULSE)
            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iTime", time)
            shader.setFloatUniform("iCenter", centerX, centerY)
            shader.setFloatUniform("iColor", resonance.color.first, resonance.color.second, resonance.color.third)
            shader.setFloatUniform("iIntensity", resonance.intensity)

            // Calculate expansion radius based on time since event
            val age = (System.currentTimeMillis() - resonance.startTime).toFloat()
            shader.setFloatUniform("iRadius", age * 0.8f * canvasScale)

            drawRect(brush = ShaderBrush(shader))
        }
    }
}
