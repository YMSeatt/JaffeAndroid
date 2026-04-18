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
 * ### Performance Design:
 * - **Shader Pooling**: Reuses `RuntimeShader` instances to avoid the overhead of native object
 *   re-allocation and the "Uniform Overwrite" bug in rapid Draw passes.
 * - **Bounding Box Clipping**: Instead of drawing a full-screen Canvas for every edge, this
 *   layer calculates a tight `drawRect` around each connection line to minimize fragment shader
 *   work and pixel-fill pressure.
 *
 * @param students The list of student UI items, providing current positions and dimensions.
 * @param edges Pre-calculated social connection edges.
 * @param canvasScale The current zoom level of the seating chart.
 * @param canvasOffset The current pan offset of the seating chart.
 * @param modifier The modifier to apply to the [Canvas].
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

    // BOLT: Pool shaders and brushes to avoid Uniform Overwrite bug and allocations.
    // Using a growable list for lazy initialization of native shader objects.
    val shaderPool = remember { mutableListOf<RuntimeShader>() }
    val brushPool = remember { mutableListOf<ShaderBrush>() }

    val studentMap = remember(students) { students.associateBy { it.id.toLong() } }

    Canvas(modifier = modifier.fillMaxSize()) {
        // BOLT: Hoist invariant uniforms that don't change per-edge.
        val activeEdgesCount = minOf(edges.size, shaderPool.size)
        for (i in 0 until activeEdgesCount) {
            shaderPool[i].setFloatUniform("iResolution", size.width, size.height)
            shaderPool[i].setFloatUniform("iTime", time)
        }

        edges.forEachIndexed { index, edge ->
            val fromStudent = studentMap[edge.fromId]
            val toStudent = studentMap[edge.toId]
            if (fromStudent != null && toStudent != null) {
                // Calculate pixel-perfect centers for connection endpoints, accounting for scale and offset.
                val startX = (fromStudent.xPosition.value * canvasScale) + canvasOffset.x + (fromStudent.displayWidth.value.toPx() * canvasScale / 2f)
                val startY = (fromStudent.yPosition.value * canvasScale) + canvasOffset.y + (fromStudent.displayHeight.value.toPx() * canvasScale / 2f)
                val endX = (toStudent.xPosition.value * canvasScale) + canvasOffset.x + (toStudent.displayWidth.value.toPx() * canvasScale / 2f)
                val endY = (toStudent.yPosition.value * canvasScale) + canvasOffset.y + (toStudent.displayHeight.value.toPx() * canvasScale / 2f)

                if (index >= shaderPool.size) {
                    val s = RuntimeShader(GhostLatticeShader.NEURAL_LATTICE)
                    // BOLT: Ensure new shaders also get the invariant uniforms for the current frame.
                    s.setFloatUniform("iResolution", size.width, size.height)
                    s.setFloatUniform("iTime", time)
                    shaderPool.add(s)
                    brushPool.add(ShaderBrush(s))
                }
                val shader = shaderPool[index]
                val brush = brushPool[index]

                shader.setFloatUniform("iStartPos", startX, startY)
                shader.setFloatUniform("iEndPos", endX, endY)
                shader.setFloatUniform("iColor", edge.color.red, edge.color.green, edge.color.blue)
                shader.setFloatUniform("iStrength", edge.strength)
                shader.setFloatUniform("iType", edge.type.value)

                // Optimized drawing: only draw the bounding box of the connection line to reduce fragment shader overhead.
                val minX = minOf(startX, endX) - 50f
                val minY = minOf(startY, endY) - 50f
                val maxX = maxOf(startX, endX) + 50f
                val maxY = maxOf(startY, endY) + 50f
                drawRect(brush = brush, topLeft = Offset(minX, minY), size = androidx.compose.ui.geometry.Size(maxX - minX, maxY - minY))
            }
        }
    }
}
