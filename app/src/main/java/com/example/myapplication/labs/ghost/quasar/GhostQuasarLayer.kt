package com.example.myapplication.labs.ghost.quasar

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostQuasarLayer: Visualizes high-energy student activity using AGSL shaders.
 *
 * This layer renders pulsing "Accretion Disks" around students identified as Quasars.
 * These disks serve as high-visibility indicators of current classroom focal points,
 * allowing teachers to see at a glance where social or academic energy is concentrating.
 *
 * Visual Logic:
 * - **Radius**: Scales with the `quasarEnergy` metric.
 * - **Color**: Cyan for positive polarity, Magenta for negative polarity.
 * - **Motion**: Swirls and pulses to represent active social momentum.
 *
 * BOLT: Optimized for 60fps performance by offloading identification logic to the
 * ViewModel and utilizing a pre-allocated shader pool to prevent JNI uniform overwrites.
 */
@Composable
fun GhostQuasarLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>, // Kept for identity/re-render trigger
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    // BOLT: Use rememberInfiniteTransition for frame-rate stability and to avoid
    // triggering recomposition of the entire layer on every frame.
    val infiniteTransition = rememberInfiniteTransition(label = "quasarTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(200000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // BOLT: Pool shaders and brushes to avoid "Uniform Overwrite" bug and object churn.
    // Growable list allows us to keep native objects alive across student count changes.
    val shaderPool = remember { mutableListOf<RuntimeShader>() }
    val brushPool = remember { mutableListOf<ShaderBrush>() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        var quasarIdx = 0

        // BOLT: Hoist invariant uniforms.
        for (i in 0 until shaderPool.size) {
            shaderPool[i].setFloatUniform("iResolution", size.width, size.height)
            shaderPool[i].setFloatUniform("iTime", time)
        }

        // BOLT: Manual loop over students to find those with pre-calculated Quasar energy.
        // Reading student positions and metrics from MutableState ensures smooth tracking
        // during drag gestures without triggering expensive global recompositions.
        for (i in students.indices) {
            val student = students[i]
            val energy = student.quasarEnergy.value
            if (energy < 0.1f) continue

            if (quasarIdx >= shaderPool.size) {
                val s = RuntimeShader(GhostQuasarShader.ACCRETION_DISK)
                // Set invariant uniforms for the new shader instance.
                s.setFloatUniform("iResolution", size.width, size.height)
                s.setFloatUniform("iTime", time)
                shaderPool.add(s)
                brushPool.add(ShaderBrush(s))
            }

            val shader = shaderPool[quasarIdx]
            val brush = brushPool[quasarIdx]
            quasarIdx++

            val polarity = student.quasarPolarity.value
            val color = if (polarity >= 0) Color.Cyan else Color.Magenta

            // Map world coordinates to screen space for the shader
            val screenX = (student.xPosition.value * canvasScale) + canvasOffset.x
            val screenY = (student.yPosition.value * canvasScale) + canvasOffset.y

            // Calculate the screen-space size of the effect (World radius 250f)
            val screenRadius = 250f * canvasScale

            shader.setFloatUniform("iCenter", screenX, screenY)
            shader.setFloatUniform("iEnergy", energy)
            shader.setFloatUniform("iColor", color.red, color.green, color.blue)

            drawRect(
                brush = brush,
                topLeft = Offset(screenX - screenRadius, screenY - screenRadius),
                size = androidx.compose.ui.geometry.Size(screenRadius * 2, screenRadius * 2)
            )
        }
    }
}
