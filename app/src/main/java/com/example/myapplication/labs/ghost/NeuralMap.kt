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
    negativeCounts: android.util.LongSparseArray<Int>,
    groupedStudents: android.util.LongSparseArray<List<StudentUiItem>>,
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
            // BOLT: Manual index-based loop to eliminate Iterator churn
            for (i in 0 until students.size) {
                val student = students[i]
                val negativeCount = negativeCounts.get(student.id.toLong()) ?: 0
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

        // BOLT: Optimized group connection rendering to avoid per-frame list/Offset allocations.
        for (i in 0 until groupedStudents.size()) {
            val groupMembers = groupedStudents.valueAt(i)
            if (groupMembers.size < 2) continue

            val groupColor = groupMembers[0].groupColor.value ?: Color.Cyan

            // BOLT: Avoid map/list allocation. Calculate center of first member and draw lines to others.
            val s1 = groupMembers[0]
            val x1 = (s1.xPosition.value * canvasScale) + canvasOffset.x + (s1.displayWidth.value.toPx() * canvasScale / 2f)
            val y1 = (s1.yPosition.value * canvasScale) + canvasOffset.y + (s1.displayHeight.value.toPx() * canvasScale / 2f)
            val center1 = Offset(x1, y1)

            // Draw connections (star pattern: everyone connects to the first member)
            for (i in 1 until groupMembers.size) {
                val s2 = groupMembers[i]
                val x2 = (s2.xPosition.value * canvasScale) + canvasOffset.x + (s2.displayWidth.value.toPx() * canvasScale / 2f)
                val y2 = (s2.yPosition.value * canvasScale) + canvasOffset.y + (s2.displayHeight.value.toPx() * canvasScale / 2f)
                val center2 = Offset(x2, y2)

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
