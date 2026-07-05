package com.example.myapplication.labs.ghost.moss

import android.graphics.RuntimeShader
import android.os.Build
import android.util.LongSparseArray
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostMossLayer: Renders digital moss around dormant student icons.
 *
 * BOLT: Optimized for API 33+ with shader pooling and zero-allocation draw loop.
 */
@Composable
fun GhostMossLayer(
    students: List<StudentUiItem>,
    mossScores: LongSparseArray<Float>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val infiniteTransition = rememberInfiniteTransition(label = "moss_anim")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // BOLT: Hoist shader and brush to avoid per-frame allocations
    val mossShader = remember { RuntimeShader(GhostMossShader.MOSS_TEXTURE) }
    val mossBrush = remember(mossShader) { ShaderBrush(mossShader) }
    val mossColor = Color(0xFF2E7D32) // Material Forest Green

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Apply global canvas transformations
        withTransform({
            translate(canvasOffset.x, canvasOffset.y)
            scale(canvasScale, canvasScale, pivot = Offset.Zero)
        }) {
            // BOLT: Use manual index-based loop for performance
            for (i in students.indices) {
                val student = students[i]
                val score = mossScores.get(student.id.toLong()) ?: 0f

                if (score > 0.05f) {
                    val x = student.xPosition.value
                    val y = student.yPosition.value
                    val w = student.displayWidth.value.value
                    val h = student.displayHeight.value.value

                    // Moss bounds slightly larger than the student icon
                    val padding = 40f * score
                    val mossWidth = w + padding * 2
                    val mossHeight = h + padding * 2

                    mossShader.setFloatUniform("iResolution", mossWidth, mossHeight)
                    mossShader.setFloatUniform("iTime", time)
                    mossShader.setFloatUniform("iDormancy", score)
                    mossShader.setFloatUniform("iColor", mossColor.red, mossColor.green, mossColor.blue, mossColor.alpha)

                    withTransform({
                        translate(x - padding, y - padding)
                    }) {
                        drawRect(
                            brush = mossBrush,
                            size = androidx.compose.ui.geometry.Size(mossWidth, mossHeight)
                        )
                    }
                }
            }
        }
    }
}
