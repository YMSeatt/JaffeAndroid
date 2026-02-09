package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.max
import kotlin.math.min

/**
 * GhostChronosEngine: Spatio-temporal analysis engine for classroom behavior.
 *
 * It divides the seating chart into a grid and calculates "Behavioral Intensity"
 * for each cell based on historical events.
 */
object GhostChronosEngine {
    private const val GRID_SIZE = 10
    private const val CANVAS_SIZE = 4000f

    /**
     * Calculates a 10x10 grid of intensities.
     * Positive intensity = Positive behaviors.
     * Negative intensity = Negative behaviors.
     */
    fun calculateHeatmap(
        students: List<StudentUiItem>,
        events: List<BehaviorEvent>,
        timeWindowMs: Long = 7 * 24 * 60 * 60 * 1000L // Default 7 days
    ): FloatArray {
        val grid = FloatArray(GRID_SIZE * GRID_SIZE)
        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - timeWindowMs

        val filteredEvents = events.filter { it.timestamp in startTime..currentTime }
        val studentMap = students.associateBy { it.id.toLong() }

        filteredEvents.forEach { event ->
            val student = studentMap[event.studentId] ?: return@forEach

            // Calculate grid coordinates
            val x = (student.xPosition.value / CANVAS_SIZE * GRID_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)
            val y = (student.yPosition.value / CANVAS_SIZE * GRID_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)
            val index = y * GRID_SIZE + x

            val weight = when {
                event.type.contains("Positive", ignoreCase = true) -> 0.2f
                event.type.contains("Negative", ignoreCase = true) -> -0.3f
                else -> 0.05f
            }

            // Time decay (linear)
            val timeFactor = (event.timestamp - startTime).toFloat() / timeWindowMs.toFloat()

            grid[index] = (grid[index] + weight * timeFactor).coerceIn(-1f, 1f)
        }

        return grid
    }
}
