package com.example.myapplication.labs.ghost

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun GhostOracleDialog(
    prophecies: List<GhostOracle.Prophecy>,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hologramPulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
        ) {
            // Hologram background effect
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val shader = remember { RuntimeShader(GhostShader.HOLOGRAM_OVERLAY) }
                shader.setFloatUniform("iTime", time)
                shader.setFloatUniform("iResolution", 2000f, 2000f) // Mock resolution
                shader.setFloatUniform("iColor", 0.0f, 1.0f, 1.0f) // Cyan

                Box(modifier = Modifier.fillMaxSize().background(ShaderBrush(shader)))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.Cyan,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "NEURAL ORACLE 👻",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.Cyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Simulating Gemini Nano Predictive Core...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Cyan.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (prophecies.isEmpty()) {
                    Box(modifier = Modifier.fillWeight(), contentAlignment = Alignment.Center) {
                        Text("No significant neural trends detected.", color = Color.White)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(prophecies) { prophecy ->
                            OracleCard(prophecy)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan.copy(alpha = 0.2f))
                ) {
                    Text("ACKNOWLEDGMENT RECEIVED", color = Color.Cyan)
                }
            }
        }
    }
}

@Composable
fun OracleCard(prophecy: GhostOracle.Prophecy) {
    val borderBrush = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { RuntimeShader(GhostShader.NEURAL_LINE) }
        shader.setFloatUniform("iTime", 0f)
        shader.setFloatUniform("iColor", 0f, 1f, 1f)
        shader.setFloatUniform("iResolution", 500f, 500f)
        ShaderBrush(shader)
    } else {
        androidx.compose.ui.graphics.SolidColor(Color.Cyan.copy(alpha = 0.5f))
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.DarkGray.copy(alpha = 0.5f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = borderBrush
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when (prophecy.type) {
                        GhostOracle.ProphecyType.SOCIAL_FRICTION -> Icons.Default.Warning
                        else -> Icons.Default.AutoAwesome
                    },
                    contentDescription = null,
                    tint = if (prophecy.type == GhostOracle.ProphecyType.SOCIAL_FRICTION) Color.Red else Color.Cyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    prophecy.type.name.replace("_", " "),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "${(prophecy.confidence * 100).toInt()}% CONFIDENCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Cyan
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                prophecy.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

private fun Modifier.fillWeight() = this.fillMaxWidth().fillMaxHeight()
