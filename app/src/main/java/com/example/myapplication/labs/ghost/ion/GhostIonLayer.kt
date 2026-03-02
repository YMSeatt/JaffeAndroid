package com.example.myapplication.labs.ghost.ion

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.labs.ghost.GhostConfig
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostIonLayer: Renders the "Neural Ionization" AGSL effect.
 *
 * @param students Current students.
 * @param behaviorLogs Current behavior logs.
 * @param canvasScale Current zoom level.
 * @param canvasOffset Current pan offset.
 */
@Composable
fun GhostIonLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    canvasScale: Float,
    canvasOffset: Offset,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !GhostConfig.ION_MODE_ENABLED) return

    val context = LocalContext.current
    val batteryTemp = remember { GhostIonEngine.getBatteryTemperature(context) }

    val infiniteTransition = rememberInfiniteTransition(label = "ionPulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(50000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val ionPoints = remember(students, behaviorLogs, batteryTemp) {
        GhostIonEngine.calculateIonization(students, behaviorLogs, batteryTemp)
    }

    val globalBalance = remember(ionPoints) {
        GhostIonEngine.calculateGlobalBalance(ionPoints)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { RuntimeShader(GhostIonShader.ION_FIELD) }

        Canvas(modifier = modifier.fillMaxSize()) {
            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iTime", time)
            shader.setFloatUniform("iGlobalBalance", globalBalance)

            val pointCount = ionPoints.size.coerceAtMost(10)
            val pointsArray = FloatArray(10 * 4)

            for (i in 0 until 10) {
                if (i < pointCount) {
                    val p = ionPoints[i]
                    // Map logical (4000x4000) to screen space
                    val screenX = p.x * canvasScale + canvasOffset.x
                    val screenY = p.y * canvasScale + canvasOffset.y

                    pointsArray[i * 4 + 0] = screenX
                    pointsArray[i * 4 + 1] = screenY
                    pointsArray[i * 4 + 2] = p.charge
                    pointsArray[i * 4 + 3] = p.density
                } else {
                    pointsArray[i * 4 + 0] = 0f
                    pointsArray[i * 4 + 1] = 0f
                    pointsArray[i * 4 + 2] = 0f
                    pointsArray[i * 4 + 3] = 0f
                }
            }

            shader.setFloatUniform("iPoints", pointsArray)
            shader.setIntUniform("iPointCount", pointCount)

            drawRect(brush = ShaderBrush(shader))
        }
    }
}
