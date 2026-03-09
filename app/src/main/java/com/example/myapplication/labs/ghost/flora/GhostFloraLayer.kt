package com.example.myapplication.labs.ghost.flora

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.QuizLog
import com.example.myapplication.ui.model.StudentUiItem
import kotlinx.coroutines.delay

/**
 * GhostFloraLayer: Integrates the Neural Botanical effect into the seating chart.
 */
@Composable
fun GhostFloraLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    quizLogs: List<QuizLog>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    var time by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            time += 0.016f
            delay(16)
        }
    }

    // Pre-allocate shader and brush pool to avoid frame drops
    val shaderPool = remember { mutableStateMapOf<Long, RuntimeShader>() }
    val brushPool = remember { mutableStateMapOf<Long, ShaderBrush>() }

    // BOLT: Move data filtering and state calculation out of the draw loop to avoid frame drops
    val floraStates = remember(students, behaviorLogs, quizLogs) {
        students.associate { student ->
            val studentBehavior = behaviorLogs.filter { it.studentId == student.id.toLong() }
            val studentQuiz = quizLogs.filter { it.studentId == student.id.toLong() }
            student.id.toLong() to GhostFloraEngine.calculateFloraState(
                student.id.toLong(),
                studentBehavior,
                studentQuiz
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        students.forEach { student ->
            val flora = floraStates[student.id.toLong()] ?: return@forEach

            val shader = shaderPool.getOrPut(student.id.toLong()) {
                RuntimeShader(GhostFloraShader.NEURAL_BLOOM)
            }
            val brush = brushPool.getOrPut(student.id.toLong()) {
                ShaderBrush(shader)
            }

            // Map logical coordinates to screen space
            val x = student.xPosition.value * canvasScale + canvasOffset.x
            val y = student.yPosition.value * canvasScale + canvasOffset.y

            // Flower size scales with growth
            val size = 150f * flora.growth * canvasScale

            shader.setFloatUniform("iResolution", size, size)
            shader.setFloatUniform("iTime", time)
            shader.setFloatUniform("iGrowth", flora.growth)
            shader.setFloatUniform("iVitality", flora.vitality)
            shader.setIntUniform("iPetalCount", flora.petalCount)
            shader.setFloatUniform("iComplexity", flora.complexity)
            shader.setFloatUniform("iColorShift", flora.colorShift)

            drawContext.canvas.save()
            drawContext.canvas.translate(x - size / 2f, y - size / 2f)
            drawRect(
                brush = brush,
                size = androidx.compose.ui.geometry.Size(size, size)
            )
            drawContext.canvas.restore()
        }
    }
}
