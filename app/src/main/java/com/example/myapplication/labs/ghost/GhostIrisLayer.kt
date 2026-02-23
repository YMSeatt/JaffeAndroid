package com.example.myapplication.labs.ghost

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush

/**
 * GhostIrisLayer: Renders the personalized Neural Iris for a student icon.
 */
@Composable
fun GhostIrisLayer(
    params: GhostIrisEngine.IrisParameters,
    modifier: Modifier = Modifier
) {
    if (Build.VERSION.SDK_INT < 33) return

    val infiniteTransition = rememberInfiniteTransition(label = "irisTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val shader = remember { RuntimeShader(GhostIrisShader.NEURAL_IRIS) }

    Canvas(modifier = modifier.fillMaxSize()) {
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        shader.setFloatUniform("iSeed", params.seed)

        val cA = params.colorA
        shader.setFloatUniform("iColorA", cA.red, cA.green, cA.blue)

        val cB = params.colorB
        shader.setFloatUniform("iColorB", cB.red, cB.green, cB.blue)

        shader.setFloatUniform("iComplexity", params.complexity)

        drawRect(brush = ShaderBrush(shader))
    }
}
