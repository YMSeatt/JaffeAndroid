package com.example.myapplication.labs.ghost.util

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Modifier.ghostShimmer: A reusable, high-performance shimmer effect for Ghost Lab.
 *
 * This modifier uses [InfiniteTransition] to animate a linear gradient across
 * the target composable, creating a "Neural Shimmer" effect.
 *
 * @param colors The colors used in the shimmer gradient. Defaults to "Neural Gray".
 * @param durationMillis The duration of one shimmer cycle in milliseconds.
 */
fun Modifier.ghostShimmer(
    colors: List<Color> = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    ),
    durationMillis: Int = 1000
): Modifier = this.composed {
    val transition = rememberInfiniteTransition(label = "ghost_shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translation"
    )

    val brush = Brush.linearGradient(
        colors = colors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    this.background(brush)
}
