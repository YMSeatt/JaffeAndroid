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

/**
 * NeuralMapLayer: A Composable that renders a data-driven visualization layer over the seating chart.
 *
 * It uses AGSL shaders to visualize:
 * 1. **Group Connections**: Animated lines connecting students belonging to the same group.
 * 2. **Cognitive Auras**: Pulsating red glows around students with more than 2 negative behavior logs.
 *
 * This layer is only rendered if `GhostConfig.GHOST_MODE_ENABLED` is true and the device
 * supports AGSL (API 33+).
 *
 * @param students The list of students to visualize.
 * @param behaviorLogs Historical behavior data used to determine aura intensity.
 * @param canvasScale The current zoom level of the seating chart.
 * @param canvasOffset The current pan position of the seating chart.
 */
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
        students.filter { it.groupId.value != null }.groupBy { it.groupId.value }
    }

    val negativeLogsByStudent = remember(behaviorLogs) {
        behaviorLogs.filter { it.type.contains("Negative", ignoreCase = true) }
            .groupBy { it.studentId }
    }

    // BOLT: Pre-allocate shader pools to avoid O(N) allocations in the draw loop.
    // Unique instances are required per item because Android's recording Canvas
    // captures the shader's state at the time of the draw call.
    val auraShaderPool = remember { mutableListOf<RuntimeShader>() }
    val auraBrushPool = remember { mutableListOf<ShaderBrush>() }
    val lineShaderPool = remember { mutableListOf<RuntimeShader>() }
    val lineBrushPool = remember { mutableListOf<ShaderBrush>() }

    Canvas(modifier = modifier.fillMaxSize()) {
        var auraIdx = 0
        var lineIdx = 0

        // Draw Cognitive Auras for students with many negative logs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && GhostConfig.COGNITIVE_ENGINE_ENABLED) {
            students.forEach { student ->
                val negativeCount = negativeLogsByStudent[student.id.toLong()]?.size ?: 0
                if (negativeCount > 2) {
                    val centerX = (student.xPosition.value * canvasScale) + canvasOffset.x + (student.displayWidth.value.toPx() * canvasScale / 2f)
                    val centerY = (student.yPosition.value * canvasScale) + canvasOffset.y + (student.displayHeight.value.toPx() * canvasScale / 2f)

                    if (auraIdx >= auraShaderPool.size) {
                        val s = RuntimeShader(GhostShader.COGNITIVE_AURA)
                        auraShaderPool.add(s)
                        auraBrushPool.add(ShaderBrush(s))
                    }
                    val shader = auraShaderPool[auraIdx]
                    val brush = auraBrushPool[auraIdx]
                    auraIdx++

                    shader.setFloatUniform("iResolution", size.width, size.height)
                    shader.setFloatUniform("iTime", time)
                    shader.setFloatUniform("iCenter", centerX, centerY)
                    shader.setFloatUniform("iColor", 1.0f, 0.2f, 0.1f) // Reddish aura
                    shader.setFloatUniform("iIntensity", min(negativeCount / 5f, 1.0f))

                    drawRect(brush = brush)
                }
            }
        }

        groupedStudents.forEach { (_, groupMembers) ->
            if (groupMembers.size < 2) return@forEach

            val groupColor = groupMembers.first().groupColor.value ?: Color.Cyan

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
                    if (lineIdx >= lineShaderPool.size) {
                        val s = RuntimeShader(GhostShader.NEURAL_LINE)
                        lineShaderPool.add(s)
                        lineBrushPool.add(ShaderBrush(s))
                    }
                    val shader = lineShaderPool[lineIdx]
                    val brush = lineBrushPool[lineIdx]
                    lineIdx++

                    shader.setFloatUniform("iTime", time)
                    shader.setFloatUniform("iColor", groupColor.red, groupColor.green, groupColor.blue)
                    shader.setFloatUniform("iResolution", size.width, size.height)

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
