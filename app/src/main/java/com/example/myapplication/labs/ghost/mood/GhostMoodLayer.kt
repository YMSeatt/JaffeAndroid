package com.example.myapplication.labs.ghost.mood

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import com.example.myapplication.labs.ghost.GhostConfig

/**
 * GhostMoodLayer: A high-performance visualization layer for the Neural Mood Board.
 *
 * This layer renders the collective mood of the classroom as an organic, atmospheric
 * background on the seating chart.
 *
 * BOLT ⚡ Optimization: Uses zero-allocation rendering by hoisting the [RuntimeShader]
 * and [ShaderBrush] and updating uniforms directly in the [Canvas] block.
 */
@Composable
fun GhostMoodLayer(
    classroomMood: GhostMoodEngine.ClassroomMood,
    isVisible: Boolean
) {
    if (!isVisible || !GhostConfig.GHOST_MODE_ENABLED) return

    val infiniteTransition = rememberInfiniteTransition(label = "ghost_mood_time")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Smoothly animate mood parameters to prevent visual jumps
    val animatedIntensity by animateFloatAsState(classroomMood.collectiveIntensity, label = "intensity")
    val animatedValence by animateFloatAsState(classroomMood.collectiveValence, label = "valence")
    val animatedStability by animateFloatAsState(classroomMood.stability, label = "stability")

    val primaryColor = when (classroomMood.aggregateState) {
        GhostMoodEngine.MoodState.TURBULENT -> Color.Red
        GhostMoodEngine.MoodState.FOCUSED -> Color.Cyan
        GhostMoodEngine.MoodState.ENERGETIC -> Color.Yellow
        GhostMoodEngine.MoodState.CALM -> Color.Green
    }

    val secondaryColor = when (classroomMood.aggregateState) {
        GhostMoodEngine.MoodState.TURBULENT -> Color(0xFF6200EE) // Purple
        GhostMoodEngine.MoodState.FOCUSED -> Color.Blue
        GhostMoodEngine.MoodState.ENERGETIC -> Color.Magenta
        GhostMoodEngine.MoodState.CALM -> Color(0xFF004D40) // Dark Teal
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { RuntimeShader(GhostMoodShader.NEURAL_MOOD_BOARD) }
        val brush = remember(shader) { ShaderBrush(shader) }

        Canvas(modifier = Modifier.fillMaxSize()) {
            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iTime", time)
            shader.setFloatUniform("iIntensity", animatedIntensity)
            shader.setFloatUniform("iValence", animatedValence)
            shader.setFloatUniform("iStability", animatedStability)

            shader.setColorUniform("iColorPrimary",
                android.graphics.Color.valueOf(primaryColor.red, primaryColor.green, primaryColor.blue, primaryColor.alpha))
            shader.setColorUniform("iColorSecondary",
                android.graphics.Color.valueOf(secondaryColor.red, secondaryColor.green, secondaryColor.blue, secondaryColor.alpha))

            drawRect(brush = brush)
        }
    }
}
