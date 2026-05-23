package com.example.myapplication.labs.ghost.carbon

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostCarbonLayer: Renders the "Behavioral Twin" connections.
 *
 * This layer uses the [GhostCarbonShader] to draw pulsing bridges between
 * students identified as twins by the [GhostCarbonEngine].
 */
@Composable
fun GhostCarbonLayer(
    students: List<StudentUiItem>,
    twins: List<GhostCarbonEngine.CarbonTwin>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean
) {
    if (!isActive || twins.isEmpty() || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val infiniteTransition = rememberInfiniteTransition(label = "carbon_resonance")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val shaders = remember(twins.size) {
        twins.map { RuntimeShader(GhostCarbonShader.RESONANCE_BRIDGE) }
    }

    val studentMap = remember(students) { students.associateBy { it.id.toLong() } }

    Canvas(modifier = Modifier.fillMaxSize()) {
        twins.forEachIndexed { index, twin ->
            val sA = studentMap[twin.studentA] ?: return@forEachIndexed
            val sB = studentMap[twin.studentB] ?: return@forEachIndexed

            val shader = shaders.getOrNull(index) ?: return@forEachIndexed
            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iTime", time)

            // BOLT: Calculate screen-space coordinates for the shader to ensure bridges align
            // during pan/zoom operations.
            val posAx = (sA.xPosition.value * canvasScale) + canvasOffset.x + (sA.displayWidth.value.toPx() * canvasScale / 2f)
            val posAy = (sA.yPosition.value * canvasScale) + canvasOffset.y + (sA.displayHeight.value.toPx() * canvasScale / 2f)
            val posBx = (sB.xPosition.value * canvasScale) + canvasOffset.x + (sB.displayWidth.value.toPx() * canvasScale / 2f)
            val posBy = (sB.yPosition.value * canvasScale) + canvasOffset.y + (sB.displayHeight.value.toPx() * canvasScale / 2f)

            shader.setFloatUniform("iPointA", posAx, posAy)
            shader.setFloatUniform("iPointB", posBx, posBy)
            shader.setFloatUniform("iStrength", twin.similarity)

            drawRect(brush = ShaderBrush(shader))
        }
    }
}
