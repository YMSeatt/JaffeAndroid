package com.example.myapplication.labs.ghost.link

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Student
import kotlin.math.abs
import kotlin.math.exp

/**
 * GhostLinkEngine: Calculates proximity-based "Neural Pairing" between students.
 *
 * This engine identifies students who exhibit strong behavioral synergy and spatial
 * proximity. It visualizes the hidden "Neural Links" that form between students
 * during collaborative or high-activity periods.
 */
object GhostLinkEngine {

    /** BOLT: Distance threshold for neural pairing (600f) on the 4000x4000 canvas. */
    private const val LINK_DISTANCE_THRESHOLD = 600f
    private const val LINK_DISTANCE_THRESHOLD_SQ = LINK_DISTANCE_THRESHOLD * LINK_DISTANCE_THRESHOLD

    data class NeuralLink(
        val studentA: Long,
        val studentB: Long,
        val synergy: Float, // 0..1
        val ax: Float,
        val ay: Float,
        val bx: Float,
        val by: Float
    )

    /**
     * Identifies neural links between students based on proximity and behavioral synergy.
     *
     * BOLT: Optimized O(N^2) with spatial pruning and pre-calculated activity metrics.
     */
    fun identifyNeuralLinks(
        students: List<Student>,
        behaviorLogsByStudent: Map<Long, List<BehaviorEvent>>,
        timeWindowMs: Long = 600_000L // 10 minutes
    ): List<NeuralLink> {
        if (students.size < 2) return emptyList()

        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - timeWindowMs

        // 1. Pre-calculate Behavioral Synergy (recent activity frequency)
        val activityMetrics = mutableMapOf<Long, Float>()
        for (i in students.indices) {
            val student = students[i]
            val logs = behaviorLogsByStudent[student.id] ?: continue
            var recentCount = 0
            for (j in logs.indices) {
                if (logs[j].timestamp >= startTime) {
                    recentCount++
                } else {
                    break // Assumes DESC sorted logs
                }
            }
            if (recentCount > 0) {
                activityMetrics[student.id] = recentCount.toFloat()
            }
        }

        val links = mutableListOf<NeuralLink>()

        // 2. Spatial Analysis + Synergy Calculation
        for (i in students.indices) {
            val s1 = students[i]
            val metrics1 = activityMetrics[s1.id] ?: 0f

            for (j in i + 1 until students.size) {
                val s2 = students[j]
                val metrics2 = activityMetrics[s2.id] ?: 0f

                // Spatial Pruning
                val dx = s1.xPosition - s2.xPosition
                val dy = s1.yPosition - s2.yPosition
                val distSq = dx * dx + dy * dy

                if (distSq < LINK_DISTANCE_THRESHOLD_SQ) {
                    // Behavioral Synergy: Parity of recent activity levels
                    val synergy = if (metrics1 > 0f && metrics2 > 0f) {
                        1f - (abs(metrics1 - metrics2) / (metrics1 + metrics2))
                    } else if (metrics1 == 0f && metrics2 == 0f) {
                        0.3f // Base resonance for proximity
                    } else {
                        0.1f // Low synergy if one is inactive
                    }

                    // Spatial weighting (Gaussian decay)
                    val spatialWeight = exp(-distSq / (2 * LINK_DISTANCE_THRESHOLD_SQ / 4f))
                    val totalStrength = (synergy * 0.4f + spatialWeight * 0.6f)

                    if (totalStrength > 0.5f) {
                        links.add(
                            NeuralLink(
                                studentA = s1.id,
                                studentB = s2.id,
                                synergy = totalStrength,
                                ax = s1.xPosition,
                                ay = s1.yPosition,
                                bx = s2.xPosition,
                                by = s2.yPosition
                            )
                        )
                    }
                }
            }
        }

        return links
    }
}
