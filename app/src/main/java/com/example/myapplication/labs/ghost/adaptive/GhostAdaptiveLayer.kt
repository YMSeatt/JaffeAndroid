package com.example.myapplication.labs.ghost.adaptive

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.withInfiniteProperties
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.example.myapplication.labs.ghost.GhostConfig
import com.example.myapplication.labs.ghost.util.rememberInfiniteTime

/**
 * GhostAdaptiveLayer: Visualizes "Crowding Zones" using high-performance AGSL.
 *
 * @param zones The pre-calculated density metrics from [GhostAdaptiveEngine].
 * @param isActive Whether this experimental layer is enabled.
 */
@Composable
fun GhostAdaptiveLayer(
    zones: List<GhostAdaptiveEngine.DensityZone>,
    isActive: Boolean
) {
    if (!isActive || !GhostConfig.GHOST_MODE_ENABLED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val time = rememberInfiniteTime()
    val shader = remember { RuntimeShader(GhostAdaptiveShader.ADAPTIVE_HEATMAP) }
    val brush = remember { ShaderBrush(shader) }

    // BOLT: Reusable buffer for packed point uniforms to avoid per-frame allocations.
    // 25 points * 4 components (x, y, density, padding) = 100 floats
    val pointsBuffer = remember { FloatArray(100) }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        shader.setFloatUniform("iResolution", width, height)
        shader.setFloatUniform("iTime", time)

        // Only pass the top 25 high-density zones to stay within AGSL uniform limits
        val topZones = zones.sortedByDescending { it.density }.take(25)
        topZones.forEachIndexed { i, zone ->
            pointsBuffer[i * 4] = zone.centerX
            pointsBuffer[i * 4 + 1] = zone.centerY
            pointsBuffer[i * 4 + 2] = zone.density
            pointsBuffer[i * 4 + 3] = 0f // Padding
        }

        // BOLT: setFloatUniform with float array for uniform float4 array[25]
        shader.setFloatUniform("iDensityPoints", pointsBuffer)
        shader.setIntUniform("iPointCount", topZones.size)

        drawRect(brush = brush)
    }
}
