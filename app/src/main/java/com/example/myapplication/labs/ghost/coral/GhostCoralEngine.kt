package com.example.myapplication.labs.ghost.coral

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Student
import kotlin.math.abs
import kotlin.math.exp

/**
 * GhostCoralEngine: Calculates "Social Calcification" and Collaborative Growth.
 *
 * Coral growth is a metaphor for long-term collaborative stability. It "calcifies"
 * (strengthens) when students in close proximity share positive behavioral history.
 *
 * ### Architectural Intent
 * The Social Reef represents the organic, slow-growing structures of positive
 * interaction. Unlike "Flares" which are ephemeral, Coral is persistent and
 * requires consistent synergy to maintain its "Vitality".
 */
object GhostCoralEngine {
    private const val PROXIMITY_THRESHOLD = 800f
    private const val PROXIMITY_THRESHOLD_SQ = PROXIMITY_THRESHOLD * PROXIMITY_THRESHOLD

    /**
     * Represents a single "Coral Branch" connecting two students in the reef.
     *
     * @property idA ID of the first student.
     * @property idB ID of the second student.
     * @property x1 Starting X in logical 4000x4000 space.
     * @property y1 Starting Y in logical 4000x4000 space.
     * @property x2 Ending X in logical 4000x4000 space.
     * @property y2 Ending Y in logical 4000x4000 space.
     * @property density The "thickness" of the calcification (0.0 to 1.0).
     * @property vitality The "glow" or health of the coral (0.0 to 1.0), driven by recent parity.
     */
    data class CoralBranch(
        val idA: Long,
        val idB: Long,
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
        val density: Float,
        val vitality: Float
    )

    /**
     * Synthesizes the Social Reef state from classroom data.
     *
     * This engine implements a multi-stage synthesis:
     * 1. **Calcification Pass**: Calculates the individual "Growth Potential" for each student
     *    based on their positive behavioral balance over the last 24 hours.
     * 2. **Proximity Pairing**: Identifies pairs of students within the [PROXIMITY_THRESHOLD].
     * 3. **Branch Synthesis**: Calculates density (shared growth) and vitality (behavioral parity).
     *
     * BOLT: Optimized O(S^2) proximity pass with O(Recent) log analysis.
     *
     * @param students The list of students to analyze.
     * @param behaviorLogsByStudent Pre-grouped logs for O(1) lookup.
     * @param timeWindowMs The window for calcification analysis (default 24h).
     * @return A list of [CoralBranch] objects representing the current reef structure.
     */
    fun calculateSocialReef(
        students: List<Student>,
        behaviorLogsByStudent: Map<Long, List<BehaviorEvent>>,
        timeWindowMs: Long = 24 * 60 * 60 * 1000L
    ): List<CoralBranch> {
        if (students.size < 2) return emptyList()

        val now = System.currentTimeMillis()
        val startTime = now - timeWindowMs

        // 1. Individual Calcification Potential (based on positive logs)
        val calcificationMap = mutableMapOf<Long, Float>()
        for (i in students.indices) {
            val s = students[i]
            val logs = behaviorLogsByStudent[s.id] ?: continue

            var positiveCount = 0
            for (j in logs.indices) {
                val log = logs[j]
                if (log.timestamp < startTime) break // Assumes DESC
                if (isPositive(log.type)) {
                    positiveCount++
                }
            }

            // Heuristic: 5+ positive logs = max calcification potential
            if (positiveCount > 0) {
                calcificationMap[s.id] = (positiveCount / 5f).coerceAtMost(1.0f)
            }
        }

        val branches = mutableListOf<CoralBranch>()

        // 2. Spatial Pairing and Branch Synthesis
        for (i in students.indices) {
            val s1 = students[i]
            val c1 = calcificationMap[s1.id] ?: 0f

            for (j in i + 1 until students.size) {
                val s2 = students[j]
                val c2 = calcificationMap[s2.id] ?: 0f

                val dx = s1.xPosition - s2.xPosition
                val dy = s1.yPosition - s2.yPosition
                val distSq = dx * dx + dy * dy

                if (distSq < PROXIMITY_THRESHOLD_SQ) {
                    // Density is derived from shared calcification potential
                    val baseDensity = (c1 + c2) / 2f

                    // Vitality is higher if they have similar engagement levels (Parity)
                    val vitality = 1f - abs(c1 - c2)

                    // Apply spatial decay to density (Gaussian)
                    val spatialWeight = exp(-distSq / (2 * PROXIMITY_THRESHOLD_SQ / 9f))
                    val effectiveDensity = baseDensity * spatialWeight

                    // Only create branch if it has sufficient structural integrity
                    if (effectiveDensity > 0.05f) {
                        branches.add(
                            CoralBranch(
                                idA = s1.id,
                                idB = s2.id,
                                x1 = s1.xPosition,
                                y1 = s1.yPosition,
                                x2 = s2.xPosition,
                                y2 = s2.yPosition,
                                density = effectiveDensity,
                                vitality = vitality
                            )
                        )
                    }
                }
            }
        }

        return branches
    }

    /**
     * Determines if a behavior type string represents a positive interaction.
     */
    private fun isPositive(type: String): Boolean {
        val lower = type.lowercase()
        return lower.contains("positive") ||
               lower.contains("participation") ||
               lower.contains("great") ||
               lower.contains("good") ||
               lower.contains("excellent") ||
               lower.contains("leadership") ||
               lower.contains("helpful")
    }
}
