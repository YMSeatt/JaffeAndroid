package com.example.myapplication.labs.ghost

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostLensLayer: A high-fidelity predictive overlay.
 *
 * It uses [android.graphics.RenderEffect] to apply the [GhostLensShader.GHOST_LENS]
 * to its children. It also displays "Prophecies" for students identified by the [engine].
 */
@Composable
fun GhostLensLayer(
    engine: GhostLensEngine,
    students: List<StudentUiItem>,
    allProphecies: List<GhostOracle.Prophecy>,
    canvasScale: Float,
    canvasOffset: Offset,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val lensPos by engine.lensPosition.collectAsState()
    val lensRadius by engine.lensRadius.collectAsState()

    val currentProphecies = remember(lensPos, students, allProphecies) {
        engine.getPropheciesForStudentsUnderLens(students, allProphecies, canvasScale, canvasOffset)
    }

    val lensShader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RuntimeShader(GhostLensShader.GHOST_LENS)
        } else null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    engine.updatePosition(lensPos + dragAmount)
                }
            }
    ) {
        // 1. Refracted Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && lensShader != null) {
                        lensShader.setFloatUniform("iResolution", size.width, size.height)
                        lensShader.setFloatUniform("iLensPos", lensPos.x, lensPos.y)
                        lensShader.setFloatUniform("iLensRadius", lensRadius)
                        lensShader.setFloatUniform("iMagnification", 0.7f)

                        renderEffect = RenderEffect.createRuntimeShaderEffect(lensShader, "child")
                            .asComposeRenderEffect()
                    }
                }
        ) {
            content()
        }

        // 2. Tactical Overlay (Non-refracted)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    onDrawWithContent {
                        // Draw a subtle tactical ring
                        drawCircle(
                            color = Color.Cyan.copy(alpha = 0.3f),
                            radius = lensRadius,
                            center = lensPos,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
        ) {
            // Overlay Prophecies near the lens
            if (currentProphecies.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .graphicsLayer {
                            translationX = (lensPos.x + lensRadius + 10f).coerceAtMost(size.width - 250.dp.toPx())
                            translationY = (lensPos.y - 50f).coerceAtLeast(10f)
                        },
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    androidx.compose.foundation.layout.Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "GHOST PREDICTION",
                            color = Color.Cyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        currentProphecies.take(2).forEach { prophecy ->
                            Text(
                                text = "• ${prophecy.description}",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
