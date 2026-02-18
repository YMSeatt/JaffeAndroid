package com.example.myapplication.labs.ghost

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.ui.model.StudentUiItem
import kotlinx.coroutines.delay
import kotlin.math.sqrt

/**
 * GhostSingularityLayer: Renders the interactive "Data Sink" overlay.
 *
 * This layer uses the [GhostSingularityShader] to visualize a black hole
 * and interacts with [GhostSingularityEngine] to provide haptic feedback.
 * It monitors student icon positions to simulate gravitational capture.
 */
@Composable
fun GhostSingularityLayer(
    students: List<StudentUiItem>,
    isSingularityActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !isSingularityActive) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val engine = remember { GhostSingularityEngine(context) }

    // Animate the singularity's pulse and rotation
    val infiniteTransition = rememberInfiniteTransition(label = "singularityTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    var singularityPos by remember { mutableStateOf(Offset.Zero) }
    val radius = 60f // Base radius of the event horizon

    val shader = remember { RuntimeShader(GhostSingularityShader.GRAVITATIONAL_LENSING) }

    // Haptic Feedback Loop: Monitor student proximity to the singularity
    LaunchedEffect(students, singularityPos, isSingularityActive) {
        if (!isSingularityActive || singularityPos == Offset.Zero) return@LaunchedEffect

        while (true) {
            var maxPull = 0f
            students.forEach { student ->
                // Calculate pull based on center of student icon
                val studentCenter = Offset(
                    student.xPosition.value + student.displayWidth.value.value / 2f,
                    student.yPosition.value + student.displayHeight.value.value / 2f
                )
                val pull = engine.calculatePull(studentCenter, singularityPos, radius)
                if (pull > maxPull) maxPull = pull
            }

            if (maxPull > 0.1f) {
                engine.triggerCollapseHaptic(maxPull)
            }

            // Pulse frequency based on proximity
            val delayMillis = (1000 - (maxPull * 800)).toLong().coerceAtLeast(100)
            delay(delayMillis)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                if (singularityPos == Offset.Zero) {
                    singularityPos = Offset(size.width * 0.9f, size.height * 0.9f)
                }
            }
    ) {
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        shader.setFloatUniform("iSingularityPos", singularityPos.x, singularityPos.y)
        shader.setFloatUniform("iRadius", radius)

        // Intensity pulses slightly
        val intensity = 1.0f + (0.2f * (1.0f + kotlin.math.sin(time * 2.0f)))
        shader.setFloatUniform("iIntensity", intensity)

        drawRect(brush = ShaderBrush(shader))
    }
}
