package com.example.myapplication.labs.ghost

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostFutureLayer: Renders the simulated "Future" behavior events.
 *
 * This layer applies the [GhostFutureShader] and draws glowing "Ghost"
 * indicators for predicted events over the seating chart.
 */
@Composable
fun GhostFutureLayer(
    students: List<StudentUiItem>,
    historicalLogs: List<BehaviorEvent>,
    isFutureActive: Boolean,
    canvasScale: Float,
    canvasOffset: Offset,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !GhostConfig.FUTURE_MODE_ENABLED || !isFutureActive) return

    val infiniteTransition = rememberInfiniteTransition(label = "futurePulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val simulatedEvents = remember(students, historicalLogs) {
        GhostFutureEngine.generateFutureEvents(students, historicalLogs, hoursIntoFuture = 2)
    }

    val futureShader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RuntimeShader(GhostFutureShader.TEMPORAL_WARP)
        } else null
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && futureShader != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                futureShader.setFloatUniform("iResolution", size.width, size.height)
                futureShader.setFloatUniform("iTime", time)
                futureShader.setFloatUniform("iIntensity", 1.0f)

                drawRect(brush = ShaderBrush(futureShader))

                // Draw Predicted Events as floating nodes
                simulatedEvents.forEach { event ->
                    val student = students.find { it.id.toLong() == event.studentId } ?: return@forEach

                    val centerX = (student.xPosition.value * canvasScale) + canvasOffset.x + (student.displayWidth.value.toPx() * canvasScale / 2f)
                    val centerY = (student.yPosition.value * canvasScale) + canvasOffset.y + (student.displayHeight.value.toPx() * canvasScale / 2f)

                    val isNegative = event.type.contains("Simulated") &&
                                   (event.type.contains("Disruptive") || event.type.contains("Conflict"))

                    val color = if (isNegative) Color.Magenta else Color.Cyan
                    val alpha = 0.4f + 0.2f * kotlin.math.sin(time * 5f + event.timestamp.toFloat())

                    drawCircle(
                        color = color.copy(alpha = alpha),
                        radius = 12.dp.toPx() * canvasScale,
                        center = Offset(centerX, centerY + 40.dp.toPx() * canvasScale * kotlin.math.sin(time + event.timestamp.toFloat()))
                    )
                }
            }
        }
    }
}
