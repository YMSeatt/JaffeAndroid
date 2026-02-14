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

@Composable
fun GhostLatticeLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    canvasScale: Float,
    canvasOffset: Offset,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !GhostConfig.LATTICE_MODE_ENABLED) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val engine = remember { GhostLatticeEngine() }
    val infiniteTransition = rememberInfiniteTransition(label = "latticeTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 100f,
        animationSpec = infiniteRepeatable(animation = tween(100000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "time"
    )

    val edges = remember(students, behaviorLogs) {
        val nodes = students.map { GhostLatticeEngine.LatticeNode(it.id.toLong(), it.xPosition.value, it.yPosition.value) }
        engine.computeLattice(nodes, behaviorLogs)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        edges.forEach { edge ->
            val fromStudent = students.find { it.id.toLong() == edge.fromId }
            val toStudent = students.find { it.id.toLong() == edge.toId }
            if (fromStudent != null && toStudent != null) {
                val startX = (fromStudent.xPosition.value * canvasScale) + canvasOffset.x + (fromStudent.displayWidth.value.toPx() * canvasScale / 2f)
                val startY = (fromStudent.yPosition.value * canvasScale) + canvasOffset.y + (fromStudent.displayHeight.value.toPx() * canvasScale / 2f)
                val endX = (toStudent.xPosition.value * canvasScale) + canvasOffset.x + (toStudent.displayWidth.value.toPx() * canvasScale / 2f)
                val endY = (toStudent.yPosition.value * canvasScale) + canvasOffset.y + (toStudent.displayHeight.value.toPx() * canvasScale / 2f)

                val shader = RuntimeShader(GhostLatticeShader.NEURAL_LATTICE)
                shader.setFloatUniform("iResolution", size.width, size.height)
                shader.setFloatUniform("iTime", time)
                shader.setFloatUniform("iStartPos", startX, startY)
                shader.setFloatUniform("iEndPos", endX, endY)
                shader.setFloatUniform("iColor", edge.color.red, edge.color.green, edge.color.blue)
                shader.setFloatUniform("iStrength", edge.strength)
                shader.setFloatUniform("iType", edge.type.value)

                val minX = minOf(startX, endX) - 50; val minY = minOf(startY, endY) - 50
                val maxX = maxOf(startX, endX) + 50; val maxY = maxOf(startY, endY) + 50
                drawRect(brush = ShaderBrush(shader), topLeft = Offset(minX, minY), size = androidx.compose.ui.geometry.Size(maxX - minX, maxY - minY))
            }
        }
    }
}
