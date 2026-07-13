package com.example.myapplication.labs.ghost.util

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
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

/**
 * Modifier.ghostNeuralGlow: A high-performance, shader-backed glow effect for student icons.
 *
 * BOLT ⚡ Optimization: Uses [RuntimeShader] on API 33+ for GPU-accelerated procedural glow.
 * Falls back to a subtle shadow on older devices to maintain visual continuity.
 *
 * @param color The base color of the glow.
 * @param intensity The radius/intensity of the glow (0.0 to 1.0).
 * @param enabled Whether the glow is active.
 */
fun Modifier.ghostNeuralGlow(
    color: Color = Color.Cyan,
    intensity: Float = 0.5f,
    enabled: Boolean = true
): Modifier = this.composed {
    if (!enabled) return@composed this

    val transition = rememberInfiniteTransition(label = "ghost_glow")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glow_time"
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { RuntimeShader(GhostNeuralGlowShader.NEURAL_GLOW) }

        this.drawBehind {
            shader.setFloatUniform("size", size.width, size.height)
            shader.setFloatUniform("time", time)
            shader.setFloatUniform("intensity", intensity)
            shader.setColorUniform(
                "color",
                android.graphics.Color.valueOf(color.red, color.green, color.blue, color.alpha)
            )

            drawRect(brush = ShaderBrush(shader))
        }
    } else {
        // Fallback for API < 33: Simple colored background with opacity
        this.background(color.copy(alpha = 0.2f * intensity))
    }
}
