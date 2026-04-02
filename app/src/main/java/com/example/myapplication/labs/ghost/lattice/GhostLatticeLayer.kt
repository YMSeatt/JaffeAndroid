package com.example.myapplication.labs.ghost.lattice

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
import com.example.myapplication.labs.ghost.GhostConfig

/**
 * GhostLatticeLayer: A futuristic visualization layer for classroom social dynamics.
 *
 * This component renders the student relationship graph (the "Social Lattice") inferred by the
 * [GhostLatticeEngine]. It utilizes high-performance AGSL shaders to draw glowing, pulsing
 * connections between student nodes.
 *
 * BOLT: Optimized to receive pre-calculated [edges] from background pipeline.
 *
 * @param students The list of student UI items, providing current positions and dimensions.
 * @param edges Pre-calculated social connection edges.
 * @param canvasScale The current zoom level of the seating chart.
 * @param canvasOffset The current pan offset of the seating chart.
 */
@Composable
fun GhostLatticeLayer(
    students: List<StudentUiItem>,
    edges: List<GhostLatticeEngine.Edge>,
    canvasScale: Float,
    canvasOffset: Offset,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !GhostConfig.LATTICE_MODE_ENABLED) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val infiniteTransition = rememberInfiniteTransition(label = "latticeTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 100f,
        animationSpec = infiniteRepeatable(animation = tween(100000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "time"
    )

    // BOLT: Use a shader pool to avoid the "Uniform Overwrite" bug during Canvas recording.
    // Each distinct connection requires its own shader instance to maintain unique uniforms.
    val shaderPool = remember { List(50) { RuntimeShader(GhostLatticeShader.NEURAL_LATTICE) } }
    val studentMap = remember(students) { students.associateBy { it.id.toLong() } }

    Canvas(modifier = modifier.fillMaxSize()) {
        edges.take(shaderPool.size).forEachIndexed { index, edge ->
            val fromStudent = studentMap[edge.fromId]
            val toStudent = studentMap[edge.toId]
            if (fromStudent != null && toStudent != null) {
                val shader = shaderPool[index]

                // Calculate pixel-perfect centers for connection endpoints, accounting for scale and offset.
                val startX = (fromStudent.xPosition.value * canvasScale) + canvasOffset.x + (fromStudent.displayWidth.value.toPx() * canvasScale / 2f)
                val startY = (fromStudent.yPosition.value * canvasScale) + canvasOffset.y + (fromStudent.displayHeight.value.toPx() * canvasScale / 2f)
                val endX = (toStudent.xPosition.value * canvasScale) + canvasOffset.x + (toStudent.displayWidth.value.toPx() * canvasScale / 2f)
                val endY = (toStudent.yPosition.value * canvasScale) + canvasOffset.y + (toStudent.displayHeight.value.toPx() * canvasScale / 2f)

                shader.setFloatUniform("iResolution", size.width, size.height)
                shader.setFloatUniform("iTime", time)
                shader.setFloatUniform("iStartPos", startX, startY)
                shader.setFloatUniform("iEndPos", endX, endY)
                shader.setFloatUniform("iColor", edge.color.red, edge.color.green, edge.color.blue)
                shader.setFloatUniform("iStrength", edge.strength)
                shader.setFloatUniform("iType", edge.type.value)

                // Optimized drawing: only draw the bounding box of the connection line to reduce fragment shader overhead.
                val minX = minOf(startX, endX) - 50; val minY = minOf(startY, endY) - 50
                val maxX = maxOf(startX, endX) + 50; val maxY = maxOf(startY, endY) + 50
                drawRect(brush = ShaderBrush(shader), topLeft = Offset(minX, minY), size = androidx.compose.ui.geometry.Size(maxX - minX, maxY - minY))
            }
        }
    }
}
