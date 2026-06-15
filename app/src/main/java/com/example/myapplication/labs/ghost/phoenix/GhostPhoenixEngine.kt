package com.example.myapplication.labs.ghost.phoenix

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Student

/**
 * GhostPhoenixEngine: Neural Resilience & Recovery Analysis.
 *
 * This engine identifies students who exhibit "Phoenix" behavior—a significant
 * recovery from a period of negative behavioral history to a recent streak of
 * positive engagement.
 *
 * ### The "Phoenix" Metric:
 * Resilience is calculated by comparing a "Struggle Period" (older logs) with a
 * "Recovery Period" (recent logs). A high Phoenix score indicates a student who
 * has overcome recent behavioral challenges.
 */
object GhostPhoenixEngine {

    /** The window for the "Recovery Period" (recent logs). */
    private const val RECOVERY_WINDOW_MS = 2 * 60 * 60 * 1000L // 2 hours

    /** The window for the "Struggle Period" (historical logs to check for negative baseline). */
    private const val STRUGGLE_WINDOW_MS = 24 * 60 * 60 * 1000L // 24 hours

    /** Threshold for the resilience score to trigger the Phoenix effect. */
    const val PHOENIX_THRESHOLD = 0.6f

    /**
     * Calculates the resilience score for each student.
     *
     * @param students The current list of student entities.
     * @param behaviorLogsByStudent Map of student IDs to their behavioral history (DESC sorted).
     * @return Map of student IDs to their normalized resilience score [0.0 - 1.0].
     */
    fun calculateResilienceScores(
        students: List<Student>,
        behaviorLogsByStudent: Map<Long, List<BehaviorEvent>>
    ): Map<Long, Float> {
        val now = System.currentTimeMillis()
        val recoveryCutoff = now - RECOVERY_WINDOW_MS
        val struggleCutoff = now - STRUGGLE_WINDOW_MS

        val scores = mutableMapOf<Long, Float>()

        for (i in 0 until students.size) {
            val student = students[i]
            val studentId = student.id
            val logs = behaviorLogsByStudent[studentId] ?: continue

            var recentPositiveCount = 0
            var historicalNegativeCount = 0

            // BOLT: Single pass through DESC sorted logs
            for (j in 0 until logs.size) {
                val event = logs[j]
                if (event.timestamp < struggleCutoff) break

                val isNegative = event.type.contains("Negative", ignoreCase = true)
                val isPositive = event.type.contains("Positive", ignoreCase = true)

                if (event.timestamp >= recoveryCutoff) {
                    if (isPositive) recentPositiveCount++
                } else {
                    // Struggle window (logs older than recovery window but newer than struggle window)
                    if (isNegative) historicalNegativeCount++
                }
            }

            // Resilience Formula:
            // Recovery is meaningful only if there was a historical struggle.
            // We reward recent positive streaks that follow historical negative logs.
            if (historicalNegativeCount > 0) {
                // Base score: 0.2 per positive event, 0.1 per historical negative event
                // This formula prioritizes recovery over just being "good".
                val score = (recentPositiveCount * 0.2f) + (historicalNegativeCount * 0.1f)
                scores[studentId] = score.coerceIn(0f, 1f)
            } else {
                scores[studentId] = 0f
            }
        }

        return scores
    }
}
