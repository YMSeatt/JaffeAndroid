package com.example.myapplication.labs.ghost.quasar

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem

@Composable
fun GhostQuasarLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>,
    canvasScale: Float,
    canvasOffset: androidx.compose.ui.geometry.Offset,
    isActive: Boolean
) {
    if (!isActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val quasars = remember(students, behaviorLogs) {
        GhostQuasarEngine.identifyQuasars(students, behaviorLogs)
    }

    if (quasars.isEmpty()) return

    val shaders = remember(quasars.size) {
        quasars.map { RuntimeShader(GhostQuasarShader.ACCRETION_DISK) }
    }

    var time by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        val startTime = System.nanoTime()
        while (true) {
            withFrameNanos { frameTime ->
                time = (frameTime - startTime) / 1_000_000_000f
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        quasars.forEachIndexed { index, quasar ->
            if (index < shaders.size) {
                val shader = shaders[index]
                val color = if (quasar.behaviorPolarity >= 0) {
                    Color.Cyan
                } else {
                    Color.Magenta
                }

                // Map world coordinates to screen space for the shader
                val screenX = (quasar.x * canvasScale) + canvasOffset.x
                val screenY = (quasar.y * canvasScale) + canvasOffset.y

                // Calculate the screen-space size of the effect
                // World-space radius of 250f (500f diameter)
                val screenRadius = 250f * canvasScale

                shader.setFloatUniform("iResolution", size.width, size.height)
                shader.setFloatUniform("iTime", time)
                shader.setFloatUniform("iCenter", screenX, screenY)
                shader.setFloatUniform("iEnergy", quasar.energy)
                shader.setFloatUniform("iColor", color.red, color.green, color.blue)

                drawRect(
                    brush = ShaderBrush(shader),
                    topLeft = androidx.compose.ui.geometry.Offset(screenX - screenRadius, screenY - screenRadius),
                    size = androidx.compose.ui.geometry.Size(screenRadius * 2, screenRadius * 2)
                )
            }
        }
    }
}
