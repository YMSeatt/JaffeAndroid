package com.example.myapplication.labs.ghost.shell

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.labs.ghost.util.GhostGlassmorphicSurface
import com.example.myapplication.labs.ghost.util.GhostHapticManager
import androidx.compose.ui.platform.LocalContext

/**
 * GhostShellLayer: An immersive bottom dock for managing Ghost experiments
 * and monitoring real-time classroom "Health" and "Pulse".
 *
 * This layer acts as the "Mission Control" for the Ghost Lab suite. It provides
 * a centralized HUD for activating R&D features and visualizes the classroom's
 * data-driven heartbeat using a high-performance AGSL shader.
 *
 * ### AGSL Uniform Mapping:
 * - `iHealth`: Drives the color shift of the neural pulse. Health below 40%
 *   causes the pulse to refract toward Red, providing ambient tension feedback.
 * - `iFrequency`: Scales the wave speed and intensity, reflecting the "tempo"
 *   of recent classroom interactions.
 *
 * @param behaviorLogs Reactive stream of classroom behavioral incidents.
 * @param isActive Master toggle for the shell's visibility.
 * @param isHudActive State of the Ghost HUD experiment.
 * @param onToggleHud Callback to activate/deactivate HUD mode.
 * @param isVisionActive State of the Ghost Vision AR experiment.
 * @param onToggleVision Callback to activate/deactivate Vision mode.
 * @param isStrategistActive State of the Ghost Strategist tactical AI.
 * @param onToggleStrategist Callback to activate/deactivate Strategist mode.
 * @param isAuroraActive State of the Ghost Aurora atmospheric experiment.
 * @param onToggleAurora Callback to activate/deactivate Aurora mode.
 */
@Composable
fun GhostShellLayer(
    behaviorLogs: List<BehaviorEvent>,
    isActive: Boolean,
    isHudActive: Boolean,
    onToggleHud: () -> Unit,
    isVisionActive: Boolean,
    onToggleVision: () -> Unit,
    isStrategistActive: Boolean,
    onToggleStrategist: () -> Unit,
    isAuroraActive: Boolean,
    onToggleAurora: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val context = LocalContext.current
    val hapticManager = remember { GhostHapticManager(context) }
    val metrics = remember(behaviorLogs) { GhostShellEngine.calculateMetrics(behaviorLogs) }

    val infiniteTransition = rememberInfiniteTransition(label = "ShellPulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        GhostGlassmorphicSurface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(100.dp),
            glassmorphismEnabled = true,
            cornerRadius = 24.dp,
            backgroundColor = Color.Black.copy(alpha = 0.4f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Classroom Health Visualization
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val shader = remember { RuntimeShader(GhostShellShader.NEURAL_PULSE) }
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            shader.setFloatUniform("iResolution", size.width, size.height)
                            shader.setFloatUniform("iTime", time)
                            shader.setFloatUniform("iHealth", metrics.healthIndex)
                            shader.setFloatUniform("iFrequency", metrics.pulseFrequency)
                            shader.setColorUniform("iColor", android.graphics.Color.valueOf(0f, 1f, 1f, 0.6f))
                            drawRect(brush = ShaderBrush(shader))
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(metrics.healthIndex * 100).toInt()}%",
                            color = Color.Cyan,
                            fontSize = 18.sp,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "HEALTH",
                            color = Color.Cyan.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // 2. Action Toggles
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShellToggleButton(
                        icon = Icons.Default.AutoFixHigh,
                        label = "HUD",
                        isActive = isHudActive,
                        onClick = {
                            onToggleHud()
                            hapticManager.perform(GhostHapticManager.Pattern.UI_CLICK)
                        }
                    )
                    ShellToggleButton(
                        icon = Icons.Default.PhotoCamera,
                        label = "VISION",
                        isActive = isVisionActive,
                        onClick = {
                            onToggleVision()
                            hapticManager.perform(GhostHapticManager.Pattern.UI_CLICK)
                        }
                    )
                    ShellToggleButton(
                        icon = Icons.Default.Psychology,
                        label = "BRAIN",
                        isActive = isStrategistActive,
                        onClick = {
                            onToggleStrategist()
                            hapticManager.perform(GhostHapticManager.Pattern.UI_CLICK)
                        }
                    )
                    ShellToggleButton(
                        icon = Icons.Default.Waves,
                        label = "CLIMATE",
                        isActive = isAuroraActive,
                        onClick = {
                            onToggleAurora()
                            hapticManager.perform(GhostHapticManager.Pattern.UI_CLICK)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShellToggleButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(44.dp)
                .alpha(if (isActive) 1f else 0.5f)
        ) {
            Surface(
                shape = CircleShape,
                color = if (isActive) Color.Cyan.copy(alpha = 0.2f) else Color.Transparent,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) Color.Cyan else Color.White
                )
            }
        }
        Text(
            text = label,
            color = if (isActive) Color.Cyan else Color.White.copy(alpha = 0.5f),
            fontSize = 9.sp,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
