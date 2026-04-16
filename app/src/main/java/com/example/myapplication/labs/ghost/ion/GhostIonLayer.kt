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
import com.example.myapplication.labs.ghost.GhostConfig
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostIonLayer: Renders the "Neural Ionization" AGSL effect.
 *
 * This layer visualizes the classroom's "Ionic Field" using procedural AGSL shaders.
 * It maps student behavior and hardware signals (Battery Temperature) to glowing
 * "Ion Cores" and "Atmospheric Haze."
 *
 * BOLT: Optimized for 60fps performance by offloading student-specific metrics
 * to the background pipeline (`SeatingChartViewModel`) and using direct `MutableState`
 * reads for fluid tracking during student drag-and-drop.
 *
 * @param students Current list of students to visualize.
 * @param globalBalance The normalized classroom-wide ion balance (-1.0 to 1.0).
 * @param canvasScale The current zoom level of the seating chart.
 * @param canvasOffset The current pan offset of the seating chart.
 */
@Composable
fun GhostIonLayer(
    students: List<StudentUiItem>,
    globalBalance: Float,
    canvasScale: Float,
    canvasOffset: Offset,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !GhostConfig.ION_MODE_ENABLED) return

    val context = LocalContext.current
    // BOLT: Hardware signal is handled once per recomposition, which is infrequent.
    val batteryTemp = remember { GhostIonEngine.getBatteryTemperature(context) }
    val tempFactor = remember(batteryTemp) { (batteryTemp - 25f).coerceIn(0f, 20f) / 20f }

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

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { RuntimeShader(GhostIonShader.ION_FIELD) }
        val brush = remember(shader) { ShaderBrush(shader) }
        // BOLT: Pre-allocate and remember the FloatArray to eliminate per-frame object churn.
        val pointsArray = remember { FloatArray(10 * 4) }

        Canvas(modifier = modifier.fillMaxSize()) {
            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iTime", time)
            shader.setFloatUniform("iGlobalBalance", globalBalance)

            // BOLT: Clear the buffer to avoid stale data.
            pointsArray.fill(0f)

            // BOLT: Read metrics directly from StudentUiItem MutableState. This allows
            // 60fps fluid tracking during student drag operations as positions and
            // charges follow the icon without whole-layer recomposition.
            var activePoints = 0
            for (i in students.indices) {
                if (activePoints >= 10) break
                val student = students[i]
                val baseDensity = student.ionDensity.value
                if (baseDensity < 0.1f) continue

                val charge = student.ionCharge.value
                val finalDensity = (baseDensity + tempFactor * 0.3f).coerceIn(0f, 1.0f)

                // Map logical (4000x4000) to screen space
                val screenX = student.xPosition.value * canvasScale + canvasOffset.x
                val screenY = student.yPosition.value * canvasScale + canvasOffset.y

                pointsArray[activePoints * 4 + 0] = screenX
                pointsArray[activePoints * 4 + 1] = screenY
                pointsArray[activePoints * 4 + 2] = charge
                pointsArray[activePoints * 4 + 3] = finalDensity
                activePoints++
            }

            shader.setFloatUniform("iPoints", pointsArray)
            shader.setIntUniform("iPointCount", activePoints)

            drawRect(brush = brush)
        }
    }
}
