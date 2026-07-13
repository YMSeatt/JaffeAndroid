package com.example.myapplication.labs.ghost.weaver

import android.graphics.RuntimeShader
import android.os.Build
import android.util.LongSparseArray
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostWeaverLayer: Renders the "Neural Thread" connections between students.
 *
 * This layer uses [GhostWeaverShader] to draw stylized connections between
 * students who share academic milestones.
 *
 * ### Architectural Intent:
 * The Weaver layer transforms abstract academic data into a living social fabric.
 * By visualizing connections between students with similar trajectories, it provides
 * teachers with immediate, spatial insight into classroom synergy.
 *
 * ### BOLT ⚡ (Performance-Obsessed) Optimization:
 * 1. **Shader Pooling**: Implements a [RuntimeShader] pool to avoid heavy object allocations
 *    and JNI overhead during 60fps rendering.
 * 2. **Primitive Containers**: Uses [LongSparseArray] to avoid [Long] boxing overhead
 *    during high-frequency student coordinate lookups.
 * 3. **Manual Iteration**: Replaces functional iterators with manual index-based loops
 *    in the draw path to minimize GC pressure.
 * 4. **GPU Pruning**: Uses `drawLine` instead of full-screen rects, limiting the fragment
 *    shader execution to the precise bounds of the thread (plus glow padding).
 *
 * @param students The current list of interactive student UI items.
 * @param threads The list of identified synergy connections from [GhostWeaverEngine].
 * @param canvasScale The current zoom factor of the seating chart.
 * @param canvasOffset The current pan offset of the seating chart.
 * @param isActive Whether the Weaver experiment is currently enabled.
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
    val timeState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // BOLT: Implement a persistent shader pool to avoid re-allocating RuntimeShaders.
    val shaderPool = remember { mutableListOf<RuntimeShader>() }
    remember(threads.size) {
        while (shaderPool.size < threads.size) {
            shaderPool.add(RuntimeShader(GhostWeaverShader.NEURAL_THREAD))
        }
    }

    // BOLT: Use LongSparseArray to avoid Long boxing during student lookups.
    val studentMap = remember(students) {
        LongSparseArray<StudentUiItem>(students.size).apply {
            for (i in students.indices) {
                val s = students[i]
                put(s.id.toLong(), s)
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val time = timeState.value
        // BOLT: Replace forEachIndexed with manual loop for 60fps drawing.
        for (i in threads.indices) {
            val thread = threads[i]
            val sA = studentMap.get(thread.studentA) ?: continue
            val sB = studentMap.get(thread.studentB) ?: continue

            val shader = shaderPool[i]
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
