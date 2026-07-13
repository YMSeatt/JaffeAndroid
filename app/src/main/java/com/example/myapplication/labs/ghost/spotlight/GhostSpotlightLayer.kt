package com.example.myapplication.labs.ghost.spotlight

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostSpotlightLayer: A Composable that renders the Ghost Spotlight effect.
 *
 * It uses the [GhostSpotlightShader] to create a focused spotlight on a specific student.
 * The rest of the classroom is dimmed to minimize cognitive load.
 *
 * @param targetStudent The student to spotlight. If null, the layer is not rendered.
 * @param isActive Whether the spotlight feature is globally enabled.
 * @param canvasScale The current zoom level.
 * @param canvasOffset The current pan position.
 */
@Composable
fun GhostSpotlightLayer(
    targetStudent: StudentUiItem?,
    isActive: Boolean,
    canvasScale: Float,
    canvasOffset: Offset,
    modifier: Modifier = Modifier
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val infiniteTransition = rememberInfiniteTransition(label = "spotlightPulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val targetAlpha by animateFloatAsState(
        targetValue = if (targetStudent != null) 0.7f else 0f,
        animationSpec = tween(500),
        label = "alpha"
    )

    if (targetAlpha <= 0f && targetStudent == null) return

    val shader = remember { RuntimeShader(GhostSpotlightShader.SPOTLIGHT) }
    val brush = remember(shader) { ShaderBrush(shader) }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (targetStudent != null) {
            val studentX = (targetStudent.xPosition.value * canvasScale) + canvasOffset.x
            val studentY = (targetStudent.yPosition.value * canvasScale) + canvasOffset.y
            val studentW = targetStudent.displayWidth.value.toPx() * canvasScale
            val studentH = targetStudent.displayHeight.value.toPx() * canvasScale

            val centerX = studentX + (studentW / 2f)
            val centerY = studentY + (studentH / 2f)
            val baseRadius = maxOf(studentW, studentH) * 0.8f + pulse

            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iCenter", centerX, centerY)
            shader.setFloatUniform("iRadius", baseRadius)
            shader.setFloatUniform("iSoftness", 100f * canvasScale)
            shader.setFloatUniform("iIntensity", targetAlpha)
            shader.setColorUniform("iColor", android.graphics.Color.BLACK)

            drawRect(brush = brush)
        }
    }
}
