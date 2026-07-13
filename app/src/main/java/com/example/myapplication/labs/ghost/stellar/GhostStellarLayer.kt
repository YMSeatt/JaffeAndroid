package com.example.myapplication.labs.ghost.stellar

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
import androidx.compose.ui.graphics.toArgb
import com.example.myapplication.data.QuizLog
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.labs.ghost.GhostConfig

/**
 * GhostStellarLayer: Renders the "Neural Constellation" visualization.
 */
@Composable
fun GhostStellarLayer(
    students: List<StudentUiItem>,
    quizLogsByStudent: Map<Long, List<QuizLog>>,
    isActive: Boolean,
    canvasScale: Float,
    canvasOffset: Offset,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !isActive) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val infiniteTransition = rememberInfiniteTransition(label = "stellarTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // BOLT: Heavy synthesis is only done when students or quiz logs change.
    val stars = remember(students, quizLogsByStudent) {
        GhostStellarEngine.calculateStars(students, quizLogsByStudent)
    }
    val constellations = remember(stars) {
        GhostStellarEngine.calculateConstellations(stars)
    }

    val starShader = remember { RuntimeShader(GhostStellarShader.STAR_FIELD) }
    val starBrush = remember(starShader) { ShaderBrush(starShader) }

    val threadShader = remember { RuntimeShader(GhostStellarShader.STELLAR_THREAD) }
    val threadBrush = remember(threadShader) { ShaderBrush(threadShader) }

    Canvas(modifier = modifier.fillMaxSize()) {
        // 1. Draw Star Field Background
        starShader.setFloatUniform("iResolution", size.width, size.height)
        starShader.setFloatUniform("iTime", time)
        starShader.setFloatUniform("iIntensity", 0.5f)
        drawRect(brush = starBrush)

        // 2. Draw Constellation Threads
        constellations.forEach { (a, b) ->
            val start = Offset(
                a.x * canvasScale + canvasOffset.x,
                a.y * canvasScale + canvasOffset.y
            )
            val end = Offset(
                b.x * canvasScale + canvasOffset.x,
                b.y * canvasScale + canvasOffset.y
            )

            threadShader.setFloatUniform("iResolution", size.width, size.height)
            threadShader.setFloatUniform("iTime", time)
            threadShader.setFloatUniform("iStart", start.x, start.y)
            threadShader.setFloatUniform("iEnd", end.x, end.y)
            threadShader.setFloatUniform("iStrength", 1.0f)
            threadShader.setColorUniform("iColor", Color(0xFF80D8FF).toArgb())

            // We use a simplified drawLine for the thread effect in this PoC
            // In a full implementation, we might use a specialized line-drawing shader technique
            // For now, we'll draw a thin rectangle covering the segment or just use standard drawLine if shader is complex
            drawLine(
                brush = threadBrush,
                start = start,
                end = end,
                strokeWidth = 4f
            )
        }

        // 3. Draw Student Stars
        stars.forEach { star ->
            val screenPos = Offset(
                star.x * canvasScale + canvasOffset.x,
                star.y * canvasScale + canvasOffset.y
            )

            // Pulse star based on magnitude
            val pulse = 1.0f + 0.2f * kotlin.math.sin(time * 5.0f + star.studentId)
            val radius = (10f + star.magnitude * 15f) * pulse

            drawCircle(
                color = Color(star.spectralColor),
                radius = radius,
                center = screenPos
            )

            // Outer Glow
            drawCircle(
                color = Color(star.spectralColor).copy(alpha = 0.3f),
                radius = radius * 2f,
                center = screenPos
            )
        }
    }
}
