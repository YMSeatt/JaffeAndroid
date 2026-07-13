package com.example.myapplication.labs.ghost.sonar

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
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.labs.ghost.util.GhostHapticManager
import kotlin.math.abs

/**
 * GhostSonarLayer: Visualizes the Spatial Engagement Discovery wave.
 *
 * This layer renders an expanding AGSL ripple originating from a target point.
 * As the ripple intersects with "quiet" students, it triggers haptic pings.
 *
 * @param origin The center point of the sonar sweep.
 * @param quietStudentIds The set of students identified as "quiet" by the engine.
 * @param students The full list of students to check for wave intersection.
 * @param isActive Whether the sonar sweep is currently active.
 * @param canvasScale Current zoom level.
 * @param canvasOffset Current pan offset.
 * @param onFinished Callback when the sweep animation completes.
 */
@Composable
fun GhostSonarLayer(
    origin: Offset,
    quietStudentIds: Set<Long>,
    students: List<StudentUiItem>,
    isActive: Boolean,
    canvasScale: Float,
    canvasOffset: Offset,
    onFinished: () -> Unit
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val hapticManager = remember { GhostHapticManager(context) }

    val animatableRadius = remember { Animatable(0f) }
    val maxRadius = 4000f // Full canvas coverage

    LaunchedEffect(isActive) {
        if (isActive) {
            animatableRadius.snapTo(0f)
            animatableRadius.animateTo(
                targetValue = maxRadius,
                animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
            )
            onFinished()
        }
    }

    val currentRadius = animatableRadius.value
    val shader = remember { RuntimeShader(GhostSonarShader.SONAR_WAVE) }
    val brush = remember(shader) { ShaderBrush(shader) }

    // Haptic Intersection Logic
    // BOLT: Manual index loop to avoid iterator churn during animation.
    // We use a local hitIds set to ensure each "quiet" student only triggers one haptic ping
    // per sonar sweep as the wavefront passes their logical center.
    val hitIds = remember { mutableSetOf<Long>() }
    LaunchedEffect(currentRadius) {
        if (currentRadius <= 0f) hitIds.clear()

        for (i in 0 until students.size) {
            val student = students[i]
            if (quietStudentIds.contains(student.id.toLong()) && !hitIds.contains(student.id.toLong())) {
                val sx = (student.xPosition.value * canvasScale) + canvasOffset.x
                val sy = (student.yPosition.value * canvasScale) + canvasOffset.y

                val dx = sx - origin.x
                val dy = sy - origin.y
                val dist = kotlin.math.sqrt(dx * dx + dy * dy)

                // Trigger ping when wave hits student center
                if (abs(dist - currentRadius) < 50f) {
                    hapticManager.perform(GhostHapticManager.Pattern.UI_CLICK)
                    hitIds.add(student.id.toLong())
                }
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iCenter", origin.x, origin.y)
        shader.setFloatUniform("iRadius", currentRadius)
        shader.setFloatUniform("iWidth", 40f * canvasScale)
        shader.setFloatUniform("iIntensity", 0.8f)
        shader.setColorUniform("iColor", android.graphics.Color.CYAN)

        drawRect(brush = brush)
    }
}
