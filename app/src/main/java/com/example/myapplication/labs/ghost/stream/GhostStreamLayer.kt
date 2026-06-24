package com.example.myapplication.labs.ghost.stream

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.labs.ghost.util.GhostGlassmorphicSurface

/**
 * GhostStreamLayer: A high-fidelity, glassmorphic activity ticker overlay.
 *
 * This component renders the "Neural Stream" of classroom interactions using a
 * vertical scrolling ticker. It is designed to provide immediate spatial and
 * temporal awareness of classroom dynamics.
 *
 * ### Architectural Features:
 * - **Glassmorphic Surface**: Utilizes [GhostGlassmorphicSurface] for a futuristic,
 *   translucent aesthetic.
 * - **AGSL Background**: Renders a procedural [GhostStreamShader.DATA_STREAM_SHADER]
 *   to visualize the "velocity" of classroom data.
 * - **Fluid Transitions**: Leverages [AnimatedVisibility] and [LazyColumn] with
 *   stable keys to ensure smooth entry/exit animations for stream entries.
 *
 * @param entries The synthesized list of classroom activities from [GhostStreamEngine].
 * @param isActive Visibility toggle for the ticker layer.
 * @param modifier Custom layout modifiers.
 */
@Composable
fun GhostStreamLayer(
    entries: List<GhostStreamEngine.StreamEntry>,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isActive,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .padding(16.dp)
    ) {
        GhostGlassmorphicSurface(
            glassmorphismEnabled = true,
            cornerRadius = 24.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color.Cyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "NEURAL STREAM",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Cyan,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Shader Background for list
                Box(modifier = Modifier.weight(1f)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val shader = remember { RuntimeShader(GhostStreamShader.DATA_STREAM_SHADER) }
                        val infiniteTransition = rememberInfiniteTransition(label = "stream_shader")
                        val time by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 6.28f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(10000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "time"
                        )

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            shader.setFloatUniform("iResolution", size.width, size.height)
                            shader.setFloatUniform("iTime", time)
                            shader.setFloatUniform("iIntensity", 1.0f)
                            drawRect(brush = ShaderBrush(shader))
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(entries, key = { it.id }) { entry ->
                            StreamEntryItem(entry)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual entry item within the [GhostStreamLayer].
 *
 * Maps the [GhostStreamEngine.EntryType] to specific thematic colors (Cyan, Magenta, Purple)
 * and applies glassmorphic styling to match the Ghost Lab aesthetic.
 */
@Composable
fun StreamEntryItem(entry: GhostStreamEngine.StreamEntry) {
    val color = when (entry.type) {
        GhostStreamEngine.EntryType.POSITIVE -> Color.Cyan
        GhostStreamEngine.EntryType.NEGATIVE -> Color.Magenta
        GhostStreamEngine.EntryType.ACADEMIC -> Color(0xFFA066FF) // Purple
        GhostStreamEngine.EntryType.SYSTEM -> Color.Gray
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.9f)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                entry.studentName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Text(
                entry.formattedTime,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            entry.content,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
    }
}
