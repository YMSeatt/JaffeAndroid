package com.example.myapplication.labs.ghost.orbit

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.input.pointer.pointerInput
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.labs.ghost.GhostConfig
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * GhostOrbitLayer: Renders the "Classroom Galaxy" visualization.
 *
 * It transforms the traditional seating chart into a dynamic orbital system where
 * students move along paths determined by their engagement and stability.
 *
 * This layer uses high-performance AGSL shaders for background nebula rendering
 * and interactive gravity well effects.
 *
 * @param students The list of students to visualize as orbiting planets.
 * @param behaviorLogs The log history used to drive orbital parameters (speed/radius).
 * @param isActive Whether the layer is currently visible and animating.
 */
@Composable
fun GhostOrbitLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!GhostConfig.GHOST_MODE_ENABLED || !GhostConfig.ORBIT_MODE_ENABLED || !isActive) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val infiniteTransition = rememberInfiniteTransition(label = "orbitRotation")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(200000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Interactive Gravity Well state
    var gravityPoint by remember { mutableStateOf<Offset?>(null) }
    var gravityIntensity by remember { mutableFloatStateOf(0f) }

    // Aggregate system energy
    val systemEnergy = remember(behaviorLogs) {
        (behaviorLogs.size.toFloat() / 50f).coerceIn(0.1f, 1.0f)
    }

    // BOLT: Heavy calculations (grouping, filtering) are only done when data changes.
    val orbitalParams = remember(students, behaviorLogs) {
        GhostOrbitEngine.calculateOrbitalParameters(students, behaviorLogs)
    }

    val nebulaShader = remember { RuntimeShader(GhostOrbitShader.NEURAL_NEBULA) }
    val nebulaBrush = remember(nebulaShader) { ShaderBrush(nebulaShader) }

    val wellShader = remember { RuntimeShader(GhostOrbitShader.GRAVITY_WELL) }
    val wellBrush = remember(wellShader) { ShaderBrush(wellShader) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        gravityPoint = offset
                        gravityIntensity = 1.0f
                        tryAwaitRelease()
                        gravityIntensity = 0f
                        gravityPoint = null
                    }
                )
            }
    ) {
        // 1. Draw Nebula Background
        nebulaShader.setFloatUniform("iResolution", size.width, size.height)
        nebulaShader.setFloatUniform("iTime", time)
        nebulaShader.setFloatUniform("iSystemEnergy", systemEnergy)
        drawRect(brush = nebulaBrush)

        // 2. Draw Orbital Paths and Student "Planets"
        val scaleX = size.width / 4000f
        val scaleY = size.height / 4000f
        val twoPi = 2f * PI.toFloat()

        for (i in orbitalParams.indices) {
            val p = orbitalParams[i]

            // BOLT: Calculate orbital position on-the-fly to eliminate per-frame allocations
            val angle = (time * p.speed + (p.studentId * 0.785f)) % twoPi
            val orbitalX = p.centerX + cos(angle) * p.radius
            val orbitalY = p.centerY + sin(angle) * p.radius

            // Scale logical classroom coordinates (4000x4000) to screen coordinates
            val screenX = orbitalX * scaleX
            val screenY = orbitalY * scaleY
            val centerX = p.centerX * scaleX
            val centerY = p.centerY * scaleY

            // Draw orbital path
            drawCircle(
                color = Color.Cyan.copy(alpha = 0.05f * p.energy),
                radius = p.radius * scaleX,
                center = Offset(centerX, centerY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )

            // Draw Student Node (Planet)
            drawCircle(
                color = if (p.stability > 0.7f) Color.Green else Color.Red,
                radius = 15f + (p.energy * 10f),
                center = Offset(screenX, screenY)
            )

            // Optional: Draw a "Moon" for recent logs
            if (p.energy > 0.5f) {
                val moonAngle = time * 5f
                val moonX = screenX + 40f * cos(moonAngle)
                val moonY = screenY + 40f * sin(moonAngle)
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    radius = 5f,
                    center = Offset(moonX, moonY)
                )
            }
        }

        // 3. Draw Interaction Gravity Well
        gravityPoint?.let { point ->
            wellShader.setFloatUniform("iResolution", size.width, size.height)
            wellShader.setFloatUniform("iPosition", point.x, point.y)
            wellShader.setFloatUniform("iIntensity", gravityIntensity)
            wellShader.setFloatUniform("iRadius", 300f)
            drawRect(brush = wellBrush)
        }
    }
}
