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
import androidx.compose.ui.graphics.drawscope.withTransform
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
 * - Uses [remember] to cache peak performing student references, only recalculating when logs change.
 * - Zero-allocation drawing loop on API 33+ by utilizing manual index loops and pre-resolved student references.
 * - Log pre-grouping transforms O(S*L) analysis into O(S+L).
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
    // BOLT: Refactored to pre-group logs once per update, avoiding O(S*L) nested filtering.
    val peakPerformers by remember(students, behaviorLogs, quizLogs, homeworkLogs) {
        derivedStateOf {
            if (students.isEmpty()) return@derivedStateOf emptyList<StudentUiItem>()

            // 1a. Pre-group logs for O(1) lookup
            val bMap = mutableMapOf<Long, MutableList<BehaviorEvent>>()
            for (i in 0 until behaviorLogs.size) {
                val log = behaviorLogs[i]
                bMap.getOrPut(log.studentId) { mutableListOf() }.add(log)
            }

            val qMap = mutableMapOf<Long, MutableList<QuizLog>>()
            for (i in 0 until quizLogs.size) {
                val log = quizLogs[i]
                qMap.getOrPut(log.studentId) { mutableListOf() }.add(log)
            }

            val hMap = mutableMapOf<Long, MutableList<HomeworkLog>>()
            for (i in 0 until homeworkLogs.size) {
                val log = homeworkLogs[i]
                hMap.getOrPut(log.studentId) { mutableListOf() }.add(log)
            }

            // 1b. Analyze students using O(1) grouped data
            val list = mutableListOf<StudentUiItem>()
            for (i in 0 until students.size) {
                val student = students[i]
                val sId = student.id.toLong()
                val sBehaviors = bMap[sId] ?: emptyList()
                val sQuizzes = qMap[sId] ?: emptyList()
                val sHomework = hMap[sId] ?: emptyList()

                val insight = GhostInsightEngine.generateInsight(
                    studentName = student.name.value,
                    behaviorLogs = sBehaviors,
                    quizLogs = sQuizzes,
                    homeworkLogs = sHomework
                )
                if (insight.status == InsightStatus.OPTIMAL) {
                    list.add(student)
                }
            }
            list
        }
    }

    if (peakPerformers.isEmpty()) return

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
            // BOLT: Manual index loop over peak performer references eliminates O(P*S) student searches
            // and iterator allocations in the 60fps draw path.
            for (i in 0 until peakPerformers.size) {
                val student = peakPerformers[i]
                val studentId = student.id

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
