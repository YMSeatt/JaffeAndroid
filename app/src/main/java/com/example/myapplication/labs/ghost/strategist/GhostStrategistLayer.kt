package com.example.myapplication.labs.ghost.strategist

import android.graphics.RuntimeShader
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * GhostStrategistLayer: Visualizes the Generative AI Co-Pilot interface.
 *
 * This layer renders pedagogical interventions proposed by the [GhostStrategistEngine]
 * using AGSL Shaders for atmospheric feedback and high-fidelity Android 15 haptics
 * for urgent alerts.
 *
 * @param interventions The current list of tactical actions proposed by the AI.
 * @param isActive Whether the strategist layer is visible on the seating chart.
 * @param isThinking Whether the AI is currently synthesizing new interventions.
 */
@Composable
fun GhostStrategistLayer(
    interventions: List<GhostStrategistEngine.TacticalIntervention>,
    isActive: Boolean,
    isThinking: Boolean
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "StrategistPulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val thinkingAlpha by animateFloatAsState(
        targetValue = if (isThinking) 1f else 0f,
        animationSpec = tween(500),
        label = "ThinkingAlpha"
    )

    // BOLT: Re-use the Neural Stream shader to visualize the AI's "thought process".
    // The shader provides a flowing cyan/white data stream that reacts to the 'isThinking' state.
    val shader = remember { RuntimeShader(GhostStrategistShader.NEURAL_STREAM_SHADER) }

    // Trigger Android 15/16 Haptic Alerts for high-urgency interventions:
    // Uses a sophisticated vibration composition to provide eyes-free tactical awareness.
    // PRIMITIVE_SPIN (0.8f): Provides a sense of "rotational momentum" or "buildup".
    // PRIMITIVE_TICK (0.5f, 200ms delay): A crisp, final alert indicating the AI has
    // finalized a critical tactical proposal.
    LaunchedEffect(interventions) {
        if (interventions.any { it.urgency > 0.9f } && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val composition = VibrationEffect.startComposition()
            composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_SPIN, 0.8f)
            composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.5f, 200)
            vibrator?.vibrate(composition.compose())
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Neural Stream Background (Active while thinking or high intensity)
        if (thinkingAlpha > 0.01f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                shader.setFloatUniform("uResolution", size.width, size.height)
                shader.setFloatUniform("uTime", time)
                shader.setFloatUniform("uAlpha", thinkingAlpha * 0.3f)
                shader.setFloatUniform("uIntensity", if (isThinking) 1.0f else 0.5f)
                drawRect(brush = ShaderBrush(shader))
            }
        }

        // 2. Tactical Overlay
        AnimatedVisibility(
            visible = interventions.isNotEmpty(),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
                border = CardDefaults.outlinedCardBorder(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.Cyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Tactical Intelligence PoC",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Cyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(interventions) { intervention ->
                            InterventionItem(intervention)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders an individual tactical intervention item with category-specific styling.
 *
 * @param intervention The tactical proposal to display.
 */
@Composable
fun InterventionItem(intervention: GhostStrategistEngine.TacticalIntervention) {
    val categoryColor = when (intervention.category) {
        GhostStrategistEngine.InterventionCategory.SOCIAL_DYNAMICS -> Color(0xFF00BCD4)
        GhostStrategistEngine.InterventionCategory.ACADEMIC_ACCELERATION -> Color(0xFF9C27B0)
        GhostStrategistEngine.InterventionCategory.BEHAVIORAL_REINFORCEMENT -> Color(0xFFFF9800)
        GhostStrategistEngine.InterventionCategory.TEMPORAL_ADJUSTMENT -> Color(0xFFE91E63)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(categoryColor.copy(alpha = 0.15f))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                if (intervention.urgency > 0.8f) Icons.Default.Warning else Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = categoryColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    intervention.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    intervention.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
