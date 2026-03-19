package com.example.myapplication.labs.ghost.quasar

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.withFrameNanos
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
 * This layer renders pulsing accretion disks around students identified as "Quasars".
 * It is optimized for 60fps performance by offloading identification logic to the
 * ViewModel and utilizing a pre-allocated shader pool.
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

    // BOLT: Determine the number of quasars to pre-allocate shaders.
    // This avoids mutating lists during the draw phase.
    val quasarCount = remember(students) {
        var count = 0
        for (i in students.indices) {
            if (students[i].quasarEnergy.value >= 0.1f) count++
        }
        count
    }

    val shaderPool = remember(quasarCount) {
        List(quasarCount) { RuntimeShader(GhostQuasarShader.ACCRETION_DISK) }
    }
    val brushPool = remember(shaderPool) {
        shaderPool.map { ShaderBrush(it) }
    }

    var time by remember { mutableStateOf(0f) }
    LaunchedEffect(isActive) {
        val startTime = System.nanoTime()
        while (isActive) {
            withFrameNanos { frameTime ->
                time = (frameTime - startTime) / 1_000_000_000f
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        var quasarIdx = 0

        // BOLT: Manual loop over students to find those with pre-calculated Quasar energy.
        // Reading student positions and metrics from MutableState ensures smooth tracking
        // during drag gestures without triggering expensive global recompositions.
        for (i in students.indices) {
            val student = students[i]
            val energy = student.quasarEnergy.value
            if (energy < 0.1f || quasarIdx >= shaderPool.size) continue

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

            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iTime", time)
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
