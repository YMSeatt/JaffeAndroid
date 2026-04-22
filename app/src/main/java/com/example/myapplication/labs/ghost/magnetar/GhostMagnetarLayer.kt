package com.example.myapplication.labs.ghost.magnetar

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
 * GhostMagnetarLayer: A Compose layer that renders the social magnetic field.
 */
@Composable
fun GhostMagnetarLayer(
    engine: GhostMagnetarEngine,
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val heading by engine.magneticHeading.collectAsState()

    // BOLT Optimization: Cap at 15 students to prevent shader uniform overflow
    // and maintain 60fps fragment throughput.
    val studentsToDisplay = remember(students) { students.take(15) }

    val infiniteTransition = rememberInfiniteTransition(label = "magnetarTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val shader = remember { RuntimeShader(GhostMagnetarShader.MAGNETIC_FIELD_LINES) }
    val brush = remember(shader) { ShaderBrush(shader) }

    // Pre-allocate uniform arrays
    val posArray = remember { FloatArray(30) } // 15 points * 2
    val strengthArray = remember { FloatArray(15) }

    Canvas(modifier = modifier.fillMaxSize()) {
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        // Passes device orientation to the shader to skew the visual field lines
        shader.setFloatUniform("iHeading", heading)

        val count = studentsToDisplay.size
        shader.setIntUniform("iDipoleCount", count)

        // BOLT: Read properties directly from StudentUiItem MutableState. This allows
        // 60fps fluid tracking during student drag operations as field lines follow
        // the icon without whole-layer recomposition or redundant object allocations.
        for (index in 0 until count) {
            val student = studentsToDisplay[index]
            // Map logical coordinates to screen space
            posArray[index * 2] = student.xPosition.value * canvasScale + canvasOffset.x
            posArray[index * 2 + 1] = student.yPosition.value * canvasScale + canvasOffset.y
            strengthArray[index] = student.magneticStrength.value
        }

        shader.setFloatUniform("iDipolePos", posArray)
        shader.setFloatUniform("iDipoleStrength", strengthArray)

        drawRect(brush = brush)
    }
}
