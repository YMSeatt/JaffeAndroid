package com.example.myapplication.labs.ghost.snapshot

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

/**
 * GhostSnapshotLayer: Provides visual feedback for the snapshot process.
 *
 * This layer implements a "Shutter Flash" effect using a simple white overlay
 * with a quick alpha animation. This provides the user with an intuitive
 * confirmation that a snapshot has been captured.
 */
@Composable
fun GhostSnapshotLayer(
    trigger: Boolean,
    onAnimationComplete: () -> Unit
) {
    var isFlashing by remember { mutableStateOf(false) }

    LaunchedEffect(trigger) {
        if (trigger) {
            isFlashing = true
            delay(150) // Duration of the flash
            isFlashing = false
            onAnimationComplete()
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (isFlashing) 0.8f else 0f,
        animationSpec = if (isFlashing) {
            tween(50, easing = LinearEasing)
        } else {
            tween(300, easing = LinearOutSlowInEasing)
        },
        label = "shutterFlash"
    )

    if (alpha > 0f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = alpha))
        )
    }
}
