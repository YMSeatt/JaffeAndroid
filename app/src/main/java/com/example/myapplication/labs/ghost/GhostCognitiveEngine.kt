package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Student
import kotlin.math.*

data class Point(var x: Float, var y: Float)

object GhostCognitiveEngine {
    private const val REPULSION_CONSTANT = 500000f
    private const val ATTRACTION_CONSTANT = 0.05f
    private const val NEGATIVE_BEHAVIOR_MULTIPLIER = 2.5f
    private const val ITERATIONS = 50
    private const val DAMPING = 0.9f

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
