package com.example.myapplication.labs.ghost.util

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode

/**
 * Modifier.ghostShimmer: A reusable, high-performance shimmer effect for Ghost Lab.
 *
 * BOLT ⚡ Optimization: Uses [drawBehind] to avoid modifier object allocations
 * in the composition tree. The gradient translation is calculated based on
 * the actual size of the composable, ensuring a consistent effect across scales.
 *
 * @param colors The colors used in the shimmer gradient. Defaults to "Neural Ghost" palette.
 * @param durationMillis The duration of one shimmer cycle in milliseconds.
 */
fun Modifier.ghostShimmer(
    colors: List<Color> = listOf(
        Color.Gray.copy(alpha = 0.1f),
        Color.Cyan.copy(alpha = 0.2f),
        Color.Gray.copy(alpha = 0.1f),
    ),
    durationMillis: Int = 1500
): Modifier = this.composed {
    val transition = rememberInfiniteTransition(label = "ghost_shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translation"
    )

    this.drawBehind {
        val brush = Brush.linearGradient(
            colors = colors,
            start = Offset(size.width * translateAnim, 0f),
            end = Offset(size.width * (translateAnim + 0.5f), size.height),
            tileMode = TileMode.Clamp
        )
        drawRect(brush = brush)
    }
}
