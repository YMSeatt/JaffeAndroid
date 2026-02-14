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
import kotlin.math.min

/**
 * GhostPhantasmLayer: A futuristic background layer that visualizes "Classroom Presence"
 * using a high-performance Meta-ball algorithm implemented in AGSL.
 *
 * **Conceptual Mechanics:**
 * - **Presence Visualization**: Students are rendered as fluid, glowing cyan/red blobs
 *   (Meta-balls) that dynamically merge and split as they move or interact.
 * - **Behavioral Weighting**: The size and intensity of a student's blob are driven by
 *   their behavioral log density. Students with higher "activity" appear more prominent.
 * - **Privacy Shielding**: Integrates with Android 15's Screen Recording detection to
 *   apply a "Privacy Glitch" noise overlay, obfuscating clear student positions during capture.
 *
 * @param students List of [StudentUiItem]s representing students currently on the canvas.
 * @param behaviorLogs Historical [BehaviorEvent]s used to calculate student "agitation" and weighting.
 * @param canvasScale The current zoom level of the seating chart.
 * @param canvasOffset The current pan position (X/Y) of the seating chart.
 * @param isRecording A boolean flag indicating if the screen is currently being recorded or cast.
 */
@Composable
fun GhostPhantasmLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    canvasScale: Float,
    canvasOffset: Offset,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !GhostConfig.PHANTASM_MODE_ENABLED) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val infiniteTransition = rememberInfiniteTransition(label = "phantasmTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Calculate agitation and student data
    val studentsToDisplay = remember(students) { students.take(20) }
    val negativeLogsByStudent = remember(behaviorLogs) {
        behaviorLogs.filter { it.type.contains("Negative", ignoreCase = true) }
            .groupBy { it.studentId }
    }
    val positiveLogsByStudent = remember(behaviorLogs) {
        behaviorLogs.filter { !it.type.contains("Negative", ignoreCase = true) }
            .groupBy { it.studentId }
    }

    /**
     * Agitation Calculation:
     * Determines the global "tension" of the classroom.
     * Formula: (Negative Logs / Total Logs) + 0.1 baseline.
     * This value drives the 'turbulence' and 'flicker' uniforms in the PHANTASM_BLOBS shader.
     */
    val agitationLevel = remember(behaviorLogs) {
        if (behaviorLogs.isEmpty()) 0.2f
        else {
            val negativeCount = behaviorLogs.count { it.type.contains("Negative", ignoreCase = true) }
            min(negativeCount.toFloat() / behaviorLogs.size.coerceAtLeast(1) + 0.1f, 1.0f)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val shader = RuntimeShader(GhostPhantasmShader.PHANTASM_BLOBS)

        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        shader.setFloatUniform("iAgitation", agitationLevel)
        shader.setFloatUniform("iIsRecording", if (isRecording) 1.0f else 0.0f)
        shader.setIntUniform("iNumPoints", studentsToDisplay.size)

        // Flatten arrays for shader
        val points = FloatArray(40)
        val weights = FloatArray(20)
        val colors = FloatArray(60)

        studentsToDisplay.forEachIndexed { index, student ->
            val centerX = (student.xPosition.value * canvasScale) + canvasOffset.x + (student.displayWidth.value.toPx() * canvasScale / 2f)
            val centerY = (student.yPosition.value * canvasScale) + canvasOffset.y + (student.displayHeight.value.toPx() * canvasScale / 2f)

            points[index * 2] = centerX
            points[index * 2 + 1] = centerY

            val negCount = negativeLogsByStudent[student.id.toLong()]?.size ?: 0
            val posCount = positiveLogsByStudent[student.id.toLong()]?.size ?: 0

            weights[index] = 1.0f + (negCount * 0.5f) + (posCount * 0.2f)

            // Color: Reddish for negative, Cyan for positive/neutral
            if (negCount > posCount) {
                colors[index * 3] = 1.0f // R
                colors[index * 3 + 1] = 0.2f // G
                colors[index * 3 + 2] = 0.1f // B
            } else {
                colors[index * 3] = 0.0f // R
                colors[index * 3 + 1] = 0.8f // G
                colors[index * 3 + 2] = 1.0f // B
            }
        }

        shader.setFloatUniform("iPoints", points)
        shader.setFloatUniform("iWeights", weights)
        shader.setFloatUniform("iColors", colors)

        drawRect(brush = ShaderBrush(shader))
    }
}
