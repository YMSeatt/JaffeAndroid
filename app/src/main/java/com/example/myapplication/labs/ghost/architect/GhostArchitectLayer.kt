package com.example.myapplication.labs.ghost.architect

import android.graphics.RuntimeShader
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.labs.ghost.lattice.GhostLatticeEngine
import com.example.myapplication.data.BehaviorEvent

/**
 * GhostArchitectLayer: Visualizes the generative layout strategies proposed by the
 * [GhostArchitectEngine] using AGSL Shaders and Android 15 haptics.
 *
 * BOLT ⚡ Optimization:
 * 1. Pre-allocates and 'remember's RuntimeShader instances to avoid per-frame allocations.
 * 2. Uses a pool of trajectory shaders to handle multiple proposed moves without object churn.
 */
@Composable
fun GhostArchitectLayer(
    students: List<StudentUiItem>,
    edges: List<GhostLatticeEngine.Edge>,
    behaviorLogs: List<BehaviorEvent>,
    goal: GhostArchitectEngine.StrategicGoal,
    canvasScale: Float,
    canvasOffset: Offset,
    isActive: Boolean
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    val proposedMoves = remember(students, edges, behaviorLogs, goal) {
        GhostArchitectEngine.proposeLayout(students, edges, behaviorLogs, goal)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ArchitectPulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(1000),
        label = "LayerAlpha"
    )

    // BOLT: Remembered shaders to avoid allocation in draw loop
    val blueprintShader = remember { RuntimeShader(GhostArchitectShader.BLUEPRINT_SHADER) }
    val trajectoryShaderPool = remember { List(20) { RuntimeShader(GhostArchitectShader.TRAJECTORY_SHADER) } }

    // Trigger "Architectural Locking" haptics when moves are recalculated.
    // Uses a multi-stage composition (Click -> Tick) to simulate the feeling of
    // structural elements snapping into a new generative alignment.
    LaunchedEffect(proposedMoves) {
        if (proposedMoves.isNotEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val composition = VibrationEffect.startComposition()
            composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.8f)
            composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.4f, 100)
            vibrator?.vibrate(composition.compose())
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Blueprint Grid Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            blueprintShader.setFloatUniform("uResolution", size.width, size.height)
            blueprintShader.setFloatUniform("uTime", time)
            blueprintShader.setFloatUniform("uAlpha", alpha)

            drawRect(brush = ShaderBrush(blueprintShader))
        }

        // 2. Trajectory Beams
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = canvasScale,
                    scaleY = canvasScale,
                    translationX = canvasOffset.x,
                    translationY = canvasOffset.y
                )
        ) {
            proposedMoves.take(trajectoryShaderPool.size).forEachIndexed { index, move ->
                val shader = trajectoryShaderPool[index]
                shader.setFloatUniform("uStart", move.currentX, move.currentY)
                shader.setFloatUniform("uEnd", move.proposedX, move.proposedY)
                shader.setFloatUniform("uTime", time)
                shader.setFloatUniform("uWeight", move.weight)

                val dx = move.proposedX - move.currentX
                val dy = move.proposedY - move.currentY

                drawRect(
                    brush = ShaderBrush(shader),
                    topLeft = Offset(Math.min(move.currentX, move.proposedX) - 100, Math.min(move.currentY, move.proposedY) - 100),
                    size = androidx.compose.ui.geometry.Size(Math.abs(dx) + 200, Math.abs(dy) + 200)
                )
            }
        }
    }
}
