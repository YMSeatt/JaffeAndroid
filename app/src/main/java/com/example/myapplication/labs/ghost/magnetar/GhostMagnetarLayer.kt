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

    val dipoles = remember(students, behaviorLogs) {
        engine.calculateDipoles(students, behaviorLogs)
    }

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
        shader.setFloatUniform("iHeading", heading)

        val count = dipoles.take(15).size
        shader.setIntUniform("iDipoleCount", count)

        dipoles.take(15).forEachIndexed { index, dipole ->
            // Map logical coordinates to screen space
            val screenPos = Offset(
                x = dipole.x * canvasScale + canvasOffset.x,
                y = dipole.y * canvasScale + canvasOffset.y
            )
            posArray[index * 2] = screenPos.x
            posArray[index * 2 + 1] = screenPos.y
            strengthArray[index] = dipole.strength
        }

        shader.setFloatUniform("iDipolePos", posArray)
        shader.setFloatUniform("iDipoleStrength", strengthArray)

        drawRect(brush = brush)
    }
}
