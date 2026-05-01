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
import com.example.myapplication.labs.ghost.GhostConfig
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostVectorLayer: Renders the social gravity vectors on the seating chart.
 *
 * This layer visualizes the invisible social forces acting on students. It receives
 * pre-calculated vectors from the background pipeline and draws directional indicators
 * (using [GhostVectorShader]) for each student.
 *
 * BOLT: Optimized to receive [vectors] from background pipeline and use manual index loops
 * in the draw pass to ensure 60fps stability.
 *
 * @param students Current list of students on the chart.
 * @param vectors Pre-calculated social force vectors.
 * @param canvasScale Current zoom level.
 * @param canvasOffset Current pan position.
 */
@Composable
fun GhostVectorLayer(
    students: List<StudentUiItem>,
    vectors: List<GhostVectorEngine.SocialVector>,
    canvasScale: Float,
    canvasOffset: Offset,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !GhostConfig.VECTOR_MODE_ENABLED) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val infiniteTransition = rememberInfiniteTransition(label = "vectorTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val studentMap = remember(students) { students.associateBy { it.id.toLong() } }

    // BOLT: Pool shaders and brushes to avoid O(N) allocations in the draw loop
    // while ensuring unique instances for correct uniform capturing per draw call.
    val vectorShaderPool = remember { mutableListOf<RuntimeShader>() }
    val vectorBrushPool = remember { mutableListOf<ShaderBrush>() }

    Canvas(modifier = modifier.fillMaxSize()) {
        var vectorIdx = 0
        val vSize = vectors.size
        for (i in 0 until vSize) {
            val vector = vectors[i]
            val student = studentMap[vector.studentId]
            // Only render if there's a significant social force
            if (student != null && vector.magnitude > 2.0f) {
                val centerX = (student.xPosition.value * canvasScale) + canvasOffset.x +
                              (student.displayWidth.value.toPx() * canvasScale / 2f)
                val centerY = (student.yPosition.value * canvasScale) + canvasOffset.y +
                              (student.displayHeight.value.toPx() * canvasScale / 2f)

                if (vectorIdx >= vectorShaderPool.size) {
                    val s = RuntimeShader(GhostVectorShader.VECTOR_NEEDLE)
                    vectorShaderPool.add(s)
                    vectorBrushPool.add(ShaderBrush(s))
                }
                val shader = vectorShaderPool[vectorIdx]
                val brush = vectorBrushPool[vectorIdx]
                vectorIdx++

                shader.setFloatUniform("iResolution", size.width, size.height)
                shader.setFloatUniform("iTime", time)
                shader.setFloatUniform("iCenter", centerX, centerY)
                shader.setFloatUniform("iAngle", vector.angle)
                shader.setFloatUniform("iMagnitude", vector.magnitude)

                // Color shift based on force intensity: Cyan for low/mid, Magenta for high tension
                val color = if (vector.magnitude > 80f) {
                    Color(0xFFFF33CC) // Hot Pink / Magenta
                } else {
                    Color(0xFF00E5FF) // Bright Cyan
                }
                shader.setFloatUniform("iColor", color.red, color.green, color.blue)

                // Define a localized drawing area for the vector needle.
                // 500f is calibrated to provide enough space for the animated "Neural Flow" trail
                // without clipping the needle even at high magnitudes.
                val drawAreaSize = 500f * canvasScale
                drawRect(
                    brush = brush,
                    topLeft = Offset(centerX - drawAreaSize / 2f, centerY - drawAreaSize / 2f),
                    size = androidx.compose.ui.geometry.Size(drawAreaSize, drawAreaSize)
                )
            }
        }
    }
}
