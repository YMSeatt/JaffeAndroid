package com.example.myapplication.labs.ghost.glance

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.labs.ghost.util.GhostGlassmorphicSurface
import com.example.myapplication.ui.theme.GhostCyan
import com.example.myapplication.ui.theme.GhostMagenta

/**
 * GhostGlanceSurface: A modular, glassmorphic UI component for the Glance preview.
 *
 * It uses a background AGSL shader to visualize "Neural Momentum" and "Stability"
 * behind a frosted-glass surface.
 *
 * @param studentName The student's name to display.
 * @param state The current [GhostGlanceEngine.GlanceState] synthesized from student logs.
 */
@Composable
fun GhostGlanceSurface(
    studentName: String,
    state: GhostGlanceEngine.GlanceState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glancePulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    GhostGlassmorphicSurface(
        modifier = modifier
            .width(220.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp)),
        glassmorphismEnabled = true
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. AGSL Background Shader
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val shader = remember { RuntimeShader(GhostGlanceShader.NEURAL_WAVE) }
                val brush = remember(shader) { ShaderBrush(shader) }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    shader.setFloatUniform("size", size.width, size.height)
                    shader.setFloatUniform("time", time)
                    shader.setFloatUniform("momentum", state.momentum)
                    shader.setFloatUniform("stability", state.stability)

                    val colorA = GhostCyan
                    val colorB = GhostMagenta
                    shader.setColorUniform("colorA", android.graphics.Color.valueOf(colorA.red, colorA.green, colorA.blue, colorA.alpha))
                    shader.setColorUniform("colorB", android.graphics.Color.valueOf(colorB.red, colorB.green, colorB.blue, colorB.alpha))

                    drawRect(brush = brush)
                }
            }

            // 2. Content Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "NEURAL GLANCE 👻",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GhostCyan.copy(alpha = 0.8f),
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = studentName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    GlanceMetric("SIG", state.signature.name, GhostCyan)
                    GlanceMetric("STB", "${(state.stability * 100).toInt()}%", if (state.stability > 0.5f) GhostCyan else GhostMagenta)
                    GlanceMetric("MOM", "${(state.momentum * 100).toInt()}%", Color.White)
                }
            }
        }
    }
}

@Composable
fun GlanceMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.Gray,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                color = color,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp
            )
        )
    }
}
