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

    // --- Phase 1: Memoized Data Processing ---

    val studentsToDisplay = remember(students) { students.take(20) }

    // Consolidate log processing into a single pass for O(L) instead of O(3L)
    val processedLogs = remember(behaviorLogs) {
        val negativeMap = mutableMapOf<Long, MutableList<BehaviorEvent>>()
        val positiveMap = mutableMapOf<Long, MutableList<BehaviorEvent>>()
        var negativeCount = 0

        behaviorLogs.forEach { event ->
            if (event.type.contains("Negative", ignoreCase = true)) {
                negativeMap.getOrPut(event.studentId) { mutableListOf() }.add(event)
                negativeCount++
            } else {
                positiveMap.getOrPut(event.studentId) { mutableListOf() }.add(event)
            }
        }

        val agitation = if (behaviorLogs.isEmpty()) 0.2f
        else min(negativeCount.toFloat() / behaviorLogs.size.coerceAtLeast(1) + 0.1f, 1.0f)

        Triple(negativeMap, positiveMap, agitation)
    }

    val negativeLogsByStudent = processedLogs.first
    val positiveLogsByStudent = processedLogs.second
    val agitationLevel = processedLogs.third

    val shader = remember { RuntimeShader(GhostPhantasmShader.PHANTASM_BLOBS) }

    // Pre-allocate arrays to avoid GC pressure during high-frequency Canvas drawing
    val pointsArray = remember { FloatArray(40) }
    val weightsArray = remember { FloatArray(20) }
    val colorsArray = remember { FloatArray(60) }

    Canvas(modifier = modifier.fillMaxSize()) {
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        shader.setFloatUniform("iAgitation", agitationLevel)
        shader.setFloatUniform("iIsRecording", if (isRecording) 1.0f else 0.0f)
        shader.setIntUniform("iNumPoints", studentsToDisplay.size)

        // Clear arrays for reuse
        pointsArray.fill(0f)
        weightsArray.fill(0f)
        colorsArray.fill(0f)

        studentsToDisplay.forEachIndexed { index, student ->
            val centerX = (student.xPosition.value * canvasScale) + canvasOffset.x + (student.displayWidth.value.toPx() * canvasScale / 2f)
            val centerY = (student.yPosition.value * canvasScale) + canvasOffset.y + (student.displayHeight.value.toPx() * canvasScale / 2f)

            pointsArray[index * 2] = centerX
            pointsArray[index * 2 + 1] = centerY

            val negCount = negativeLogsByStudent[student.id.toLong()]?.size ?: 0
            val posCount = positiveLogsByStudent[student.id.toLong()]?.size ?: 0

            weightsArray[index] = 1.0f + (negCount * 0.5f) + (posCount * 0.2f)

            // Color: Reddish for negative, Cyan for positive/neutral
            if (negCount > posCount) {
                colorsArray[index * 3] = 1.0f // R
                colorsArray[index * 3 + 1] = 0.2f // G
                colorsArray[index * 3 + 2] = 0.1f // B
            } else {
                colorsArray[index * 3] = 0.0f // R
                colorsArray[index * 3 + 1] = 0.8f // G
                colorsArray[index * 3 + 2] = 1.0f // B
            }
        }

        shader.setFloatUniform("iPoints", pointsArray)
        shader.setFloatUniform("iWeights", weightsArray)
        shader.setFloatUniform("iColors", colorsArray)

        drawRect(brush = ShaderBrush(shader))
    }
}
