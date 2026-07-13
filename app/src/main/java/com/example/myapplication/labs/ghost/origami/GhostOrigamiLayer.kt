package com.example.myapplication.labs.ghost.origami

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer

/**
 * GhostOrigamiLayer: Implements the 3D folding UI transition.
 *
 * This layer wraps its content and applies a "book-fold" or "origami" transformation
 * based on the state from [GhostOrigamiEngine].
 *
 * BOLT Optimization:
 * - Uses `graphicsLayer` for hardware-accelerated 3D rotations.
 * - Shaders are hoisted to prevent per-frame allocation.
 */
@Composable
fun GhostOrigamiLayer(
    engine: GhostOrigamiEngine,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val foldProgress by engine.foldProgress.collectAsState()
    val animatedProgress by animateFloatAsState(
        targetValue = foldProgress,
        animationSpec = tween(durationMillis = 800),
        label = "foldAnimation"
    )

    // Folding Parameters
    val foldAxis = 0.5f // Fold down the middle
    val maxRotation = -180f // Full flip

    Box(modifier = modifier.fillMaxSize()) {
        // 1. The "Backside" material (only visible when folding)
        if (animatedProgress > 0.01f) {
            BacksideMaterial(progress = animatedProgress)
        }

        // 2. The Main Content with 3D folding transformation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Set the transform origin to the fold axis (center-left for a book fold)
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)

                    // Rotate the "page" around the Y axis
                    rotationY = animatedProgress * maxRotation

                    // Perspective adjustment
                    cameraDistance = 12f * density
                }
        ) {
            content()

            // 3. The "Crease" shadow overlay
            if (animatedProgress > 0.01f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                CreaseShadow(progress = animatedProgress, axis = foldAxis)
            }
        }
    }
}

@Composable
private fun BacksideMaterial(progress: Float) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = Color(0xFF0A192F).copy(alpha = progress))
        }
        return
    }

    val shader = remember { RuntimeShader(GhostOrigamiShader.BACKSIDE_MATERIAL) }
    val brush = remember(shader) { ShaderBrush(shader) }

    Canvas(modifier = Modifier.fillMaxSize()) {
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", progress) // Use progress as a pseudo-time
        drawRect(brush = brush, alpha = progress)
    }
}

@Composable
private fun CreaseShadow(progress: Float, axis: Float) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val shader = remember { RuntimeShader(GhostOrigamiShader.PAPER_CREASE) }
    val brush = remember(shader) { ShaderBrush(shader) }

    Canvas(modifier = Modifier.fillMaxSize()) {
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iFoldProgress", progress)
        shader.setFloatUniform("iFoldAxis", axis)

        // Use multiply-like blending by drawing with the shadow shader
        drawRect(brush = brush, alpha = progress * 0.5f)
    }
}
