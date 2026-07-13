package com.example.myapplication.labs.ghost.catalyst

import android.content.Intent
import android.graphics.RuntimeShader
import android.os.Build
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

/**
 * GhostCatalystDialog: A neural analysis interface for macroscopic classroom kinetics.
 *
 * This dialog visualizes the classroom's "Reaction Field" using the [GhostCatalystShader.REACTION_FIELD]
 * and provides detailed metrics on behavioral chain reactions.
 */
@Composable
fun GhostCatalystDialog(
    kinetics: GhostCatalystEngine.GlobalKinetics,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "GHOST CATALYST: KINETICS REPORT",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.Cyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Kinetics Visualization Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(vertical = 16.dp)
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val infiniteTransition = rememberInfiniteTransition(label = "catalyst_field")
                        val time by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 1000f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(100000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "time"
                        )

                        val shader = remember { RuntimeShader(GhostCatalystShader.REACTION_FIELD) }
                        val brush = remember(shader) { ShaderBrush(shader) }

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            shader.setFloatUniform("iResolution", size.width, size.height)
                            shader.setFloatUniform("iTime", time)
                            shader.setFloatUniform("iColor", 0.0f, 1.0f, 0.8f) // Cyan
                            shader.setFloatUniform("iRate", (kinetics.reactionRate / 10f).coerceIn(0.1f, 1.0f))
                            drawRect(brush = brush)
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.DarkGray.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Kinetics Visualization (API 33+)", color = Color.Gray)
                            }
                        }
                    }
                }

                // Macroscopic Metrics
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KineticsMetricRow("REACTIONS DETECTED", kinetics.reactionsDetected.toString(), "count")
                    KineticsMetricRow("REACTION RATE", String.format(Locale.US, "%.2f", kinetics.reactionRate), "r/5min")
                    KineticsMetricRow("ACTIVATION ENERGY", String.format(Locale.US, "%.2f", kinetics.activationEnergy), "eV (eq)")
                    KineticsMetricRow("EQUILIBRIUM CONSTANT", String.format(Locale.US, "%.2f", kinetics.equilibriumConstant), "K_eq")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Social equilibrium is reached when the reaction rate is balanced by the activation energy. High rates without matching energy indicate high social friction.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val report = GhostCatalystEngine.generateCatalystReport(kinetics)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Ghost Catalyst: Kinetics Report")
                        putExtra(Intent.EXTRA_TEXT, report)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Kinetics Report"))
                }
            ) {
                Text("EXPORT REPORT", color = Color.Cyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE")
            }
        },
        containerColor = Color(0xFF001212),
        textContentColor = Color.LightGray
    )
}

@Composable
private fun KineticsMetricRow(label: String, value: String, unit: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(unit, style = MaterialTheme.typography.labelSmall, color = Color.Gray.copy(alpha = 0.5f))
        }
        Text(
            value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}
