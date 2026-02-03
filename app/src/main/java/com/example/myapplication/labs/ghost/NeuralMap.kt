package com.example.myapplication.labs.ghost

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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.min

@Composable
fun NeuralMapLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    canvasScale: Float,
    canvasOffset: Offset,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED) return

    val infiniteTransition = rememberInfiniteTransition(label = "neuralPulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val groupedStudents = remember(students) {
        students.filter { it.groupId != null }.groupBy { it.groupId }
    }

    val negativeLogsByStudent = remember(behaviorLogs) {
        behaviorLogs.filter { it.type.contains("Negative", ignoreCase = true) }
            .groupBy { it.studentId }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Draw Cognitive Auras for students with many negative logs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && GhostConfig.COGNITIVE_ENGINE_ENABLED) {
            students.forEach { student ->
                val negativeCount = negativeLogsByStudent[student.id.toLong()]?.size ?: 0
                if (negativeCount > 2) {
                    val centerX = (student.xPosition.value * canvasScale) + canvasOffset.x + (student.displayWidth.value.toPx() * canvasScale / 2f)
                    val centerY = (student.yPosition.value * canvasScale) + canvasOffset.y + (student.displayHeight.value.toPx() * canvasScale / 2f)

                    val shader = RuntimeShader(GhostShader.COGNITIVE_AURA)
                    shader.setFloatUniform("iResolution", size.width, size.height)
                    shader.setFloatUniform("iTime", time)
                    shader.setFloatUniform("iCenter", centerX, centerY)
                    shader.setFloatUniform("iColor", 1.0f, 0.2f, 0.1f) // Reddish aura
                    shader.setFloatUniform("iIntensity", min(negativeCount / 5f, 1.0f))

                    drawRect(brush = ShaderBrush(shader))
                }
            }
        }

        groupedStudents.forEach { (_, groupMembers) ->
            if (groupMembers.size < 2) return@forEach

            val groupColor = groupMembers.first().groupColor ?: Color.Cyan

            // Calculate centers in the canvas coordinate system
            val centers = groupMembers.map { student ->
                val x = (student.xPosition.value * canvasScale) + canvasOffset.x
                val y = (student.yPosition.value * canvasScale) + canvasOffset.y
                val w = student.displayWidth.value.toPx() * canvasScale
                val h = student.displayHeight.value.toPx() * canvasScale
                Offset(x + w / 2f, y + h / 2f)
            }

            // Draw connections (star pattern: everyone connects to the first member)
            val center1 = centers.first()
            for (i in 1 until centers.size) {
                val center2 = centers[i]

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val shader = RuntimeShader(GhostShader.NEURAL_LINE)
                    shader.setFloatUniform("iTime", time)
                    shader.setFloatUniform("iColor", groupColor.red, groupColor.green, groupColor.blue)
                    shader.setFloatUniform("iResolution", size.width, size.height)

                    val brush = ShaderBrush(shader)
                    drawLine(
                        brush = brush,
                        start = center1,
                        end = center2,
                        strokeWidth = 4.dp.toPx() * canvasScale,
                        cap = StrokeCap.Round
                    )
                } else {
                    drawLine(
                        color = groupColor.copy(alpha = 0.6f),
                        start = center1,
                        end = center2,
                        strokeWidth = 2.dp.toPx() * canvasScale,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
