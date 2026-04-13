package com.example.myapplication.labs.ghost.emergence

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import kotlinx.coroutines.delay

/**
 * GhostEmergenceLayer: Visualizes the Emergence simulation as an AGSL background layer.
 *
 * This component manages two separate loops:
 * 1. **Simulation Loop**: Updates the [GhostEmergenceEngine] every 100ms based on student data.
 * 2. **Animation Loop**: Updates the `iTime` uniform at ~60fps to drive shader-based organic noise.
 *
 * @param engine The [GhostEmergenceEngine] instance responsible for the simulation.
 * @param students The current list of students on the seating chart.
 * @param behaviorLogs The list of behavioral logs used to drive simulation impulses.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun GhostEmergenceLayer(
    engine: GhostEmergenceEngine,
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>
) {
    var grid by remember { mutableStateOf(engine.getGrid()) }
    var time by remember { mutableFloatStateOf(0f) }

    val shader = remember { RuntimeShader(GhostEmergenceShader.SHADER) }
    val brush = remember { ShaderBrush(shader) }

    LaunchedEffect(students, behaviorLogs) {
        while (true) {
            grid = engine.update(students, behaviorLogs)
            delay(100) // Update simulation every 100ms
        }
    }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (true) {
            time = (System.currentTimeMillis() - startTime) / 1000f
            delay(16) // ~60fps for visual animation
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        shader.setFloatUniform("iGrid", grid)

        drawRect(brush = brush)
    }
}
