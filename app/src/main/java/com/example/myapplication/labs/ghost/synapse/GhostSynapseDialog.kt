package com.example.myapplication.labs.ghost.synapse

import android.content.Context
import android.graphics.RuntimeShader
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import kotlinx.coroutines.delay

/**
 * GhostSynapseDialog: A futuristic dialog that displays AI-generated student narratives.
 *
 * It features:
 * - An AGSL animated background (NEURAL_FLOW).
 * - A typewriter effect for text delivery.
 * - Dynamic haptic feedback based on narrative generation milestones.
 */
@Composable
fun GhostSynapseDialog(
    studentName: String,
    behaviorLogs: List<BehaviorEvent>,
    quizLogs: List<QuizLog>,
    homeworkLogs: List<HomeworkLog>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var narrativeText by remember { mutableStateOf("Initializing Neural Link...") }
    var isGenerating by remember { mutableStateOf(true) }
    var displayedText by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "synapsePulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Generate narrative and typewriter effect
    LaunchedEffect(Unit) {
        triggerHaptic(context, HapticType.LINK_START)
        val fullNarrative = GhostSynapseEngine.generateNarrative(
            studentName, behaviorLogs, quizLogs, homeworkLogs
        )
        narrativeText = fullNarrative
        isGenerating = false
        triggerHaptic(context, HapticType.DATA_STREAM)

        fullNarrative.forEachIndexed { index, char ->
            displayedText += char
            if (index % 5 == 0) delay(10) // Rapid typewriter
            if (char == '.') {
                delay(200)
                triggerHaptic(context, HapticType.NEURAL_PULSE)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 300.dp, max = 500.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // AGSL Background
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val shader = remember { RuntimeShader(GhostSynapseShader.NEURAL_FLOW) }
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        shader.setFloatUniform("iResolution", size.width, size.height)
                        shader.setFloatUniform("iTime", time)
                        shader.setFloatUniform("iColor", 0f, 1f, 1f) // Cyan
                        drawRect(brush = ShaderBrush(shader))
                    }
                } else {
                    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.9f)) {}
                }

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "SYNAPSE REPORT: $studentName",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Cyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Cyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = displayedText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan.copy(alpha = 0.2f)),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("DISCONNECT", color = Color.Cyan)
                    }
                }
            }
        }
    }
}

enum class HapticType {
    LINK_START,
    DATA_STREAM,
    NEURAL_PULSE
}

private fun triggerHaptic(context: Context, type: HapticType) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        when (type) {
            HapticType.LINK_START -> {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            }
            HapticType.DATA_STREAM -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val composition = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.5f)
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.5f, 50)
                        .compose()
                    vibrator.vibrate(composition)
                }
            }
            HapticType.NEURAL_PULSE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    vibrator.vibrate(VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 1.0f)
                        .compose())
                }
            }
        }
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(20)
    }
}
