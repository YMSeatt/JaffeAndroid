package com.example.myapplication.labs.ghost

import android.content.Context
import android.graphics.RuntimeShader
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.labs.ghost.GhostOracle
import kotlinx.coroutines.delay

@Composable
fun GhostHUDLayer(
    hudViewModel: GhostHUDViewModel,
    students: List<StudentUiItem>,
    prophecies: List<GhostOracle.Prophecy>,
    modifier: Modifier = Modifier
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val heading by hudViewModel.heading.collectAsState()
    val targetAngles by hudViewModel.targetAngles.collectAsState()
    val targetScores by hudViewModel.targetScores.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "hudPulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    LaunchedEffect(students, prophecies) {
        hudViewModel.updateTargets(students, prophecies)
    }

    var lastVibrationTime by remember { mutableLongStateOf(0L) }
    LaunchedEffect(heading, targetAngles) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastVibrationTime < 1000L) return@LaunchedEffect

        targetAngles.forEachIndexed { index, targetAngle ->
            var diff = heading - targetAngle
            while (diff < -Math.PI) diff += 2 * Math.PI
            while (diff > Math.PI) diff -= 2 * Math.PI

            if (Math.abs(diff) < 0.1) {
                triggerHapticFeedback(context)
                lastVibrationTime = currentTime
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val shader = RuntimeShader(GhostHUDShader.TACTICAL_RADAR)
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        shader.setFloatUniform("iHeading", heading)
        shader.setIntUniform("iTargetCount", targetAngles.size)

        val targetArray = FloatArray(10)
        val scoreArray = FloatArray(10)
        targetAngles.take(10).forEachIndexed { i, angle -> targetArray[i] = angle }
        targetScores.take(10).forEachIndexed { i, score -> scoreArray[i] = score }

        shader.setFloatUniform("iTargets", targetArray)
        shader.setFloatUniform("iTargetScores", scoreArray)

        drawRect(brush = ShaderBrush(shader))
    }
}

private fun triggerHapticFeedback(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(50)
    }
}
