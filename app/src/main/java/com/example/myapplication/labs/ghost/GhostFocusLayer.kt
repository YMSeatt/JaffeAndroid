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
 * GhostFocusLayer: Renders the neural focus field visualization.
 */
@Composable
fun GhostFocusLayer(
    students: List<StudentUiItem>,
    behaviorLogsByStudent: Map<Long, List<BehaviorEvent>>,
    focusStartTime: Long,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val shader = remember { RuntimeShader(GhostFocusShader.FOCUS_FIELD) }
    val brush = remember(shader) { ShaderBrush(shader) }

    val infiniteTransition = rememberInfiniteTransition(label = "focus_pulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val scores = remember(students, behaviorLogsByStudent, focusStartTime) {
        GhostFocusEngine.calculateConcentrationScores(students, behaviorLogsByStudent, focusStartTime)
    }
    val globalFocus = remember(scores) { GhostFocusEngine.calculateGlobalFocus(scores) }

    // BOLT: Pre-allocate float array for uniforms
    val focusData = remember { FloatArray(80) } // 20 students * 4 floats

    Canvas(modifier = Modifier.fillMaxSize()) {
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        shader.setFloatUniform("iGlobalFocus", globalFocus)

        focusData.fill(0f)
        val count = students.size.coerceAtMost(20)
        for (i in 0 until count) {
            val student = students[i]
            val score = scores.get(student.id.toLong()) ?: 0.8f

            // Map logical 4000x4000 to screen pixels
            val screenX = student.xPosition.value * canvasScale + canvasOffset.x
            val screenY = student.yPosition.value * canvasScale + canvasOffset.y

            focusData[i * 4 + 0] = screenX
            focusData[i * 4 + 1] = screenY
            focusData[i * 4 + 2] = score
            focusData[i * 4 + 3] = 0f
        }

        shader.setFloatUniform("iStudentFocus", focusData)
        shader.setIntUniform("iStudentCount", count)

        drawRect(brush = brush)
    }
}
