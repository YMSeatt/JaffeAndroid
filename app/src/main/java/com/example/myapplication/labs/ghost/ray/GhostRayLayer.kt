package com.example.myapplication.labs.ghost.ray

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.geometry.Offset
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostRayLayer: A real-time volumetric visualization of the teacher's directional pointer.
 *
 * This layer renders a pulsating, data-refractive AGSL beam that originates from a
 * virtual "source" (typically the device itself) and highlights student nodes.
 */
@Composable
fun GhostRayLayer(
    engine: GhostRayEngine,
    students: List<StudentUiItem>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || !isActive) return

    val target by engine.rayTarget.collectAsState()
    val intersectedId by engine.intersectedStudentId.collectAsState()

    // Smoothly animate time uniform for procedural effects
    val infiniteTransition = rememberInfiniteTransition(label = "rayPulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val shader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RuntimeShader(GhostRayShader.NEURAL_BEAM)
        } else {
            null
        }
    }
    val brush = remember(shader) { shader?.let { ShaderBrush(it) } }

    // Update intersection logic in the background when the target changes
    LaunchedEffect(target, students, canvasScale, canvasOffset) {
        if (isActive) {
            engine.updateIntersection(students, canvasScale, canvasOffset)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (shader != null && brush != null && target != null) {
            try {
                shader.setFloatUniform("iResolution", size.width, size.height)
                shader.setFloatUniform("iTime", time)

                // SOURCE PLACEMENT:
                // The ray source is centered at the bottom of the screen (size.width / 2f, size.height),
                // simulating the beam originating from the teacher's physical device/tablet.
                shader.setFloatUniform("iSource", size.width / 2f, size.height)

                // TARGET MAPPING:
                // The target is provided in pixel coordinates already mapped by the engine.
                shader.setFloatUniform("iTarget", target!!.x, target!!.y)
                shader.setFloatUniform("iIntensity", 1.0f)

                // STATE-DRIVEN COLOR:
                // The beam color shifts based on whether it is currently intersecting a student node.
                val color = if (intersectedId != null) {
                    Color.Magenta // At-risk / High-energy focus
                } else {
                    Color.Cyan // Stable observation
                }

                shader.setFloatUniform("iColor", color.red, color.green, color.blue)

                // DRAWING:
                // We draw a full-screen rectangle using the ShaderBrush. The shader itself
                // handles the "volumetric" beam shaping based on iSource and iTarget.
                drawRect(brush = brush)
            } catch (e: Exception) {
                // Ignore shader runtime errors
            }
        }
    }
}
