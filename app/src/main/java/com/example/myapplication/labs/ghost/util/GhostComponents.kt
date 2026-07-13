package com.example.myapplication.labs.ghost.util

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * GhostGlassmorphicSurface: A reusable glassmorphic container for Ghost Lab.
 *
 * Implements a "Frosted Glass" effect using blur, semi-transparent backgrounds,
 * and a subtle border. The intensity is controlled by the [glassmorphismEnabled] flag.
 *
 * BOLT ⚡ Optimization: This implementation uses a separate background layer for
 * the blur effect to ensure that child content remains sharp and readable.
 *
 * NATIVE 📱: Degrades gracefully on devices < Android 12 (API 31) where [Modifier.blur]
 * is not natively supported by the hardware renderer.
 *
 * @param modifier Composable modifier.
 * @param glassmorphismEnabled Whether the effect is active (from preferences).
 * @param blurRadius The radius of the background blur.
 * @param backgroundColor The semi-transparent background color.
 * @param borderColor The subtle border color.
 * @param cornerRadius The corner radius of the container.
 * @param content The composable content to be rendered inside the surface.
 */
@Composable
fun GhostGlassmorphicSurface(
    modifier: Modifier = Modifier,
    glassmorphismEnabled: Boolean = false,
    blurRadius: Dp = 16.dp,
    backgroundColor: Color = Color.White.copy(alpha = 0.1f),
    borderColor: Color = Color.White.copy(alpha = 0.2f),
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
    ) {
        // Background Layer (Blurred or Fallback)
        if (glassmorphismEnabled) {
            val backgroundModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Modifier
                    .matchParentSize()
                    .blur(blurRadius)
            } else {
                // Fallback: More opaque background for pre-API 31
                Modifier
                    .matchParentSize()
            }

            Box(
                modifier = backgroundModifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                backgroundColor.copy(alpha = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) backgroundColor.alpha * 1.5f else 0.8f),
                                backgroundColor.copy(alpha = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) backgroundColor.alpha else 0.4f)
                            )
                        )
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(backgroundColor.copy(alpha = 0.3f))
            )
        }

        // Content Layer (Sharp)
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(1.dp, borderColor, RoundedCornerShape(cornerRadius)),
            content = content
        )
    }
}
