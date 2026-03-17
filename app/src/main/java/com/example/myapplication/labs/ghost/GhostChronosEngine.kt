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
     *
     * BOLT: Optimized to use manual loops and early breaks for DESC-sorted logs.
     * Accepts raw Student entities to avoid UI model overhead in background threads.
     */
    fun calculateHeatmap(
        students: List<com.example.myapplication.data.Student>,
        behaviorLogsByStudent: Map<Long, List<BehaviorEvent>>,
        timeWindowMs: Long = 7 * 24 * 60 * 60 * 1000L // Default 7 days
    ): FloatArray {
        val grid = FloatArray(GRID_SIZE * GRID_SIZE)
        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - timeWindowMs

        for (student in students) {
            val logs = behaviorLogsByStudent[student.id] ?: continue

            // Calculate grid coordinates once per student
            val x = (student.xPosition / CANVAS_SIZE * GRID_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)
            val y = (student.yPosition / CANVAS_SIZE * GRID_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)
            val index = y * GRID_SIZE + x

            // BOLT: Manual loop with early break for DESC sorted logs
            for (event in logs) {
                if (event.timestamp < startTime) break
                if (event.timestamp > currentTime) continue

                val weight = when {
                    event.type.contains("Positive", ignoreCase = true) -> 0.2f
                    event.type.contains("Negative", ignoreCase = true) -> -0.3f
                    else -> 0.05f
                }

                // Time decay (linear)
                val timeFactor = (event.timestamp - startTime).toFloat() / timeWindowMs.toFloat()
                grid[index] = (grid[index] + weight * timeFactor).coerceIn(-1f, 1f)
            }
        }

        return grid
    }

    /**
     * Compatibility overload for UI layers.
     */
    fun calculateHeatmap(
        students: List<StudentUiItem>,
        events: List<BehaviorEvent>
    ): FloatArray {
        val behaviorLogsByStudent = events.groupBy { it.studentId }
        val rawStudents = students.map { ui ->
            com.example.myapplication.data.Student(
                id = ui.id.toLong(),
                firstName = "", lastName = "", gender = "",
                xPosition = ui.xPosition.value,
                yPosition = ui.yPosition.value
            )
        }
        return calculateHeatmap(rawStudents, behaviorLogsByStudent)
    }
}
