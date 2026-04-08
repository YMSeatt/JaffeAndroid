package com.example.myapplication.labs.ghost.catalyst

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
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
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostCatalystLayer: Renders the behavioral chain reaction field.
 *
 * This layer uses high-performance AGSL shaders to visualize the "Kinetics" of the
 * classroom, including a global reaction field and individual ionic bonds between
 * students.
 *
 * ### Performance (BOLT):
 * - **Shader Pooling**: To maintain 60fps, the layer maintains a [bondShaderPool] and
 *   [bondBrushPool]. This avoids the O(R) object allocations during the high-frequency
 *   draw loop, where R is the number of active reactions.
 * - **Memoized Data**: The [engine] and [reactions] list are `remember`ed to avoid
 *   redundant calculations during recomposition.
 *
 * @param students The current list of students on the chart.
 * @param behaviorLogs Historical behavior logs for reaction analysis.
 * @param canvasScale The current zoom level of the seating chart.
 * @param canvasOffset The current pan offset of the seating chart.
 * @param isActive Master toggle for this experimental layer.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun GhostCatalystLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean
) {
    if (!isActive) return

    val engine = remember { GhostCatalystEngine() }
    val reactions = remember(behaviorLogs, students) {
        engine.calculateReactionsUI(students, behaviorLogs)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "GhostCatalyst")
    val iTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "iTime"
    )

    val reactionShader = remember { RuntimeShader(GhostCatalystShader.REACTION_FIELD) }
    val reactionBrush = remember(reactionShader) { ShaderBrush(reactionShader) }

    val reactionRate = remember(reactions) {
        (reactions.size.toFloat() / 20f).coerceIn(0.1f, 1.0f)
    }

    val studentMap = remember(students) { students.associateBy { it.id.toLong() } }

    // BOLT: Pool shaders and brushes to avoid O(R) allocations in the draw loop
    // while ensuring unique instances for correct uniform capturing per draw call.
    val bondShaderPool = remember { mutableListOf<RuntimeShader>() }
    val bondBrushPool = remember { mutableListOf<ShaderBrush>() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Draw Global Reaction Field
        reactionShader.setFloatUniform("iResolution", width, height)
        reactionShader.setFloatUniform("iTime", iTime)
        reactionShader.setFloatUniform("iColor", 0.0f, 1.0f, 0.8f) // Cyan/Cyan-Green
        reactionShader.setFloatUniform("iRate", reactionRate)

        drawRect(brush = reactionBrush)

        // 2. Draw Ionic Bonds between catalysts and reactants
        var bondIdx = 0
        reactions.forEach { reaction ->
            val catalyst = studentMap[reaction.catalystId] ?: return@forEach
            val reactant = studentMap[reaction.reactantId] ?: return@forEach

            val startX = (catalyst.xPosition.value * canvasScale) + canvasOffset.x
            val startY = (catalyst.yPosition.value * canvasScale) + canvasOffset.y
            val endX = (reactant.xPosition.value * canvasScale) + canvasOffset.x
            val endY = (reactant.yPosition.value * canvasScale) + canvasOffset.y

            if (bondIdx >= bondShaderPool.size) {
                val s = RuntimeShader(GhostCatalystShader.IONIC_BOND)
                bondShaderPool.add(s)
                bondBrushPool.add(ShaderBrush(s))
            }
            val shader = bondShaderPool[bondIdx]
            val brush = bondBrushPool[bondIdx]
            bondIdx++

            shader.setFloatUniform("iResolution", width, height)
            shader.setFloatUniform("iTime", iTime)
            shader.setFloatUniform("iColor", 0.9f, 0.1f, 0.5f) // Magenta
            shader.setFloatUniform("iIntensity", reaction.intensity)

            drawLine(
                brush = brush,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 4f * canvasScale
            )
        }
    }
}
