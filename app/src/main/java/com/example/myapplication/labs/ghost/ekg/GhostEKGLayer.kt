package com.example.myapplication.labs.ghost.ekg

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.labs.ghost.util.GhostGlassmorphicSurface

/**
 * GhostEKGLayer: A UI component that visualizes a student's real-time "Neural EKG".
 *
 * This component renders a scrolling waveform using [GhostEKGShader] and is driven by
 * data from [GhostEKGEngine].
 */
@Composable
fun GhostEKGLayer(
    studentName: String,
    vitality: Float,
    stress: Float,
    recentSpike: Float,
    isActive: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val infiniteTransition = rememberInfiniteTransition(label = "ekg_time")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Waveform history buffer
    val waveformBuffer = remember { FloatArray(100) }

    // Update the waveform buffer periodically
    LaunchedEffect(time, vitality, stress, recentSpike) {
        // Shift buffer to the left
        for (i in 0 until 99) {
            waveformBuffer[i] = waveformBuffer[i + 1]
        }
        // Add new point at the end
        waveformBuffer[99] = GhostEKGEngine.synthesizeSignal(time, vitality, stress, recentSpike)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        GhostGlassmorphicSurface(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(300.dp),
            glassmorphismEnabled = true,
            cornerRadius = 16.dp,
            backgroundColor = Color.Black.copy(alpha = 0.6f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NEURAL EKG: $studentName",
                            color = Color.Cyan,
                            fontSize = 14.sp,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "BIOMETRIC MONITORING ACTIVE",
                            color = Color.Cyan.copy(alpha = 0.5f),
                            fontSize = 8.sp,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Text(
                        text = "VITALITY: ${(vitality * 100).toInt()}%",
                        color = if (stress > 0.6f) Color.Magenta else Color.Cyan,
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Shader Viewport
                val shader = remember { RuntimeShader(GhostEKGShader.EKG_LINE) }
                val brush = remember(shader) { ShaderBrush(shader) }

                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    shader.setFloatUniform("iResolution", size.width, size.height)
                    shader.setFloatUniform("iTime", time)
                    shader.setFloatUniform("iWaveform", waveformBuffer)
                    shader.setFloatUniform("iVitality", vitality)
                    shader.setFloatUniform("iStress", stress)

                    drawRect(brush = brush)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (stress > 0.6f) "DETECTED: ELEVATED STRESS" else "STATUS: STABLE",
                        color = if (stress > 0.6f) Color.Magenta else Color.Cyan.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
