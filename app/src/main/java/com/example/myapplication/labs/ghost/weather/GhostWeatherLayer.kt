package com.example.myapplication.labs.ghost.weather

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
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import com.example.myapplication.labs.ghost.GhostConfig
import com.example.myapplication.ui.model.StudentUiItem
import kotlinx.coroutines.delay

/**
 * GhostWeatherLayer: A data-driven atmospheric weather visualization layer.
 *
 * This layer renders neural precipitation (rain/snow) and academic lightning
 * over the seating chart.
 */
@Composable
fun GhostWeatherLayer(
    engine: GhostWeatherEngine,
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    quizLogs: List<QuizLog>,
    homeworkLogs: List<HomeworkLog>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "weatherTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(100000, easing = LinearEasing)),
        label = "time"
    )

    // Physics Update Loop (60fps)
    LaunchedEffect(isActive, students, behaviorLogs, quizLogs, homeworkLogs) {
        while (isActive) {
            engine.update(students, behaviorLogs, quizLogs, homeworkLogs)
            delay(16)
        }
    }

    val atmosphereShader = remember { RuntimeShader(GhostWeatherShader.WEATHER_ATMOSPHERE) }
    val atmosphereBrush = remember(atmosphereShader) { ShaderBrush(atmosphereShader) }

    val lightningShader = remember { RuntimeShader(GhostWeatherShader.WEATHER_LIGHTNING) }
    val lightningBrush = remember(lightningShader) { ShaderBrush(lightningShader) }

    Canvas(modifier = modifier.fillMaxSize()) {
        // 1. Draw Atmosphere
        atmosphereShader.setFloatUniform("iResolution", size.width, size.height)
        atmosphereShader.setFloatUniform("iTime", time)
        atmosphereShader.setFloatUniform("iIntensity", engine.intensity)
        atmosphereShader.setFloatUniform("iLightningAlpha", engine.lightningAlpha)
        atmosphereShader.setFloatUniform("iLightningX", (engine.lightningX * canvasScale) + canvasOffset.x)
        drawRect(brush = atmosphereBrush)

        // 2. Draw Lightning Bolt
        if (engine.lightningAlpha > 0.1f) {
            lightningShader.setFloatUniform("iResolution", size.width, size.height)
            lightningShader.setFloatUniform("iTime", time)
            lightningShader.setFloatUniform("iAlpha", engine.lightningAlpha)
            lightningShader.setFloatUniform("iX", (engine.lightningX * canvasScale) + canvasOffset.x)
            drawRect(brush = lightningBrush)
        }

        // 3. Draw Particles (Rain/Snow)
        for (i in 0 until GhostWeatherEngine.MAX_PARTICLES) {
            if (engine.partActive[i]) {
                val sx = (engine.partX[i] * canvasScale) + canvasOffset.x
                val sy = (engine.partY[i] * canvasScale) + canvasOffset.y

                if (engine.partType[i] == 0) {
                    // Rain
                    drawLine(
                        color = Color.Cyan.copy(alpha = 0.4f * engine.intensity),
                        start = Offset(sx, sy),
                        end = Offset(sx + engine.windForce * 0.1f, sy + 20f * canvasScale),
                        strokeWidth = 2f * canvasScale
                    )
                } else {
                    // Snow
                    drawCircle(
                        color = Color.White.copy(alpha = 0.6f * engine.intensity),
                        center = Offset(sx, sy),
                        radius = 3f * canvasScale
                    )
                }
            }
        }
    }
}
