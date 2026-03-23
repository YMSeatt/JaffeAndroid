package com.example.myapplication.labs.ghost.vision

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostVisionLayer: Compose Neural AR Viewport.
 *
 * This layer renders the AR HUD and projects student data into 3D AR space
 * based on the device's physical orientation.
 *
 * It uses [GhostVisionEngine] for coordinate projection and [GhostVisionShader]
 * for the futuristic HUD visuals.
 */
@Composable
fun GhostVisionLayer(
    engine: GhostVisionEngine,
    students: List<StudentUiItem>,
    isActive: Boolean
) {
    if (!isActive) return

    val infiniteTransition = rememberInfiniteTransition(label = "VisionPulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    var size by remember { mutableStateOf(IntSize(0, 0)) }
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .onSizeChanged { size = it }
    ) {
        // 1. AR HUD Shader Layer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hudShader = remember { RuntimeShader(GhostVisionShader.AR_HUD) }
            Canvas(modifier = Modifier.fillMaxSize()) {
                hudShader.setFloatUniform("iResolution", size.width.toFloat(), size.height.toFloat())
                hudShader.setFloatUniform("iTime", time)
                hudShader.setFloatUniform("iStaticIntensity", 0.5f)
                drawRect(ShaderBrush(hudShader))
            }
        }

        // 2. Projected Student Neural Glyphs
        students.forEach { student ->
            val projectedOffset = engine.project(
                student.xPosition.value,
                student.yPosition.value,
                size.width.toFloat(),
                size.height.toFloat()
            )

            projectedOffset?.let { offset ->
                Box(
                    modifier = Modifier
                        .offset(
                            x = with(density) { offset.x.toDp() } - 50.dp,
                            y = with(density) { offset.y.toDp() } - 50.dp
                        )
                        .size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val glyphShader = remember { RuntimeShader(GhostVisionShader.VISION_GLYPH) }
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            glyphShader.setFloatUniform("iResolution", 100f, 100f)
                            glyphShader.setFloatUniform("iTime", time)
                            glyphShader.setFloatUniform("iPulse", pulse)
                            drawRect(ShaderBrush(glyphShader))
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = student.fullName.value.take(10),
                            color = Color.Cyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "ENERGY: ${(student.behaviorEntropy.value * 100).toInt()}%",
                            color = Color.Cyan.copy(alpha = 0.7f),
                            fontSize = 8.sp,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // 3. Compass / Orientation HUD
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    "AZIMUTH: ${Math.toDegrees(engine.azimuth.value.toDouble()).toInt()}°",
                    color = Color.Cyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "PITCH: ${Math.toDegrees(engine.pitch.value.toDouble()).toInt()}°",
                    color = Color.Cyan,
                    fontSize = 12.sp
                )
            }
        }
    }
}
