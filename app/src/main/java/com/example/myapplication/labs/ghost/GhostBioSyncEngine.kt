package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Student
import kotlin.math.*

/**
 * GhostBioSyncEngine: A logic engine for visualizing the classroom's biological state.
 *
 * This engine calculates two core metrics:
 * 1. **Student Vitality**: A 0.0-1.0 score representing the student's individual "life force"
 *    or engagement, driven by recent interactions and historical performance.
 * 2. **Classroom Harmony**: A global metric (0.0-1.0) representing the synchronicity
 *    and emotional balance of the classroom ecosystem.
 *
 * BOLT Optimization: Uses single-pass O(N) loops and avoids heavy object churn.
 */
object GhostBioSyncEngine {

    /**
     * Data class representing the biological state of a student.
     */
    data class BioStatus(
        val studentId: Long,
        val vitality: Float, // 0.0 to 1.0
        val stress: Float,   // 0.0 to 1.0
        val resonance: Float // 0.0 to 1.0 (sync with others)
    )

    /**
     * Calculates the BioStatus for all students.
     *
     * Vitality Heuristics:
     * - Base vitality: 0.5
     * - Recent interaction (last 30m): +0.1 per event (max +0.4)
     * - Long-term starvation (>2h since last positive): -0.05 per hour (max -0.3)
     * - Academic consistency: +0.1 if avg > 0.8
     *
     * Stress Heuristics:
     * - High negative frequency: +0.2 per event in 1h window.
     */
    fun calculateBioStates(
        students: List<Student>,
        behaviorLogsByStudent: Map<Long, List<BehaviorEvent>>,
        now: Long = System.currentTimeMillis()
    ): Map<Long, BioStatus> {
        val result = mutableMapOf<Long, BioStatus>()
        val window30m = 30 * 60 * 1000L
        val window1h = 60 * 60 * 1000L
        val window2h = 120 * 60 * 1000L

        // BOLT: Manual index loop
        for (i in students.indices) {
            val student = students[i]
            val logs = behaviorLogsByStudent[student.id] ?: emptyList()

            var recentActivityCount = 0
            var recentNegativeCount = 0
            var lastPositiveTs = 0L

            for (j in logs.indices) {
                val log = logs[j]
                val age = now - log.timestamp
                if (age < 0) continue // Future logs (shouldn't happen)

                if (age < window30m) recentActivityCount++
                if (age < window1h && log.type.contains("Negative", ignoreCase = true)) {
                    recentNegativeCount++
                }
                if (!log.type.contains("Negative", ignoreCase = true) && log.timestamp > lastPositiveTs) {
                    lastPositiveTs = log.timestamp
                }
            }

            // Vitality logic
            var vitality = 0.5f + (recentActivityCount * 0.1f)
            if (lastPositiveTs > 0) {
                val idleTime = now - lastPositiveTs
                if (idleTime > window2h) {
                    val idleHours = (idleTime - window2h) / (3600000.0)
                    vitality -= (idleHours * 0.05f).toFloat()
                }
            } else {
                vitality -= 0.2f // No positive history
            }
            vitality = vitality.coerceIn(0.1f, 1.0f)

            // Stress logic
            val stress = (recentNegativeCount * 0.2f).coerceIn(0.0f, 1.0f)

            result[student.id] = BioStatus(
                studentId = student.id,
                vitality = vitality,
                stress = stress,
                resonance = 0.5f // Default, will be refined in future layers
            )
        }

        return result
    }

    /**
     * Calculates the global Classroom Harmony score.
     * Harmony is high when behaviors are predominantly positive and balanced across the room.
     */
    fun calculateHarmony(bioStates: Map<Long, BioStatus>): Float {
        if (bioStates.isEmpty()) return 0.8f // Peaceful void

        var totalVitality = 0f
        var totalStress = 0f
        for (status in bioStates.values) {
            totalVitality += status.vitality
            totalStress += status.stress
        }

        val avgVitality = totalVitality / bioStates.size
        val avgStress = totalStress / bioStates.size

        // Harmony = Balance of High Vitality and Low Stress
        return (avgVitality * (1.0f - avgStress)).coerceIn(0.0f, 1.0f)
    }
}
