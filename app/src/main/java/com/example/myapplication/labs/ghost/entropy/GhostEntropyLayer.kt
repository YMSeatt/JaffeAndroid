package com.example.myapplication.labs.ghost.entropy

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.QuizLog
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostEntropyLayer: A visualization layer that applies "Neural Entropy" distortion.
 *
 * This layer uses [android.graphics.RenderEffect] and [GhostEntropyShader] to distort
 * the seating chart based on the calculated entropy of the students.
 */
@Composable
fun GhostEntropyLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    quizLogs: List<QuizLog>,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Box(modifier = modifier.fillMaxSize()) {
            content()
        }
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "entropyTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(50000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // BOLT: Entropy is now pre-calculated in the SeatingChartViewModel's memoized Stage 2
    // and synced to StudentUiItem, eliminating the redundant O(N*L) calculation here.
    val maxEntropy = remember(students) {
        var maxE = 0f
        for (i in 0 until students.size.coerceAtMost(20)) {
            val e = students[i].behaviorEntropy.value
            if (e > maxE) maxE = e
        }
        maxE
    }

    val entropyShader = remember { RuntimeShader(GhostEntropyShader.ENTROPY_DISTORTION) }

    // BOLT: Cache RenderEffect to avoid O(1) allocation in the graphicsLayer block per frame.
    // Tied to maxEntropy as that's the primary driver of the effect's state.
    val cachedRenderEffect = remember(maxEntropy) {
        if (maxEntropy > 0.1f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RenderEffect.createRuntimeShaderEffect(entropyShader, "contents")
                .asComposeRenderEffect()
        } else null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                if (maxEntropy > 0.1f && cachedRenderEffect != null) {
                    entropyShader.setFloatUniform("iResolution", size.width, size.height)
                    entropyShader.setFloatUniform("iTime", time)
                    entropyShader.setFloatUniform("iEntropy", maxEntropy)

                    renderEffect = cachedRenderEffect
                }
            }
    ) {
        content()
    }
}
