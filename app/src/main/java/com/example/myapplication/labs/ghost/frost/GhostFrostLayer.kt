package com.example.myapplication.labs.ghost.frost

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
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.Student

/**
 * GhostFrostLayer: A BOLT-optimized ⚡ visualization layer for "Cold Zones".
 *
 * This layer renders procedural frost crystallization over students who are
 * identified by [GhostFrostEngine] as being in a high-entropy "Cold Zone".
 *
 * Optimization Techniques:
 * 1. **Shader Pooling**: Reuses RuntimeShader instances to avoid allocations.
 * 2. **State Hoisting**: Defers coordinate transformations to the draw phase.
 * 3. **Identity Preservation**: Uses student IDs to track nodes across frames.
 */
@Composable
fun GhostFrostLayer(
    students: List<Student>,
    behaviorLogs: List<BehaviorEvent>,
    quizLogs: List<QuizLog>,
    homeworkLogs: List<HomeworkLog>,
    canvasScale: Float,
    canvasOffset: Offset
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    // BOLT: Calculate frost nodes in the background
    val frostNodes by remember(students, behaviorLogs, quizLogs, homeworkLogs) {
        derivedStateOf {
            GhostFrostEngine.calculateFrost(students, behaviorLogs, quizLogs, homeworkLogs)
        }
    }

    if (frostNodes.isEmpty()) return

    val infiniteTransition = rememberInfiniteTransition(label = "frost_pulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // BOLT: Reuse shader instances
    val shaderPool = remember { mutableMapOf<Long, RuntimeShader>() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        // BOLT: Manual index-based loop for 60fps rendering
        for (i in 0 until frostNodes.size) {
            val node = frostNodes[i]

            val shader = shaderPool.getOrPut(node.studentId) {
                RuntimeShader(GhostFrostShader.FROST_EFFECT)
            }

            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iTime", time)
            shader.setFloatUniform("iTarget", node.x, node.y)
            shader.setFloatUniform("iIntensity", node.intensity)
            shader.setFloatUniform("iCanvasOffset", canvasOffset.x, canvasOffset.y)
            shader.setFloatUniform("iCanvasScale", canvasScale)

            drawRect(brush = ShaderBrush(shader))
        }
    }
}
