package com.example.myapplication.labs.ghost.mirage

import android.os.SystemClock
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

/**
 * GhostMirageEngine: Neural Focus Tracking Engine.
 *
 * This engine tracks the teacher's spatial attention by mapping touch interactions
 * (focus hits) into a persistent neural heatmap. This surfaces "spatial bias,"
 * helping educators identify which areas of the classroom receive the most (or least)
 * attention over time.
 *
 * Performance (Bolt ⚡):
 * - Uses a fixed 20x20 primitive FloatArray grid to represent the 4000x4000 logical canvas.
 * - Employs manual index-based loops for decay and synthesis to avoid iterator overhead.
 * - Implements a linear decay model to ensure focus "fades" naturally over time.
 */
class GhostMirageEngine {
    private val gridSize = 20
    private val cellCount = gridSize * gridSize
    private val grid = FloatArray(cellCount)
    private var lastUpdateTimestamp = SystemClock.elapsedRealtime()

    private val _heatmap = MutableStateFlow(FloatArray(cellCount))
    /** Flattened 20x20 grid representing focus intensities. */
    val heatmap: StateFlow<FloatArray> = _heatmap.asStateFlow()

    /**
     * Records a focus hit at the specified logical coordinates.
     *
     * @param x Seating chart X coordinate [0..4000].
     * @param y Seating chart Y coordinate [0..4000].
     * @param intensity The "weight" of this focus event. Defaults to 0.2f.
     */
    fun recordFocus(x: Float, y: Float, intensity: Float = 0.2f) {
        val col = (x / 4000f * gridSize).toInt().coerceIn(0, gridSize - 1)
        val row = (y / 4000f * gridSize).toInt().coerceIn(0, gridSize - 1)
        val index = row * gridSize + col

        synchronized(grid) {
            grid[index] = (grid[index] + intensity).coerceIn(0f, 1.0f)
        }
        triggerSync()
    }

    /**
     * Updates the focus grid by applying temporal decay.
     * Should be called periodically (e.g., from a background loop or on every UI update).
     *
     * @param decayRate The amount of intensity to decay per second. Defaults to 0.05f.
     */
    fun update(decayRate: Float = 0.05f) {
        val now = SystemClock.elapsedRealtime()
        val deltaTimeSeconds = (now - lastUpdateTimestamp) / 1000f
        lastUpdateTimestamp = now

        val decayAmount = decayRate * deltaTimeSeconds
        var changed = false

        synchronized(grid) {
            for (i in 0 until cellCount) {
                if (grid[i] > 0f) {
                    grid[i] = (grid[i] - decayAmount).coerceAtLeast(0f)
                    changed = true
                }
            }
        }

        if (changed) {
            triggerSync()
        }
    }

    /**
     * Resets the entire focus heatmap.
     */
    fun clear() {
        synchronized(grid) {
            for (i in 0 until cellCount) {
                grid[i] = 0f
            }
        }
        triggerSync()
    }

    private fun triggerSync() {
        val snapshot = synchronized(grid) { grid.copyOf() }
        _heatmap.value = snapshot
    }

    companion object {
        const val GRID_SIZE = 20
    }
}
