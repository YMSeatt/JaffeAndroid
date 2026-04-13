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
    val nebulaBrush = remember(nebulaShader) { ShaderBrush(nebulaShader) }
    val (globalIntensity, clusters) = remember(students, behaviorLogs) {
        GhostNebulaEngine.calculateNebula(students, behaviorLogs)
    }

    // BOLT: Use rememberInfiniteTransition for better performance and consistency.
    val infiniteTransition = rememberInfiniteTransition(label = "nebulaTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // BOLT: Pre-allocate and remember the FloatArray to eliminate per-frame object churn.
    val clusterData = remember { FloatArray(40) }

    Canvas(modifier = Modifier.fillMaxSize()) {
        nebulaShader.setFloatUniform("iResolution", size.width, size.height)
        nebulaShader.setFloatUniform("iTime", time)
        nebulaShader.setFloatUniform("iGlobalIntensity", globalIntensity)

        // BOLT: Clear the buffer to avoid stale data.
        clusterData.fill(0f)

        val count = clusters.size.coerceAtMost(10)
        // BOLT: Use manual loop to avoid iterator allocation.
        for (i in 0 until count) {
            val cluster = clusters[i]
            // Convert coordinate system if necessary, but here we assume canvas space
            clusterData[i * 4 + 0] = cluster.x
            clusterData[i * 4 + 1] = cluster.y
            clusterData[i * 4 + 2] = cluster.density
            clusterData[i * 4 + 3] = cluster.colorIndex
        }

        nebulaShader.setFloatUniform("iClusters", clusterData)
        nebulaShader.setIntUniform("iClusterCount", count)

        drawRect(brush = nebulaBrush)
    }
}
