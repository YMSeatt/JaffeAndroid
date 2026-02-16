package com.example.myapplication.labs.ghost

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * GhostVoiceVisualizer: Renders a reactive neural waveform in response to voice input.
 *
 * This Composable uses the [GhostShader.VOICE_WAVEFORM] to draw an animated sine wave
 * whose height (amplitude) is driven by real-time voice levels captured by the [GhostVoiceAssistant].
 *
 * @param amplitude The current volume level (0.0 to 1.0) used to drive the shader height.
 * @param isListening Whether the voice assistant is currently active.
 * @param currentText The partial or final text recognized by the speech engine.
 */
@Composable
fun GhostVoiceVisualizer(
    amplitude: Float,
    isListening: Boolean,
    currentText: String,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !isListening) return

    val infiniteTransition = rememberInfiniteTransition(label = "voiceWave")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val animatedAmplitude by animateFloatAsState(
        targetValue = amplitude,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "amplitude"
    )

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val color = MaterialTheme.colorScheme.tertiary
            val shader = remember { RuntimeShader(GhostShader.VOICE_WAVEFORM) }
            Canvas(modifier = Modifier.fillMaxSize()) {
                shader.setFloatUniform("iResolution", size.width, size.height)
                shader.setFloatUniform("iTime", time)
                shader.setFloatUniform("iAmplitude", animatedAmplitude * 0.5f + 0.1f)
                shader.setFloatUniform("iColor", color.red, color.green, color.blue)

                drawRect(brush = ShaderBrush(shader))
            }
        }

        if (currentText.isNotEmpty()) {
            Text(
                text = currentText,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
            )
        }
    }
}
