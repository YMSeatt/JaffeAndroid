package com.example.myapplication.labs.ghost.kaleidoscope

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Student
import kotlin.math.abs

/**
 * GhostKaleidoscopeEngine: Synthesizes "Neural Fragments" for symmetry visualization.
 *
 * This engine identifies recent behavioral clusters and academic milestones to create
 * multifaceted fragments. It calculates a "Harmony Index" (0.0 to 1.0) which drives the
 * symmetry factor of the kaleidoscope effect.
 *
 * BOLT: Optimized for 60fps by using pre-grouped logs and manual index-based loops.
 */
object GhostKaleidoscopeEngine {

    /**
     * Represents a fragment of classroom activity to be mirrored.
     *
     * @property x Logical X (0-4000).
     * @property y Logical Y (0-4000).
     * @property polarity Behavioral balance (-1.0 to 1.0).
     * @property intensity Recent activity volume (0.0 to 1.0).
     */
    data class NeuralFragment(
        val x: Float,
        val y: Float,
        val polarity: Float,
        val intensity: Float
    )

    /**
     * Synthesizes fragments from the last 15 minutes of activity.
     *
     * BOLT: Single-pass analysis over grouped logs.
     */
    fun synthesizeFragments(
        students: List<Student>,
        behaviorLogsByStudent: Map<Long, List<BehaviorEvent>>,
        now: Long = System.currentTimeMillis()
    ): List<NeuralFragment> {
        val fragments = ArrayList<NeuralFragment>()
        val fifteenMinutesMs = 15 * 60 * 1000L

        // Iterate over students to find active nodes
        for (i in students.indices) {
            val student = students[i]
            val logs = behaviorLogsByStudent[student.id] ?: continue

            var posCount = 0
            var negCount = 0
            var recentCount = 0

            for (j in logs.indices) {
                val log = logs[j]
                if (now - log.timestamp > fifteenMinutesMs) break // Sorted DESC

                recentCount++
                if (log.type.contains("Negative", ignoreCase = true)) {
                    negCount++
                } else {
                    posCount++
                }
            }

            if (recentCount > 0) {
                val polarity = (posCount - negCount).toFloat() / recentCount.toFloat()
                val intensity = (recentCount.toFloat() / 5f).coerceAtMost(1.0f)

                fragments.add(NeuralFragment(
                    x = student.xPosition,
                    y = student.yPosition,
                    polarity = polarity,
                    intensity = intensity
                ))
            }
            if (fragments.size >= 12) break // Cap at 12 shards for performance
        }

        return fragments
    }

    /**
     * Calculates the Global Harmony Index based on fragment distribution and polarity.
     * Higher harmony (more positive, evenly distributed) leads to higher symmetry.
     */
    fun calculateHarmony(fragments: List<NeuralFragment>): Float {
        if (fragments.isEmpty()) return 0.5f

        var totalPolarity = 0f
        for (i in fragments.indices) {
            totalPolarity += fragments[i].polarity
        }

        val avgPolarity = (totalPolarity / fragments.size).coerceIn(-1f, 1f)
        // Harmony is high when polarity is positive and shards are present.
        return (avgPolarity * 0.5f + 0.5f).coerceIn(0.1f, 1.0f)
    }
}
