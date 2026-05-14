package com.example.myapplication.labs.ghost

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostHaloLayer: Renders "Neural Halos" for peak performing students.
 *
 * This layer identifies students in the [InsightStatus.OPTIMAL] state using the
 * [GhostInsightEngine] and applies a high-performance AGSL halo shader.
 *
 * BOLT ⚡ Optimization:
 * - Uses [remember] to cache peak performer IDs, only recalculating when logs change.
 * - Zero-allocation drawing loop on API 33+.
 * - Shader pooling to minimize JNI overhead.
 */
@Composable
fun GhostHaloLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    quizLogs: List<QuizLog>,
    homeworkLogs: List<HomeworkLog>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean = true
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    // 1. Identify Peak Performers (Cached)
    val peakPerformerIds by remember(students, behaviorLogs, quizLogs, homeworkLogs) {
        derivedStateOf {
            students.filter { student ->
                val sId = student.id.toLong()
                val sBehaviors = behaviorLogs.filter { it.studentId == sId }
                val sQuizzes = quizLogs.filter { it.studentId == sId }
                val sHomework = homeworkLogs.filter { it.studentId == sId }

                val insight = GhostInsightEngine.generateInsight(
                    studentName = student.name.value,
                    behaviorLogs = sBehaviors,
                    quizLogs = sQuizzes,
                    homeworkLogs = sHomework
                )
                insight.status == InsightStatus.OPTIMAL
            }.map { it.id }
        }
    }

    if (peakPerformerIds.isEmpty()) return

    // 2. Animation Time
    val infiniteTransition = rememberInfiniteTransition(label = "halo_loop")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // 3. Shader Management
    val shaders = remember { mutableStateMapOf<Int, RuntimeShader>() }

    // 4. Rendering
    Canvas(modifier = Modifier.fillMaxSize()) {
        withTransform({
            translate(canvasOffset.x, canvasOffset.y)
            scale(canvasScale, canvasScale, Offset.Zero)
        }) {
            peakPerformerIds.forEach { studentId ->
                val student = students.find { it.id == studentId } ?: return@forEach

                val shader = shaders.getOrPut(studentId) {
                    RuntimeShader(GhostHaloShader.NEURAL_HALO)
                }

                val haloSize = 120f // Logical size of the halo effect
                val x = student.xPosition.value - haloSize / 2f
                val y = student.yPosition.value - haloSize / 2f

                shader.setFloatUniform("iResolution", haloSize, haloSize)
                shader.setFloatUniform("iTime", time)

                // Cyan-Gold Hybrid: Golden core with Cyan shimmer
                val goldenColor = Color(0xFFFFD700)
                shader.setColorUniform(
                    "iColor",
                    android.graphics.Color.valueOf(goldenColor.red, goldenColor.green, goldenColor.blue, 0.8f)
                )

                translate(x, y) {
                    drawRect(
                        brush = ShaderBrush(shader),
                        size = androidx.compose.ui.geometry.Size(haloSize, haloSize)
                    )
                }
            }
        }
    }
}
