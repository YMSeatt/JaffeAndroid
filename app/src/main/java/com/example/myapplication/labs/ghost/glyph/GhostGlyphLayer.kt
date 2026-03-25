package com.example.myapplication.labs.ghost.glyph

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.example.myapplication.ui.model.StudentUiItem
import kotlinx.coroutines.launch

/**
 * GhostGlyphLayer: A Compose layer for neural gesture logging.
 *
 * It intercepts touch events on the seating chart, draws a glowing "Neural Ink"
 * trail, and triggers behavioral or academic logs when a glyph is recognized.
 */
@Composable
fun GhostGlyphLayer(
    students: List<StudentUiItem>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean,
    onLogBehavior: (Long, String) -> Unit,
    onLogAcademic: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var recognizedGlyph by remember { mutableStateOf(GhostGlyphEngine.GlyType.UNKNOWN) }
    var glyphFade by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    // BOLT: Neural Ink Animation
    val infiniteTransition = rememberInfiniteTransition(label = "NeuralInk")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RuntimeShader(GhostGlyphShader.NEURAL_INK_SHADER)
        } else null
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(isActive) {
                detectDragGestures(
                    onDragStart = {
                        currentPath = listOf(it)
                        glyphFade = 1f
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        currentPath = currentPath + change.position
                    },
                    onDragEnd = {
                        val glyph = GhostGlyphEngine.recognizeGlyph(currentPath)
                        recognizedGlyph = glyph

                        if (glyph != GhostGlyphEngine.GlyType.UNKNOWN) {
                            val startPoint = currentPath.first()
                            val student = findStudentAtPoint(startPoint, students, canvasScale, canvasOffset)

                            student?.let {
                                val sId = it.id.toLong()
                                when (glyph) {
                                    GhostGlyphEngine.GlyType.POSITIVE -> onLogBehavior(sId, "Positive (✔ Glyph)")
                                    GhostGlyphEngine.GlyType.NEGATIVE -> onLogBehavior(sId, "Negative (✖ Glyph)")
                                    GhostGlyphEngine.GlyType.ACADEMIC -> onLogAcademic(sId, "Academic (▲ Glyph)")
                                    else -> {}
                                }
                            }
                        }

                        scope.launch {
                            animate(
                                initialValue = 1f,
                                targetValue = 0f,
                                animationSpec = tween(500)
                            ) { value, _ ->
                                glyphFade = value
                                if (value == 0f) {
                                    // BOLT: Only clear path if we haven't started a new one
                                    if (glyphFade == 0f) {
                                        currentPath = emptyList()
                                        recognizedGlyph = GhostGlyphEngine.GlyType.UNKNOWN
                                    }
                                }
                            }
                        }
                    }
                )
            }
            .graphicsLayer {
                if (shader != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    shader.setFloatUniform("resolution", size.width, size.height)
                    shader.setFloatUniform("time", time)

                    // BOLT: Fixed-size uniform array (64 floats for 32 float2 elements)
                    val shaderPoints = currentPath.takeLast(32)
                    val pointsArray = FloatArray(64) { 0f }
                    shaderPoints.forEachIndexed { i, offset ->
                        pointsArray[i * 2] = offset.x
                        pointsArray[i * 2 + 1] = offset.y
                    }

                    shader.setFloatUniform("points", pointsArray)
                    shader.setIntUniform("pointCount", shaderPoints.size)
                    shader.setFloatUniform("intensity", if (recognizedGlyph != GhostGlyphEngine.GlyType.UNKNOWN) 20f else 10f)
                }
            }
    ) {
        if (shader != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && currentPath.size >= 2) {
            drawRect(brush = ShaderBrush(shader), alpha = glyphFade)
        }
    }
}

private fun findStudentAtPoint(
    point: Offset,
    students: List<StudentUiItem>,
    scale: Float,
    offset: Offset
): StudentUiItem? {
    return students.find { student ->
        val studentX = student.xPosition.value * scale + offset.x
        val studentY = student.yPosition.value * scale + offset.y
        val dx = studentX - point.x
        val dy = studentY - point.y
        (dx * dx + dy * dy) < (100f * scale * 100f * scale)
    }
}
