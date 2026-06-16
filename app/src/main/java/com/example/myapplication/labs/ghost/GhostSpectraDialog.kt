package com.example.myapplication.labs.ghost

import android.content.Intent
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
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
import com.example.myapplication.labs.ghost.GhostSpectraEngine.SpectralState
import com.example.myapplication.labs.ghost.GhostSpectraEngine.StudentSpectra
import com.example.myapplication.util.maskStudentName
import java.util.*

/**
 * GhostSpectraDialog: A neural analysis interface for individual student spectroscopy.
 *
 * This dialog visualizes a student's "Spectral Signature" using the [GhostSpectraShader.SPECTROGRAPH].
 * It provides detailed metrics on behavioral intensity and spectral shift.
 */
@Composable
fun GhostSpectraDialog(
    studentName: String,
    spectra: StudentSpectra,
    globalDensity: Float,
    globalAgitation: Float,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "NEURAL SPECTROSCOPY: $studentName",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.Cyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Spectral Visualization Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(vertical = 16.dp)
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val infiniteTransition = rememberInfiniteTransition(label = "spectrograph")
                        val time by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 6.28f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(5000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "time"
                        )

                        val shader = remember { RuntimeShader(GhostSpectraShader.SPECTROGRAPH) }
                        val brush = remember(shader) { ShaderBrush(shader) }

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            shader.setFloatUniform("iResolution", size.width, size.height)
                            shader.setFloatUniform("iTime", time)
                            shader.setFloatUniform("iIntensity", spectra.intensity)
                            shader.setFloatUniform("iShift", spectra.shift)
                            drawRect(brush = brush)
                        }
                    } else {
                        // Fallback for older APIs
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.DarkGray.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Spectral Visualization (API 33+)", color = Color.Gray)
                            }
                        }
                    }
                }

                // Metrics Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem("INTENSITY", String.format(Locale.US, "%.2f", spectra.intensity))
                    MetricItem("SHIFT", String.format(Locale.US, "%.2f", spectra.shift))
                    MetricItem("STATE", spectra.state.name)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (spectra.state) {
                        SpectralState.INFRARED -> "High negative shift detected. Student may require behavioral intervention."
                        SpectralState.ULTRAVIOLET -> "High intensity engagement detected. Exceptional performance trajectory."
                        SpectralState.STABLE -> "Balanced spectral signature. Stable behavioral and academic baseline."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val report = GhostSpectraEngine.generateSpectraReport(
                        listOf(spectra),
                        mapOf(spectra.studentId to studentName),
                        globalDensity,
                        globalAgitation
                    )
                    // PRIVACY: Mask the student name in the intent subject using the hardened standard
                    val maskedName = maskStudentName(studentName)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Neural Spectroscopy Report: $maskedName")
                        putExtra(Intent.EXTRA_TEXT, report)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Spectral Report"))
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
private fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}
