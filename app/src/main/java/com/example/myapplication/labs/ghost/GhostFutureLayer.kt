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
    futureEvents: List<BehaviorEvent>,
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

    val futureShader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RuntimeShader(GhostFutureShader.TEMPORAL_WARP)
        } else null
    }
    val futureBrush = remember(futureShader) { futureShader?.let { ShaderBrush(it) } }

    // BOLT: Pre-index students for O(1) lookup in the Canvas draw pass.
    val studentMap = remember(students) { students.associateBy { it.id.toLong() } }

    // BOLT: Hoist constants and pre-calculate render metadata for simulated events
    // to eliminate per-frame string parsing and unit conversions in the Canvas.
    data class EventRenderData(
        val studentId: Long,
        val color: Color,
        val timeSeed: Float
    )
    val eventMetadata = remember(futureEvents) {
        val list = ArrayList<EventRenderData>(futureEvents.size)
        for (i in futureEvents.indices) {
            val event = futureEvents[i]
            val type = event.type
            val isNegative = type.contains("Disruptive") || type.contains("Conflict")
            list.add(EventRenderData(
                studentId = event.studentId,
                color = if (isNegative) Color.Magenta else Color.Cyan,
                timeSeed = event.timestamp.toFloat()
            ))
        }
        list
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && futureShader != null && futureBrush != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                futureShader.setFloatUniform("iResolution", size.width, size.height)
                futureShader.setFloatUniform("iTime", time)
                futureShader.setFloatUniform("iIntensity", 1.0f)

                drawRect(brush = futureBrush)

                // BOLT: Hoist DP conversions and reuse scaled metrics
                val circleRadius = 12.dp.toPx() * canvasScale
                val floatAmplitude = 40.dp.toPx() * canvasScale

                // BOLT: Optimized drawing loop using manual indices and O(1) student lookups.
                for (i in eventMetadata.indices) {
                    val meta = eventMetadata[i]
                    val student = studentMap[meta.studentId] ?: continue

                    // Map student coordinates to screen space
                    val centerX = (student.xPosition.value * canvasScale) + canvasOffset.x + (student.displayWidth.value.toPx() * canvasScale / 2f)
                    val centerY = (student.yPosition.value * canvasScale) + canvasOffset.y + (student.displayHeight.value.toPx() * canvasScale / 2f)

                    val alpha = 0.4f + 0.2f * kotlin.math.sin(time * 5f + meta.timeSeed)
                    val floatingY = centerY + floatAmplitude * kotlin.math.sin(time + meta.timeSeed)

                    drawCircle(
                        color = meta.color.copy(alpha = alpha),
                        radius = circleRadius,
                        center = Offset(centerX, floatingY)
                    )
                }
            }
        }
    }
}
