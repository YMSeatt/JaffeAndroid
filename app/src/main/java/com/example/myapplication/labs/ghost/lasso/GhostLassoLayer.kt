package com.example.myapplication.labs.ghost.lasso

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
 * GhostLassoLayer: A gesture-interception layer for multi-student selection.
 *
 * Teachers can draw a lasso path around students to select them.
 * Uses AGSL for visual feedback and [GhostLassoEngine] for geometric intersection.
 */
@Composable
fun GhostLassoLayer(
    students: List<StudentUiItem>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean,
    onSelectionChange: (Set<Long>) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val lassoPath = remember { mutableStateListOf<Offset>() }
    var lassoFade by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    // BOLT: Pre-allocate buffers and hoisted state to avoid per-frame allocations.
    val pointsArray = remember { FloatArray(64) }

    val infiniteTransition = rememberInfiniteTransition(label = "GhostLasso")
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
            RuntimeShader(GhostLassoShader.LASSO_SHADER)
        } else null
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(isActive) {
                detectDragGestures(
                    onDragStart = {
                        lassoPath.clear()
                        lassoPath.add(it)
                        lassoFade = 1f
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        lassoPath.add(change.position)
                    },
                    onDragEnd = {
                        // Resample path for selection logic
                        val simplifiedPath = GhostLassoEngine.resample(lassoPath, 50)

                        val selectedIds = mutableSetOf<Long>()
                        val studentCount = students.size
                        for (i in 0 until studentCount) {
                            val student = students[i]
                            val studentPos = Offset(
                                student.xPosition.value * canvasScale + canvasOffset.x,
                                student.yPosition.value * canvasScale + canvasOffset.y
                            )
                            if (GhostLassoEngine.contains(simplifiedPath, studentPos)) {
                                selectedIds.add(student.id.toLong())
                            }
                        }

                        if (selectedIds.isNotEmpty()) {
                            onSelectionChange(selectedIds)
                        }

                        // Fade out the lasso
                        scope.launch {
                            animate(
                                initialValue = 1f,
                                targetValue = 0f,
                                animationSpec = tween(800)
                            ) { value, _ ->
                                lassoFade = value
                                if (value == 0f) {
                                    lassoPath.clear()
                                }
                            }
                        }
                    }
                )
            }
            .graphicsLayer {
                if (shader != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    shader.setFloatUniform("iResolution", size.width, size.height)
                    shader.setFloatUniform("iTime", time)
                    shader.setFloatUniform("iAlpha", lassoFade)

                    // BOLT: Zero-allocation resampling directly into remembered buffer.
                    val pointCount = 32
                    GhostLassoEngine.resampleToBuffer(lassoPath, pointCount, pointsArray)

                    shader.setFloatUniform("points", pointsArray)
                    shader.setIntUniform("pointCount", pointCount)
                }
            }
    ) {
        if (shader != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && lassoPath.size >= 3) {
            drawRect(brush = ShaderBrush(shader))
        }
    }
}
