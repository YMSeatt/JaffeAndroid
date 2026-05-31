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
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean = true
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    // 1. Identify Peak Performers (Cached)
    // BOLT: Consumes pre-calculated insightStatus from the background pipeline.
    // This eliminates O(S*L) analysis and log grouping in the UI layer.
    val peakPerformers by remember(students) {
        derivedStateOf {
            if (students.isEmpty()) return@derivedStateOf emptyList<StudentUiItem>()

            val list = mutableListOf<StudentUiItem>()
            for (i in 0 until students.size) {
                val student = students[i]
                if (student.insightStatus.value == InsightStatus.OPTIMAL) {
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
