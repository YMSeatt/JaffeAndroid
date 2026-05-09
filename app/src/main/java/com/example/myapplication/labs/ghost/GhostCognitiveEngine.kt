package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Student
import kotlin.math.*

data class Point(var x: Float, var y: Float)

/**
 * GhostCognitiveEngine: An automated seating chart optimizer using a force-directed graph algorithm.
 *
 * This engine treats students as nodes and relationships as forces:
 * - **Repulsion**: Every student pushes away every other student to ensure they don't overlap and
 *   to fill the available space. The force follows an inverse-square law.
 * - **Social Distance Repulsion**: Students with negative behavioral history exert a stronger
 *   repulsion force (scaled by [NEGATIVE_BEHAVIOR_MULTIPLIER]), effectively suggesting they
 *   be seated further apart from others.
 * - **Group Attraction**: Students belonging to the same group exert an attractive force
 *   on each other, pulling them together into clusters.
 *
 * Performance optimizations:
 * - Uses primitive [FloatArray] and [LongArray] to avoid object allocation and boxing in the simulation loop.
 * - Pre-calculates group indices and behavioral scores to minimize O(N) or O(log N) lookups inside the O(N^2) loop.
 */
object GhostCognitiveEngine {
    /** Base repulsion force. Calibrated for a 4000x4000 canvas. */
    private const val REPULSION_CONSTANT = 500000f
    /** Percentage of distance covered per iteration for group attraction. */
    private const val ATTRACTION_CONSTANT = 0.05f
    /** Scaling factor for repulsion when students have negative behavior records. */
    private const val NEGATIVE_BEHAVIOR_MULTIPLIER = 2.5f
    /** Number of simulation steps to run per optimization call. */
    private const val ITERATIONS = 50
    /** Velocity decay factor to ensure the simulation converges to equilibrium. */
    private const val DAMPING = 0.9f

    /**
     * Executes the layout optimization simulation.
     *
     * @param students List of students to position.
     * @param behaviorLogs Historical logs used to calculate social repulsion forces.
     * @param canvasWidth The logical width of the seating chart.
     * @param canvasHeight The logical height of the seating chart.
     * @return A map of student IDs to their new [Point] coordinates.
     */
    fun optimizeLayout(
        students: List<Student>,
        behaviorLogs: List<BehaviorEvent>,
        canvasWidth: Int = 4000,
        canvasHeight: Int = 4000
    ): Map<Long, Point> {
        val n = students.size
        if (n == 0) return emptyMap()

        // State arrays for performance
        val studentIds = LongArray(n)
        val posX = FloatArray(n)
        val posY = FloatArray(n)
        val velX = FloatArray(n)
        val velY = FloatArray(n)
        val forceX = FloatArray(n)
        val forceY = FloatArray(n)
        val negScores = FloatArray(n)

        // BOLT: Single-pass for negative behavior counts using manual loop to avoid allocations
        val negativeCounts = mutableMapOf<Long, Int>()
        for (i in behaviorLogs.indices) {
            val log = behaviorLogs[i]
            if (log.type.contains("Negative", ignoreCase = true)) {
                negativeCounts[log.studentId] = (negativeCounts[log.studentId] ?: 0) + 1
            }
        }

        // BOLT: Single-pass for student data and pre-calculated social multipliers
        val groupsMap = mutableMapOf<Long, MutableList<Int>>()
        for (i in 0 until n) {
            val student = students[i]
            studentIds[i] = student.id
            posX[i] = student.xPosition
            posY[i] = student.yPosition

            val count = negativeCounts[student.id] ?: 0
            // Pre-calculate: (0.5 + count * multiplier) such that s1 + s2 = 1.0 + (c1+c2)*multiplier
            negScores[i] = 0.5f + (count.toFloat() * NEGATIVE_BEHAVIOR_MULTIPLIER)

            val gid = student.groupId
            if (gid != null) {
                var groupList = groupsMap[gid]
                if (groupList == null) {
                    groupList = mutableListOf()
                    groupsMap[gid] = groupList
                }
                groupList.add(i)
            }
        }

        // BOLT: Pre-calculate group indices as Array of IntArray for zero-allocation iteration
        val groupIndices = Array(groupsMap.size) { IntArray(0) }
        var gIdx = 0
        for (entry in groupsMap) {
            groupIndices[gIdx++] = entry.value.toIntArray()
        }

        val canvasW = canvasWidth.toFloat()
        val canvasH = canvasHeight.toFloat()

        repeat(ITERATIONS) {
            // Reset forces for current iteration
            forceX.fill(0f)
            forceY.fill(0f)

            // 1. Repulsion between all pairs - O(N^2)
            for (i in 0 until n) {
                val p1x = posX[i]
                val p1y = posY[i]
                val s1 = negScores[i]

                for (j in i + 1 until n) {
                    val dx = p1x - posX[j]
                    val dy = p1y - posY[j]
                    // Add small epsilon to avoid division by zero
                    val distSq = dx * dx + dy * dy + 0.01f

                    // BOLT: Consolidated repulsion calculation to minimize math overhead
                    val multiplier = s1 + negScores[j]
                    val dist = sqrt(distSq)
                    val factor = (REPULSION_CONSTANT * multiplier) / (distSq * dist)

                    val fx = dx * factor
                    val fy = dy * factor

                    forceX[i] += fx
                    forceY[i] += fy
                    forceX[j] -= fx
                    forceY[j] -= fy
                }
            }

            // 2. Attraction for group members - O(G * N_g^2)
            for (g in 0 until groupIndices.size) {
                val indices = groupIndices[g]
                val gSize = indices.size
                for (i in 0 until gSize) {
                    val idx1 = indices[i]
                    val p1x = posX[idx1]
                    val p1y = posY[idx1]

                    for (j in i + 1 until gSize) {
                        val idx2 = indices[j]
                        val dx = p1x - posX[idx2]
                        val dy = p1y - posY[idx2]

                        val fx = dx * ATTRACTION_CONSTANT
                        val fy = dy * ATTRACTION_CONSTANT

                        forceX[idx1] -= fx
                        forceY[idx1] -= fy
                        forceX[idx2] += fx
                        forceY[idx2] += fy
                    }
                }
            }

            // 3. Apply forces and update positions
            for (i in 0 until n) {
                var vx = (velX[i] + forceX[i]) * DAMPING
                var vy = (velY[i] + forceY[i]) * DAMPING

                // BOLT: Velocity capping optimized to avoid sqrt in the common path
                val speedSq = vx * vx + vy * vy
                if (speedSq > 2500f) { // 50.0^2
                    val invSpeed = 50f / sqrt(speedSq)
                    vx *= invSpeed
                    vy *= invSpeed
                }

                velX[i] = vx
                velY[i] = vy

                posX[i] = (posX[i] + vx).coerceIn(0f, canvasW - 100f)
                posY[i] = (posY[i] + vy).coerceIn(0f, canvasH - 100f)
            }
        }

        // Return the final positions mapped by student ID
        val results = mutableMapOf<Long, Point>()
        for (i in 0 until n) {
            results[studentIds[i]] = Point(posX[i], posY[i])
        }
        return results
    }
}
