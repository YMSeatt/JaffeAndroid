package com.example.myapplication.labs.ghost

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog

/**
 * GhostAuroraLayer: A futuristic background layer that visualizes "Classroom Climate"
 * using a flowing aurora AGSL shader.
 *
 * @param behaviorLogs Current list of behavior events for climate analysis.
 * @param quizLogs Current list of quiz logs.
 * @param homeworkLogs Current list of homework logs.
 * @param modifier Standard Compose modifier.
 */
@Composable
fun GhostAuroraLayer(
    behaviorLogs: List<BehaviorEvent>,
    quizLogs: List<QuizLog>,
    homeworkLogs: List<HomeworkLog>,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !GhostConfig.AURORA_MODE_ENABLED) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val params = remember(behaviorLogs, quizLogs, homeworkLogs) {
        GhostAuroraEngine.calculateAuroraParams(behaviorLogs, quizLogs, homeworkLogs)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "auroraTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val shader = remember { RuntimeShader(GhostAuroraShader.CLASSROOM_AURORA) }

    Canvas(modifier = modifier.fillMaxSize()) {
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        shader.setFloatUniform("iIntensity", params.intensity)
        shader.setFloatUniform(
            "iColorPrimary",
            params.colorPrimary.first,
            params.colorPrimary.second,
            params.colorPrimary.third
        )
        shader.setFloatUniform(
            "iColorSecondary",
            params.colorSecondary.first,
            params.colorSecondary.second,
            params.colorSecondary.third
        )
        shader.setFloatUniform("iSpeed", params.speed)

        drawRect(brush = ShaderBrush(shader))
    }
}
