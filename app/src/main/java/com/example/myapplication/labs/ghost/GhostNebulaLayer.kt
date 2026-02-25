package com.example.myapplication.labs.ghost

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostNebulaLayer: A futuristic background layer that renders the gaseous nebula.
 */
@Composable
fun GhostNebulaLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val nebulaShader = remember { RuntimeShader(GhostNebulaShader.NEBULA_CORE) }
    val (globalIntensity, clusters) = remember(students, behaviorLogs) {
        GhostNebulaEngine.calculateNebula(students, behaviorLogs)
    }

    var time by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            withInfiniteAnimationFrameMillis {
                time = it / 1000f
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        nebulaShader.setFloatUniform("iResolution", size.width, size.height)
        nebulaShader.setFloatUniform("iTime", time)
        nebulaShader.setFloatUniform("iGlobalIntensity", globalIntensity)

        val clusterData = FloatArray(40) // 10 clusters * 4 floats
        clusters.take(10).forEachIndexed { index, cluster ->
            // Convert coordinate system if necessary, but here we assume canvas space
            clusterData[index * 4 + 0] = cluster.x
            clusterData[index * 4 + 1] = cluster.y
            clusterData[index * 4 + 2] = cluster.density
            clusterData[index * 4 + 3] = cluster.colorIndex
        }

        nebulaShader.setFloatUniform("iClusters", clusterData)
        nebulaShader.setIntUniform("iClusterCount", clusters.size.coerceAtMost(10))

        drawRect(brush = ShaderBrush(nebulaShader))
    }
}
