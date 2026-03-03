package com.example.myapplication.labs.ghost.entropy

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.QuizLog
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostEntropyLayer: A visualization layer that applies "Neural Entropy" distortion.
 *
 * This layer uses [android.graphics.RenderEffect] and [GhostEntropyShader] to distort
 * the seating chart based on the calculated entropy of the students.
 */
@Composable
fun GhostEntropyLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    quizLogs: List<QuizLog>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Box(modifier = modifier.fillMaxSize()) {
            content()
        }
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "entropyTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(50000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Calculate entropy for each student
    val studentEntropies = remember(students, behaviorLogs, quizLogs) {
        students.take(20).map { student ->
            val sLogs = behaviorLogs.filter { it.studentId == student.id.toLong() }
            val sQuizzes = quizLogs.filter { it.studentId == student.id.toLong() }

            val bEntropy = GhostEntropyEngine.calculateBehaviorEntropy(sLogs)
            val aVariance = GhostEntropyEngine.calculateAcademicVariance(sQuizzes)

            GhostEntropyEngine.calculateEntropyScore(bEntropy, aVariance)
        }
    }

    val entropyShader = remember { RuntimeShader(GhostEntropyShader.ENTROPY_DISTORTION) }

    // We'll use a simplified version for the PoC: Global entropy based on the most chaotic student
    val maxEntropy = remember(studentEntropies) {
        studentEntropies.maxOrNull() ?: 0f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                if (maxEntropy > 0.1f) {
                    entropyShader.setFloatUniform("iResolution", size.width, size.height)
                    entropyShader.setFloatUniform("iTime", time)
                    entropyShader.setFloatUniform("iEntropy", maxEntropy)

                    renderEffect = RenderEffect.createRuntimeShaderEffect(entropyShader, "contents")
                        .asComposeRenderEffect()
                }
            }
    ) {
        content()
    }
}
