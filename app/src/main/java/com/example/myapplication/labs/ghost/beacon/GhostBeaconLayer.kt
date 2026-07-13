package com.example.myapplication.labs.ghost.beacon

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.labs.ghost.util.GhostHapticManager

/**
 * GhostBeaconLayer: Renders the volumetric attention beacon.
 */
@Composable
fun GhostBeaconLayer(
    targetPosition: Offset,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean,
    onAnimationComplete: () -> Unit = {}
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val hapticManager = remember { GhostHapticManager(context) }

    // Intensity animation: fade in, stay, fade out
    val animatedIntensity by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(1000, easing = LinearEasing),
        label = "beaconIntensity",
        finishedListener = { if (!isActive) onAnimationComplete() }
    )

    // Time animation for shader effects
    val infiniteTransition = rememberInfiniteTransition(label = "beaconTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Trigger haptic feedback when activated
    LaunchedEffect(isActive) {
        if (isActive) {
            hapticManager.perform(GhostHapticManager.Pattern.SUCCESS)
        }
    }

    if (animatedIntensity > 0f) {
        val shader = remember { RuntimeShader(GhostBeaconShader.BEACON_EFFECT) }
        val brush = remember(shader) { ShaderBrush(shader) }

        Canvas(modifier = Modifier.fillMaxSize()) {
            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iTime", time)
            shader.setFloatUniform("iTarget", targetPosition.x, targetPosition.y)
            shader.setFloatUniform("iIntensity", animatedIntensity)
            shader.setFloatUniform("iCanvasOffset", canvasOffset.x, canvasOffset.y)
            shader.setFloatUniform("iCanvasScale", canvasScale)

            drawRect(brush = brush)
        }
    }
}
