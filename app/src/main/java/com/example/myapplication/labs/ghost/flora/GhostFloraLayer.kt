package com.example.myapplication.labs.ghost.flora

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostFloraLayer: A Proof of Concept for Neural Botanical Visualization.
 * It renders procedural, data-driven flowers under each student's icon on the seating chart.
 *
 * Each flower's appearance (growth, vitality, complexity) is calculated by [GhostFloraEngine]
 * and rendered using an AGSL shader defined in [GhostFloraShader].
 *
 * BOLT Performance Architecture:
 * 1. **Pre-grouping**: Groups logs by student ID once per update to avoid O(N*L) complexity.
 * 2. **Memoization**: Uses `remember` to cache the calculation results and shader objects.
 * 3. **Zero-Allocation**: Hoists all object creations (RuntimeShader, ShaderBrush) out of
 *     the `Canvas` draw scope to achieve stable 60fps.
 *
 * @param students The list of students currently visible on the chart.
 * @param behaviorLogs Global list of behavior events for filtering.
 * @param quizLogs Global list of quiz logs for filtering.
 * @param homeworkLogs Global list of homework logs for filtering.
 * @param canvasScale The current zoom level of the seating chart.
 * @param canvasOffset The current pan offset of the seating chart.
 * @param isActive Toggle to enable/disable the layer.
 */
@Composable
fun GhostFloraLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    quizLogs: List<QuizLog>,
    homeworkLogs: List<HomeworkLog>,
    canvasScale: Float,
    canvasOffset: androidx.compose.ui.geometry.Offset,
    isActive: Boolean
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "floraRotation")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // BOLT Optimization: Pre-group logs outside the draw loop to avoid O(N*L) complexity per frame
    val groupedBehaviors = remember(behaviorLogs) { behaviorLogs.groupBy { it.studentId } }
    val groupedQuizzes = remember(quizLogs) { quizLogs.groupBy { it.studentId } }
    val groupedHomework = remember(homeworkLogs) { homeworkLogs.groupBy { it.studentId } }

    // Pre-calculate flora states and pool shaders/brushes outside the draw loop
    val floraData = remember(students, behaviorLogs, quizLogs, homeworkLogs) {
        students.map { student ->
            val studentId = student.id.toLong()
            val state = GhostFloraEngine.calculateFloraState(
                studentId,
                groupedBehaviors[studentId] ?: emptyList(),
                groupedQuizzes[studentId] ?: emptyList(),
                groupedHomework[studentId] ?: emptyList()
            )
            val shader = RuntimeShader(GhostFloraShader.FLORA_SHADER_SRC)
            val brush = ShaderBrush(shader)
            Triple(state, shader, brush)
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                // Apply global canvas transformations to match seating chart
                scaleX = canvasScale
                scaleY = canvasScale
                translationX = canvasOffset.x
                translationY = canvasOffset.y
            }
    ) {
        students.forEachIndexed { index, student ->
            if (index >= floraData.size) return@forEachIndexed

            val (floraState, shader, brush) = floraData[index]

            // Correct Dp to Px conversion using density
            // xPosition and yPosition are Floats representing Dp values
            val xPosPx = student.xPosition.value.dp.toPx()
            val yPosPx = student.yPosition.value.dp.toPx()
            val floraSizePx = student.displayWidth.value.toPx() * 2f

            shader.setFloatUniform("size", floraSizePx, floraSizePx)
            shader.setFloatUniform("time", time)
            shader.setFloatUniform("growth", floraState.growth)
            shader.setFloatUniform("vitality", floraState.vitality)
            shader.setFloatUniform("complexity", floraState.complexity)
            shader.setFloatUniform("seed", floraState.seed)

            withTransform({
                translate(xPosPx, yPosPx)
            }) {
                drawRect(
                    brush = brush,
                    size = Size(floraSizePx, floraSizePx),
                    topLeft = androidx.compose.ui.geometry.Offset(-floraSizePx / 2f, -floraSizePx / 2f)
                )
            }
        }
    }
}
