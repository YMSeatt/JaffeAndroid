package com.example.myapplication.labs.ghost.coral

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * GhostCoralLayer: Renders the Social Reef on the seating chart.
 *
 * This layer visualizes student collaboration as organic coral structures.
 * BOLT: Optimized for API 33+ with shader pooling and zero-allocation draw loop.
 */
@Composable
fun GhostCoralLayer(
    branches: List<GhostCoralEngine.CoralBranch>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val infiniteTransition = rememberInfiniteTransition(label = "coral_anim")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // BOLT: Hoist shader and brush to avoid per-frame allocations
    val coralShader = remember { RuntimeShader(GhostCoralShader.CORAL_SHADER) }
    val coralBrush = remember(coralShader) { ShaderBrush(coralShader) }
    val baseColor = Color(0xFF00BCD4) // Cyan 500

    Canvas(modifier = Modifier.fillMaxSize()) {
        withTransform({
            translate(canvasOffset.x, canvasOffset.y)
            scale(canvasScale, canvasScale, pivot = Offset.Zero)
        }) {
            // BOLT: Manual index-based loop for 60fps performance
            for (i in branches.indices) {
                val branch = branches[i]

                val dx = branch.x2 - branch.x1
                val dy = branch.y2 - branch.y1
                val length = sqrt(dx * dx + dy * dy)
                val angle = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat()

                // Branch thickness based on density
                val thickness = 40f + 60f * branch.density

                // Update shader uniforms
                coralShader.setFloatUniform("iResolution", length, thickness)
                coralShader.setFloatUniform("iTime", time)
                coralShader.setFloatUniform("iDensity", branch.density)
                coralShader.setFloatUniform("iVitality", branch.vitality)
                coralShader.setFloatUniform("iColor", baseColor.red, baseColor.green, baseColor.blue, baseColor.alpha)

                withTransform({
                    translate(branch.x1, branch.y1 - thickness / 2f)
                    rotate(angle, pivot = Offset(0f, thickness / 2f))
                }) {
                    drawRect(
                        brush = coralBrush,
                        size = Size(length, thickness)
                    )
                }
            }
        }
    }
}
