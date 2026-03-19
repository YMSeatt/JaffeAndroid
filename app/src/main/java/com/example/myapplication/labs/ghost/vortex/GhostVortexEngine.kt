package com.example.myapplication.labs.ghost.vortex

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.sqrt

/**
 * GhostVortexEngine: Identifies rotational behavior clusters in the classroom.
 *
 * This engine detects "Vortex" centers — areas where multiple students with
 * high behavioral activity are clustered together. It calculates the "Angular Momentum"
 * (intensity) of these vortices to drive spatial distortion effects.
 */
object GhostVortexEngine {

    /**
     * Represents a detected behavioral vortex.
     */
    data class VortexPoint(
        val x: Float,
        val y: Float,
        val momentum: Float, // Rotational intensity (0.0 to 1.0)
        val radius: Float,   // Influence radius
        val polarity: Float  // 1.0 for positive, -1.0 for negative
    )

    /**
     * Identifies behavioral vortices based on student proximity and log density.
     *
     * @param students List of students and their current positions.
     * @param behaviorLogs Historical behavior events.
     * @param windowMillis Time window for analysis (default 10 minutes).
     * @return A list of detected [VortexPoint]s.
     */
    fun identifyVortices(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>,
        windowMillis: Long = 600_000L
    ): List<VortexPoint> {
        if (students.isEmpty()) return emptyList()

        val currentTime = System.currentTimeMillis()
        val recentLogs = behaviorLogs.filter { currentTime - it.timestamp < windowMillis }

        // BOLT: Group logs by studentId for O(N + L) efficiency.
        val logsByStudent = recentLogs.groupBy { it.studentId }

        // Calculate student "Energy" (density of recent logs)
        val studentEnergy = students.associate { student ->
            val logs = logsByStudent[student.id.toLong()] ?: emptyList()
            val posCount = logs.count { !it.type.contains("Negative", ignoreCase = true) }
            val negCount = logs.count { it.type.contains("Negative", ignoreCase = true) }

            val netPolarity = if (negCount > posCount) -1.0f else 1.0f
            val intensity = (logs.size.toFloat() / 5.0f).coerceIn(0f, 1.0f)

            student.id to (intensity to netPolarity)
        }

        val vortices = mutableListOf<VortexPoint>()
        val clusterThreshold = 800f // Radius to consider students part of the same vortex

        // Simple clustering: Identify high-energy nodes and check neighbors
        students.forEach { student ->
            val energy = studentEnergy[student.id]?.first ?: 0f
            if (energy > 0.4f) {
                // Check if this student is already near an identified vortex
                val nearExisting = vortices.any { v ->
                    val dx = v.x - student.xPosition.value
                    val dy = v.y - student.yPosition.value
                    sqrt(dx * dx + dy * dy) < clusterThreshold
                }

                if (!nearExisting) {
                    // Find neighbors to calculate cluster momentum
                    val neighbors = students.filter { other ->
                        if (other.id == student.id) return@filter false
                        val dx = other.xPosition.value - student.xPosition.value
                        val dy = other.yPosition.value - student.yPosition.value
                        sqrt(dx * dx + dy * dy) < clusterThreshold
                    }

                    if (neighbors.isNotEmpty()) {
                        val neighborEnergy = neighbors.sumOf { (studentEnergy[it.id]?.first ?: 0f).toDouble() }.toFloat()
                        val avgPolarity = (neighbors.sumOf { (studentEnergy[it.id]?.second ?: 1.0f).toDouble() }.toFloat() + (studentEnergy[student.id]?.second ?: 1.0f)) / (neighbors.size + 1)

                        val momentum = ((energy + neighborEnergy) / (neighbors.size + 1)).coerceIn(0.1f, 1.0f)

                        vortices.add(
                            VortexPoint(
                                x = student.xPosition.value,
                                y = student.yPosition.value,
                                momentum = momentum,
                                radius = 400f + (momentum * 600f),
                                polarity = if (avgPolarity >= 0) 1.0f else -1.0f
                            )
                        )
                    }
                }
            }
        }

        return vortices.sortedByDescending { it.momentum }.take(5)
    }
}
