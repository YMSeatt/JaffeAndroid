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
    val bondShader = remember { RuntimeShader(GhostCatalystShader.IONIC_BOND) }

    val reactionRate = remember(reactions) {
        (reactions.size.toFloat() / 20f).coerceIn(0.1f, 1.0f)
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Draw Global Reaction Field
        reactionShader.setFloatUniform("iResolution", width, height)
        reactionShader.setFloatUniform("iTime", iTime)
        reactionShader.setFloatUniform("iColor", 0.0f, 1.0f, 0.8f) // Cyan/Cyan-Green
        reactionShader.setFloatUniform("iRate", reactionRate)

        drawRect(brush = ShaderBrush(reactionShader))

        // 2. Draw Ionic Bonds between catalysts and reactants
        reactions.forEach { reaction ->
            val catalyst = students.find { it.id.toLong() == reaction.catalystId } ?: return@forEach
            val reactant = students.find { it.id.toLong() == reaction.reactantId } ?: return@forEach

            val startX = (catalyst.xPosition.value * canvasScale) + canvasOffset.x
            val startY = (catalyst.yPosition.value * canvasScale) + canvasOffset.y
            val endX = (reactant.xPosition.value * canvasScale) + canvasOffset.x
            val endY = (reactant.yPosition.value * canvasScale) + canvasOffset.y

            bondShader.setFloatUniform("iResolution", width, height)
            bondShader.setFloatUniform("iTime", iTime)
            bondShader.setFloatUniform("iColor", 0.9f, 0.1f, 0.5f) // Magenta
            bondShader.setFloatUniform("iIntensity", reaction.intensity)

            drawLine(
                brush = ShaderBrush(bondShader),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 4f * canvasScale
            )
        }
    }
}
