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
 *
 * ### Physics Model:
 * The engine implements a "Social Whirlpool" model where student density and log
 * frequency act as localized pressure points. When multiple high-activity nodes
 * are in close proximity, their collective "Energy" is summed and normalized
 * to create a rotational field.
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
     * BOLT: Optimized overload for Student entities and pre-grouped logs.
     * Replaces expensive functional operators with manual loops and uses squared
     * distance checks to avoid unnecessary sqrt calls.
     *
     * @param students List of students and their current positions.
     * @param behaviorLogsByStudent Historical behavior events grouped by studentId.
     * @param windowMillis Time window for analysis (default 10 minutes).
     * @return A list of detected [VortexPoint]s.
     */
    /** BOLT: Internal data structure to avoid Map lookups in nested loops. */
    private data class VortexNode(
        val x: Float,
        val y: Float,
        val intensity: Float,
        val polarity: Float
    )

    fun identifyVortices(
        students: List<com.example.myapplication.data.Student>,
        behaviorLogsByStudent: Map<Long, List<BehaviorEvent>>,
        windowMillis: Long = 600_000L
    ): List<VortexPoint> {
        if (students.isEmpty()) return emptyList()

        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - windowMillis

        // BOLT: Collect only active students to transform O(N^2) into O(A^2)
        val activeNodes = mutableListOf<VortexNode>()

        for (student in students) {
            val logs = behaviorLogsByStudent[student.id] ?: continue
            var posCount = 0
            var negCount = 0
            var recentCount = 0

            // BOLT: Manual loop with early break assuming logs are DESC sorted by timestamp
            for (log in logs) {
                if (log.timestamp < startTime) break
                recentCount++
                if (log.type.contains("Negative", ignoreCase = true)) {
                    negCount++
                } else {
                    posCount++
                }
            }

            if (recentCount > 0) {
                val netPolarity = if (negCount > posCount) -1.0f else 1.0f
                val intensity = (recentCount.toFloat() / 5.0f).coerceIn(0f, 1.0f)
                activeNodes.add(VortexNode(student.xPosition, student.yPosition, intensity, netPolarity))
            }
        }

        if (activeNodes.isEmpty()) return emptyList()

        val vortices = mutableListOf<VortexPoint>()
        val clusterThresholdSq = 800f * 800f

        // BOLT: Optimized clustering using only active nodes
        for (i in activeNodes.indices) {
            val node = activeNodes[i]

            if (node.intensity > 0.4f) {
                // Check if this student is already near an identified vortex
                var nearExisting = false
                for (v in vortices) {
                    val dx = v.x - node.x
                    val dy = v.y - node.y
                    if (dx * dx + dy * dy < clusterThresholdSq) {
                        nearExisting = true
                        break
                    }
                }

                if (!nearExisting) {
                    // Find neighbors to calculate cluster momentum
                    var neighborEnergySum = 0f
                    var neighborPolaritySum = 0f
                    var neighborCount = 0

                    for (j in activeNodes.indices) {
                        if (i == j) continue
                        val other = activeNodes[j]
                        val dx = other.x - node.x
                        val dy = other.y - node.y
                        if (dx * dx + dy * dy < clusterThresholdSq) {
                            neighborEnergySum += other.intensity
                            neighborPolaritySum += other.polarity
                            neighborCount++
                        }
                    }

                    if (neighborCount > 0) {
                        val avgPolarity = (neighborPolaritySum + node.polarity) / (neighborCount + 1)
                        val momentum = ((node.intensity + neighborEnergySum) / (neighborCount + 1)).coerceIn(0.1f, 1.0f)

                        vortices.add(
                            VortexPoint(
                                x = node.x,
                                y = node.y,
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

    /**
     * Compatibility overload for UI layers and legacy callers.
     * BOLT: Internally wraps students to avoid redundant mapping if possible.
     */
    fun identifyVortices(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>,
        windowMillis: Long = 600_000L
    ): List<VortexPoint> {
        if (students.isEmpty()) return emptyList()

        val behaviorLogsByStudent = behaviorLogs.groupBy { it.studentId }
        val rawStudents = students.map { ui ->
            com.example.myapplication.data.Student(
                id = ui.id.toLong(),
                firstName = "", lastName = "", gender = "",
                xPosition = ui.xPosition.value,
                yPosition = ui.yPosition.value
            )
        }
        return identifyVortices(rawStudents, behaviorLogsByStudent, windowMillis)
    }
}
