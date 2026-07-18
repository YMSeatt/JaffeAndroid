package com.example.myapplication.labs.ghost

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostNebulaLayer: An upgraded, high-performance background layer that renders the 3D Volumetric Nebula.
 *
 * It manages the lifecycle of [GhostNebulaEngine] to stream gravity/accelerometer and light sensor events.
 * It translates these real-time physical properties and student data into uniforms for [GhostNebulaShader],
 * performing GPU-accelerated volumetric raymarching.
 */
@Composable
fun GhostNebulaLayer(
    students: List<StudentUiItem>,
    behaviorLogs: List<BehaviorEvent>
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val engine = remember(context) { GhostNebulaEngine(context) }

    // Bind sensor listener lifecycle to the composable lifecycle
    DisposableEffect(engine) {
        engine.start()
        onDispose {
            engine.stop()
        }
    }

    val tiltX by engine.tiltX.collectAsState()
    val tiltY by engine.tiltY.collectAsState()
    val depthFactor by engine.depthFactor.collectAsState()

    val (globalIntensity, clusters) = remember(students, behaviorLogs) {
        engine.calculateNebula(students, behaviorLogs)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "nebula3dTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(120000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val nebulaShader = remember { RuntimeShader(GhostNebulaShader.NEBULA_CORE) }
    val nebulaBrush = remember(nebulaShader) { ShaderBrush(nebulaShader) }

    // BOLT: Pre-allocate and remember the FloatArray to eliminate per-frame object churn.
    val clusterData = remember { FloatArray(40) }

    Canvas(modifier = Modifier.fillMaxSize()) {
        nebulaShader.setFloatUniform("iResolution", size.width, size.height)
        nebulaShader.setFloatUniform("iTime", time)
        nebulaShader.setFloatUniform("iGlobalIntensity", globalIntensity)

        // Pass the real-time smoothed sensor-fusion parameters
        nebulaShader.setFloatUniform("iTilt", tiltX, tiltY)
        nebulaShader.setFloatUniform("iDepthFactor", depthFactor)

        // BOLT: Clear the buffer to avoid stale data.
        clusterData.fill(0f)

        val count = clusters.size.coerceAtMost(10)
        // BOLT: Use manual loop to avoid iterator allocation.
        for (i in 0 until count) {
            val cluster = clusters[i]
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
