package com.example.myapplication.labs.ghost.adaptive

import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.*

/**
 * GhostAdaptiveEngine: Density-aware layout optimization for the seating chart.
 *
 * This engine analyzes the spatial distribution of students on the 4000x4000 logical canvas
 * to identify "Crowding Zones" (high-density clusters) and "Low-Pressure Zones" (empty areas).
 *
 * It provides the logic for "Ghost Adaptive UI" which suggests layout adjustments to
 * maintain social distancing or optimize classroom flow.
 */
object GhostAdaptiveEngine {

    /** The resolution of the density grid (e.g., 20x20 cells). */
    private const val GRID_SIZE = 20
    private const val CANVAS_SIZE = 4000f
    private const val CELL_SIZE = CANVAS_SIZE / GRID_SIZE

    data class DensityZone(
        val gridX: Int,
        val gridY: Int,
        val density: Float, // 0.0 to 1.0 (normalized)
        val centerX: Float,
        val centerY: Float
    )

    data class AdaptiveMove(
        val studentId: Long,
        val fromX: Float,
        val fromY: Float,
        val toX: Float,
        val toY: Float,
        val pressure: Float // 0.0 to 1.0
    )

    /**
     * Calculates density metrics across the classroom.
     *
     * @param students The current list of students.
     * @return A list of [DensityZone] objects representing the classroom's spatial pressure.
     */
    fun calculateDensityMetrics(students: List<StudentUiItem>): List<DensityZone> {
        val grid = FloatArray(GRID_SIZE * GRID_SIZE)
        if (students.isEmpty()) return emptyList()

        // 1. Accumulate density
        // BOLT: Manual loop to avoid iterator allocation in high-frequency engine
        for (i in students.indices) {
            val student = students[i]
            val x = student.xPosition.value.coerceIn(0f, CANVAS_SIZE - 1f)
            val y = student.yPosition.value.coerceIn(0f, CANVAS_SIZE - 1f)
            val gx = (x / CELL_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)
            val gy = (y / CELL_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)

            // Main cell
            grid[gy * GRID_SIZE + gx] += 1.0f

            // Soften the density by contributing to neighbors (simple blur)
            // BOLT: Direct bounds checking to eliminate neighbor list allocation
            if (gx > 0) grid[gy * GRID_SIZE + (gx - 1)] += 0.3f
            if (gx < GRID_SIZE - 1) grid[gy * GRID_SIZE + (gx + 1)] += 0.3f
            if (gy > 0) grid[(gy - 1) * GRID_SIZE + gx] += 0.3f
            if (gy < GRID_SIZE - 1) grid[(gy + 1) * GRID_SIZE + gx] += 0.3f
        }

        // 2. Normalize and create zones
        val maxDensity = grid.maxOrNull() ?: 1.0f
        val zones = mutableListOf<DensityZone>()

        for (gy in 0 until GRID_SIZE) {
            for (gx in 0 until GRID_SIZE) {
                val d = grid[gy * GRID_SIZE + gx]
                if (d > 0.1f) {
                    zones.add(
                        DensityZone(
                            gridX = gx,
                            gridY = gy,
                            density = (d / maxDensity).coerceIn(0f, 1f),
                            centerX = gx * CELL_SIZE + CELL_SIZE / 2f,
                            centerY = gy * CELL_SIZE + CELL_SIZE / 2f
                        )
                    )
                }
            }
        }
        return zones
    }

    /**
     * Proposes layout adjustments to alleviate "Crowding Zones".
     *
     * @param students The current list of students.
     * @param zones Pre-calculated [DensityZone] metrics.
     * @return A list of proposed [AdaptiveMove]s.
     */
    fun proposeAdaptiveLayout(
        students: List<StudentUiItem>,
        zones: List<DensityZone>
    ): List<AdaptiveMove> {
        val proposals = mutableListOf<AdaptiveMove>()
        // BOLT: Use a flat FloatArray for density lookup instead of a Map with Pair keys
        // to eliminate thousands of object allocations (Pair, Map.Entry, etc.).
        val densityGrid = FloatArray(GRID_SIZE * GRID_SIZE)
        for (i in zones.indices) {
            val zone = zones[i]
            densityGrid[zone.gridY * GRID_SIZE + zone.gridX] = zone.density
        }

        for (i in students.indices) {
            val student = students[i]
            val x = student.xPosition.value
            val y = student.yPosition.value
            val gx = (x / CELL_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)
            val gy = (y / CELL_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)

            val currentDensity = densityGrid[gy * GRID_SIZE + gx]

            // Only suggest moves for students in high-pressure areas (> 0.7 density)
            if (currentDensity > 0.7f) {
                // Find nearest low-pressure neighbor
                var bestNx = gx
                var bestNy = gy
                var minDensity = currentDensity

                for (dx in -1..1) {
                    for (dy in -1..1) {
                        if (dx == 0 && dy == 0) continue
                        val nx = (gx + dx).coerceIn(0, GRID_SIZE - 1)
                        val ny = (gy + dy).coerceIn(0, GRID_SIZE - 1)
                        val nDensity = densityGrid[ny * GRID_SIZE + nx]

                        if (nDensity < minDensity) {
                            minDensity = nDensity
                            bestNx = nx
                            bestNy = ny
                        }
                    }
                }

                if (bestNx != gx || bestNy != gy) {
                    proposals.add(
                        AdaptiveMove(
                            studentId = student.id.toLong(),
                            fromX = x,
                            fromY = y,
                            toX = bestNx * CELL_SIZE + CELL_SIZE / 2f,
                            toY = bestNy * CELL_SIZE + CELL_SIZE / 2f,
                            pressure = currentDensity
                        )
                    )
                }
            }
        }
        return proposals.sortedByDescending { it.pressure }
    }
}
