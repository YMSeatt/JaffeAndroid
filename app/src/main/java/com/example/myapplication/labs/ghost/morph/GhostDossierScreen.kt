package com.example.myapplication.labs.ghost.morph

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.labs.ghost.GhostLinkEngine
import androidx.compose.ui.graphics.toArgb

/**
 * GhostDossierScreen: A high-fidelity, full-screen student dossier.
 *
 * This screen displays the Markdown output from [GhostLinkEngine] over a
 * dynamic "Neural Fluid" AGSL background.
 *
 * @param studentId The ID of the student to display.
 * @param studentName The name of the student.
 * @param onDismiss Callback to close the dossier.
 */
@Composable
fun GhostDossierScreen(
    studentId: Long,
    studentName: String,
    onDismiss: () -> Unit
) {
    val dossierMarkdown = remember(studentId, studentName) {
        GhostLinkEngine.generateNeuralDossier(studentId, studentName)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "dossier_pulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    var size by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

    Surface(
        modifier = Modifier.fillMaxSize().onSizeChanged { size = it },
        color = Color.Black
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Neural Fluid Background
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && size.width > 0) {
                val shader = remember { RuntimeShader(GhostMorphShader.NEURAL_FLUID) }
                val color1 = Color(0xFF1A0033) // Deep Violet
                val color2 = Color(0xFF00BFA5) // Cyber Cyan

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            ShaderBrush(shader.apply {
                                setFloatUniform("iTime", time)
                                setFloatUniform("iResolution", size.width.toFloat(), size.height.toFloat())
                                setColorUniform("iColor1", color1.toArgb())
                                setColorUniform("iColor2", color2.toArgb())
                            })
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "NEURAL DOSSIER",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF00BFA5),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Simple Markdown-ish display for the dossier
                        dossierMarkdown.split("\n").forEach { line ->
                            when {
                                line.startsWith("# ") -> {
                                    Text(
                                        text = line.removePrefix("# "),
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                line.startsWith("## ") -> {
                                    Text(
                                        text = line.removePrefix("## "),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.Cyan,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                    )
                                }
                                line.startsWith("- ") -> {
                                    Row(modifier = Modifier.padding(start = 8.dp, vertical = 2.dp)) {
                                        Text(text = "•", color = Color.Cyan)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = line.removePrefix("- "), color = Color.LightGray)
                                    }
                                }
                                line.startsWith("|") -> {
                                    // Skip table headers/separators for simplicity in PoC
                                    if (!line.contains("---")) {
                                        Text(
                                            text = line.replace("|", "  "),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.LightGray
                                        )
                                    }
                                }
                                line.isNotBlank() -> {
                                    Text(
                                        text = line.replace("**", ""),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.LightGray,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    )
                ) {
                    Text("De-Phase Dossier")
                }

                Spacer(modifier = Modifier.height(64.dp))
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}
