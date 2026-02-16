package com.example.myapplication.labs.ghost

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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas

/**
 * GhostPortalLayer: A visual experiment exploring the future of "Inter-app Data Teleportation."
 *
 * This layer renders a swirling AGSL wormhole shader that activates during Drag & Drop operations.
 * It provides a natural, high-fidelity visual cue when student data is being moved or shared.
 *
 * **Android 15 Integration:**
 * This component is designed to work in tandem with `Modifier.dragAndDropSource` and
 * `Modifier.dragAndDropTarget`, simulating a spatial portal for data transfer.
 *
 * @param isDraggingActive A reactive boolean state indicating if an active drag operation
 *   (e.g., student selection) is currently in progress.
 * @param portalPosition The pixel coordinate (Offset) where the portal should be centered.
 *   This typically follows the user's touch point or resides at a fixed drop target.
 * @param color The base theme color for the portal's energy field.
 */
@Composable
fun GhostPortalLayer(
    isDraggingActive: Boolean,
    portalPosition: Offset,
    color: Color = Color.Cyan,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !GhostConfig.PORTAL_MODE_ENABLED) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val infiniteTransition = rememberInfiniteTransition(label = "portalTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(50000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val intensity by animateFloatAsState(
        targetValue = if (isDraggingActive) 1.0f else 0.0f,
        animationSpec = tween(500),
        label = "intensity"
    )

    if (intensity <= 0.01f) return

    val shader = remember { RuntimeShader(GhostPortalShader.PORTAL_WORMHOLE) }

    Canvas(modifier = modifier.fillMaxSize()) {
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        shader.setFloatUniform("iPortalPos", portalPosition.x, portalPosition.y)
        shader.setFloatUniform("iRadius", 0.15f) // Normalized radius relative to height
        shader.setFloatUniform("iColor", color.red, color.green, color.blue)
        shader.setFloatUniform("iIntensity", intensity)

        drawRect(brush = ShaderBrush(shader))
    }
}
