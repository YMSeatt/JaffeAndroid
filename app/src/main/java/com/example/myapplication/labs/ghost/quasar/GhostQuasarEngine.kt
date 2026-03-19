package com.example.myapplication.labs.ghost.quasar

import com.example.myapplication.data.BehaviorEvent

/**
 * GhostQuasarEngine: Identifies "Quasar Students" — high-energy nodes in the classroom ecosystem.
 *
 * A student becomes a Quasar if they meet specific criteria for behavioral log density
 * or rapid academic shifts, creating a visual "Gravity Well" and "Accretion Disk"
 * on the seating chart.
 *
 * BOLT: Optimized for single-pass O(Recent) analysis using manual loops and early exits.
 */
object GhostQuasarEngine {

    /**
     * Identifies Quasars from the behavioral log stream.
     *
     * BOLT: This method is optimized to run in O(Recent) time by assuming the input list
     * is sorted descending by timestamp. It performs a single pass to aggregate energy
     * and polarity for all students within a 30-minute sliding window.
     *
     * @param behaviorLogs The stream of behavior events (sorted DESC).
     * @param currentTime Current system time.
     * @return A map of Student ID to Quasar metrics: Pair(Energy, Polarity).
     */
    fun identifyQuasars(
        behaviorLogs: List<BehaviorEvent>,
        currentTime: Long = System.currentTimeMillis()
    ): Map<Long, Pair<Float, Float>> {
        val window = 30 * 60 * 1000L // 30 minutes
        val cutoff = currentTime - window

        // studentId -> (total_count, positive_count, negative_count)
        val stats = mutableMapOf<Long, IntArray>()

        for (log in behaviorLogs) {
            if (log.timestamp < cutoff) break // BOLT: Early exit for O(Recent) performance

            val counts = stats.getOrPut(log.studentId) { IntArray(3) }
            counts[0]++ // Total
            if (log.type.contains("Positive", ignoreCase = true)) {
                counts[1]++
            } else if (log.type.contains("Negative", ignoreCase = true)) {
                counts[2]++
            }
        }

        val results = mutableMapOf<Long, Pair<Float, Float>>()
        for ((studentId, counts) in stats) {
            val total = counts[0]
            if (total >= 3) { // Threshold for Quasar status
                val energy = (total.toFloat() / 10f).coerceAtMost(1.0f)
                val pos = counts[1]
                val neg = counts[2]
                val relevantTotal = (pos + neg).coerceAtLeast(1)
                val polarity = (pos - neg).toFloat() / relevantTotal.toFloat()
                results[studentId] = energy to polarity
            }
        }

        return results
    }

    /**
     * Legacy structure for compatibility or specialized analysis.
     */
    data class QuasarState(
        val studentId: Long,
        val x: Float,
        val y: Float,
        val energy: Float,
        val luminosity: Float,
        val behaviorPolarity: Float
    )
}
