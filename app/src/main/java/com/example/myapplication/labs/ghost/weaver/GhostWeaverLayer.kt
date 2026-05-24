package com.example.myapplication.labs.ghost.weaver

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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostWeaverLayer: Renders the "Neural Thread" connections between students.
 *
 * This layer uses [GhostWeaverShader] to draw stylized connections between
 * students who share academic milestones.
 *
 * BOLT ⚡ Optimization: Instead of drawing a full-screen rectangle for each thread
 * (which causes massive overdraw), this layer draws constrained lines to limit
 * fragment shader execution to the thread area.
 */
@Composable
fun GhostWeaverLayer(
    students: List<StudentUiItem>,
    threads: List<GhostWeaverEngine.NeuralThread>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean
) {
    if (!isActive || threads.isEmpty() || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val infiniteTransition = rememberInfiniteTransition(label = "weaver_flow")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val shaders = remember(threads.size) {
        threads.map { RuntimeShader(GhostWeaverShader.NEURAL_THREAD) }
    }

    val studentMap = remember(students) { students.associateBy { it.id.toLong() } }

    Canvas(modifier = Modifier.fillMaxSize()) {
        threads.forEachIndexed { index, thread ->
            val sA = studentMap[thread.studentA] ?: return@forEachIndexed
            val sB = studentMap[thread.studentB] ?: return@forEachIndexed

            val shader = shaders.getOrNull(index) ?: return@forEachIndexed
            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iTime", time)

            val posAx = (sA.xPosition.value * canvasScale) + canvasOffset.x + (sA.displayWidth.value.toPx() * canvasScale / 2f)
            val posAy = (sA.yPosition.value * canvasScale) + canvasOffset.y + (sA.displayHeight.value.toPx() * canvasScale / 2f)
            val posBx = (sB.xPosition.value * canvasScale) + canvasOffset.x + (sB.displayWidth.value.toPx() * canvasScale / 2f)
            val posBy = (sB.yPosition.value * canvasScale) + canvasOffset.y + (sB.displayHeight.value.toPx() * canvasScale / 2f)

            shader.setFloatUniform("iPointA", posAx, posAy)
            shader.setFloatUniform("iPointB", posBx, posBy)
            shader.setFloatUniform("iStrength", thread.strength)

            val color = when (thread.type) {
                GhostWeaverEngine.ThreadType.ACADEMIC_SYNERGY -> Color(0xFF00E5FF) // Cyan
                GhostWeaverEngine.ThreadType.HOMEWORK_COLLABORATION -> Color(0xFF7C4DFF) // Purple
            }
            shader.setFloatUniform("iColor", color.red, color.green, color.blue)

            // BOLT ⚡: Draw a line with a wide stroke instead of a full-screen rect.
            // This ensures the fragment shader only runs for pixels within the line's reach (including glow).
            drawLine(
                brush = ShaderBrush(shader),
                start = Offset(posAx, posAy),
                end = Offset(posBx, posBy),
                strokeWidth = 40f * canvasScale // Enough width to cover the exponential glow
            )
        }
    }
}
