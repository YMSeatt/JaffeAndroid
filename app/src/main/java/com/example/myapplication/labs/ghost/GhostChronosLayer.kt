package com.example.myapplication.labs.ghost

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

/**
 * GhostChronosLayer: Renders the behavioral heatmap.
 */
@Composable
fun GhostChronosLayer(
    students: List<StudentUiItem>,
    events: List<BehaviorEvent>,
    canvasScale: Float,
    canvasOffset: Offset,
    modifier: Modifier = Modifier
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val infiniteTransition = rememberInfiniteTransition(label = "chronosPulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val heatmapGrid = remember(students, events) {
        GhostChronosEngine.calculateHeatmap(students, events)
    }

    val shader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RuntimeShader(GhostChronosShader.CHRONOS_HEATMAP)
        } else null
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && shader != null) {
            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iTime", time)
            shader.setFloatUniform("iIntensity", heatmapGrid)
            shader.setFloatUniform("iColorPositive", 0.2f, 0.8f, 0.4f)
            shader.setFloatUniform("iColorNegative", 0.9f, 0.2f, 0.2f)
            shader.setFloatUniform("iOffset", canvasOffset.x, canvasOffset.y)
            shader.setFloatUniform("iScale", canvasScale)

            drawRect(brush = ShaderBrush(shader))
        }
    }
}
