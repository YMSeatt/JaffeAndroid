package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Student
import kotlin.math.*

data class Point(var x: Float, var y: Float)

/**
 * GhostCognitiveEngine: An automated seating chart optimizer using a force-directed graph algorithm.
 *
 * This engine simulates a physical system where students act as particles that exert forces on each other.
 * It aims to find an optimal layout by balancing:
 * 1. Global Repulsion: Keeping students from overlapping or being too close.
 * 2. Targeted Repulsion: Increasing distance between students with high negative behavior logs.
 * 3. Group Attraction: Pulling students in the same group (e.g., project teams) closer together.
 */
object GhostCognitiveEngine {
    /** The base strength of the force that pushes all students away from each other. */
    private const val REPULSION_CONSTANT = 500000f

    /** The base strength of the force that pulls group members toward each other. */
    private const val ATTRACTION_CONSTANT = 0.05f

    /** Multiplier applied to the repulsion force when students have negative behavior records. */
    private const val NEGATIVE_BEHAVIOR_MULTIPLIER = 2.5f

    /** The number of simulation steps to run; higher values lead to more stable layouts but take longer. */
    private const val ITERATIONS = 50

    /** The energy loss per iteration to ensure the system eventually settles (reaches equilibrium). */
    private const val DAMPING = 0.9f

    /**
     * Executes the layout optimization simulation.
     *
     * @param students The list of students to arrange.
     * @param behaviorLogs Historical behavior data used to calculate social "repulsion".
     * @param canvasWidth The total width of the seating chart area in pixels.
     * @param canvasHeight The total height of the seating chart area in pixels.
     * @return A map of student IDs to their new suggested [Point] coordinates.
     */
    fun optimizeLayout(
        students: List<Student>,
        behaviorLogs: List<BehaviorEvent>,
        canvasWidth: Int = 4000,
        canvasHeight: Int = 4000
    ): Map<Long, Point> {
        val studentPoints = students.associate { it.id to Point(it.xPosition, it.yPosition) }.toMutableMap()
        val velocities = students.associate { it.id to Point(0f, 0f) }.toMutableMap()

        // Pre-calculate behavioral "toxicity" scores
        val negativeScores = behaviorLogs.filter { it.type.contains("Negative", ignoreCase = true) }
            .groupBy { it.studentId }
            .mapValues { it.value.size.toFloat() }

        repeat(ITERATIONS) {
            val forces = students.associate { it.id to Point(0f, 0f) }.toMutableMap()

            // 1. Repulsion between all pairs
            for (i in students.indices) {
                for (j in i + 1 until students.size) {
                    val s1 = students[i]
                    val s2 = students[j]
                    val p1 = studentPoints[s1.id]!!
                    val p2 = studentPoints[s2.id]!!

                    val dx = p1.x - p2.x
                    val dy = p1.y - p2.y
                    val distanceSq = dx * dx + dy * dy + 0.01f
                    val distance = sqrt(distanceSq)

                    var repulsion = REPULSION_CONSTANT / distanceSq

                    // Extra repulsion for negative behavior
                    val score1 = negativeScores[s1.id] ?: 0f
                    val score2 = negativeScores[s2.id] ?: 0f
                    if (score1 > 0 || score2 > 0) {
                        repulsion *= (1f + (score1 + score2) * NEGATIVE_BEHAVIOR_MULTIPLIER)
                    }

                    val fx = (dx / distance) * repulsion
                    val fy = (dy / distance) * repulsion

                    forces[s1.id]!!.x += fx
                    forces[s1.id]!!.y += fy
                    forces[s2.id]!!.x -= fx
                    forces[s2.id]!!.y -= fy
                }
            }

            // 2. Attraction for group members
            val groupMap = students.filter { it.groupId != null }.groupBy { it.groupId }
            groupMap.forEach { (_, members) ->
                for (i in members.indices) {
                    for (j in i + 1 until members.size) {
                        val s1 = members[i]
                        val s2 = members[j]
                        val p1 = studentPoints[s1.id]!!
                        val p2 = studentPoints[s2.id]!!

                        val dx = p1.x - p2.x
                        val dy = p1.y - p2.y

                        val fx = dx * ATTRACTION_CONSTANT
                        val fy = dy * ATTRACTION_CONSTANT

                        forces[s1.id]!!.x -= fx
                        forces[s1.id]!!.y -= fy
                        forces[s2.id]!!.x += fx
                        forces[s2.id]!!.y += fy
                    }
                }
            }

            // 3. Apply forces and update positions
            students.forEach { s ->
                val f = forces[s.id]!!
                val v = velocities[s.id]!!
                val p = studentPoints[s.id]!!

                v.x = (v.x + f.x) * DAMPING
                v.y = (v.y + f.y) * DAMPING

                // Cap velocity
                val speed = sqrt(v.x * v.x + v.y * v.y)
                if (speed > 50f) {
                    v.x = (v.x / speed) * 50f
                    v.y = (v.y / speed) * 50f
                }

                p.x += v.x
                p.y += v.y

                // Constrain to canvas
                p.x = p.x.coerceIn(0f, canvasWidth.toFloat() - 100f)
                p.y = p.y.coerceIn(0f, canvasHeight.toFloat() - 100f)
            }
        }

        return studentPoints
    }
}
