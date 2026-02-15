package com.example.myapplication.labs.ghost.vector

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
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.labs.ghost.GhostConfig
import com.example.myapplication.labs.ghost.lattice.GhostLatticeEngine
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostVectorLayer: Renders the social gravity vectors on the seating chart.
 *
 * This layer visualizes the invisible social forces acting on students. It combines data
 * from the [GhostLatticeEngine] and [GhostVectorEngine] to draw directional indicators
 * (using [GhostVectorShader]) for each student.
 *
 * @param students Current list of students on the chart.
 * @param behaviorLogs Historical logs used to calculate social links.
 * @param canvasScale Current zoom level.
 * @param canvasOffset Current pan position.
 */
@Composable
fun GhostVectorLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    canvasScale: Float,
    canvasOffset: Offset,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !GhostConfig.VECTOR_MODE_ENABLED) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val engine = remember { GhostVectorEngine() }
    val latticeEngine = remember { GhostLatticeEngine() }

    val infiniteTransition = rememberInfiniteTransition(label = "vectorTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Calculate vectors based on current student positions and lattice connections
    val vectors = remember(students, behaviorLogs) {
        val nodes = students.map {
            GhostLatticeEngine.LatticeNode(it.id.toLong(), it.xPosition.value, it.yPosition.value)
        }
        val edges = latticeEngine.computeLattice(nodes, behaviorLogs)
        engine.calculateVectors(nodes, edges)
    }

    val vectorShader = remember { RuntimeShader(GhostVectorShader.VECTOR_NEEDLE) }
    val studentMap = remember(students) { students.associateBy { it.id.toLong() } }

    Canvas(modifier = modifier.fillMaxSize()) {
        vectors.forEach { vector ->
            val student = studentMap[vector.studentId]
            // Only render if there's a significant social force
            if (student != null && vector.magnitude > 2.0f) {
                val centerX = (student.xPosition.value * canvasScale) + canvasOffset.x +
                              (student.displayWidth.value.toPx() * canvasScale / 2f)
                val centerY = (student.yPosition.value * canvasScale) + canvasOffset.y +
                              (student.displayHeight.value.toPx() * canvasScale / 2f)

                vectorShader.setFloatUniform("iResolution", size.width, size.height)
                vectorShader.setFloatUniform("iTime", time)
                vectorShader.setFloatUniform("iCenter", centerX, centerY)
                vectorShader.setFloatUniform("iAngle", vector.angle)
                vectorShader.setFloatUniform("iMagnitude", vector.magnitude)

                // Color shift based on force intensity: Cyan for low/mid, Magenta for high tension
                val color = if (vector.magnitude > 80f) {
                    Color(0xFFFF33CC) // Hot Pink / Magenta
                } else {
                    Color(0xFF00E5FF) // Bright Cyan
                }
                vectorShader.setFloatUniform("iColor", color.red, color.green, color.blue)

                // Define a localized drawing area for the vector needle
                val drawAreaSize = 500f * canvasScale
                drawRect(
                    brush = ShaderBrush(vectorShader),
                    topLeft = Offset(centerX - drawAreaSize / 2f, centerY - drawAreaSize / 2f),
                    size = androidx.compose.ui.geometry.Size(drawAreaSize, drawAreaSize)
                )
            }
        }
    }
}
