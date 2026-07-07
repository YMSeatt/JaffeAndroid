package com.example.myapplication.labs.ghost.prism

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import kotlin.math.abs

/**
 * GhostPrismEngine: Synthesizes individual student "vibes" based on behavioral and academic data.
 *
 * This engine maps complex classroom metrics to a discrete set of aesthetic "vibes"
 * used to drive dynamic student themes.
 *
 * BOLT ⚡ Optimization: Uses manual index-based loops and O(Recent) single-pass analysis
 * to minimize GC pressure and ensure high-frequency updates (60fps) remain fluid.
 */
object GhostPrismEngine {

    enum class Vibe {
        NEON_DREAM,  // High positive balance + High academic performance
        CYBER_PUNK,  // High activity frequency + Mixed valence (Chaotic)
        ZEN_GARDEN,  // Low frequency + Stable performance
        VOID_RUNNER, // High negative frequency + Academic struggle
        SOLAR_FLARE  // Recent burst of positive interactions (Last 15 mins)
    }

    /**
     * Synthesizes the vibe for a single student.
     *
     * @param studentId The unique ID of the student.
     * @param bLogs Student's behavior events (assumed DESC sorted).
     * @param qLogs Student's quiz logs.
     * @param hLogs Student's homework logs.
     * @param now Current system time for window comparison.
     * @return The student's synthesized [Vibe].
     */
    fun calculateVibe(
        studentId: Long,
        bLogs: List<BehaviorEvent>?,
        qLogs: List<QuizLog>?,
        hLogs: List<HomeworkLog>?,
        now: Long = System.currentTimeMillis()
    ): Vibe {
        var negCount = 0
        var posCount = 0
        var recentPosCount = 0
        val timeWindow = 15 * 60 * 1000L // 15 minutes for Solar Flare

        if (bLogs != null) {
            // BOLT: Manual index loop to avoid iterator churn
            for (i in 0 until bLogs.size) {
                val log = bLogs[i]
                if (log.type.contains("Negative", ignoreCase = true)) {
                    negCount++
                } else {
                    posCount++
                    if (now - log.timestamp < timeWindow) {
                        recentPosCount++
                    }
                }
            }
        }

        var quizSum = 0.0
        var quizCount = 0
        if (qLogs != null) {
            for (i in 0 until qLogs.size) {
                val log = qLogs[i]
                val v = log.markValue
                val m = log.maxMarkValue
                if (v != null && m != null && m > 0.0) {
                    quizSum += (v / m)
                    quizCount++
                }
            }
        }
        val academicAvg = if (quizCount > 0) (quizSum / quizCount).toFloat() else 0.75f

        val totalLogs = (bLogs?.size ?: 0) + quizCount + (hLogs?.size ?: 0)
        val behaviorBalance = if ((posCount + negCount) > 0) {
            posCount.toFloat() / (posCount + negCount).toFloat()
        } else 0.5f

        return when {
            // Priority 1: Recent burst of positivity
            recentPosCount >= 2 -> Vibe.SOLAR_FLARE

            // Priority 2: High negative / struggle
            negCount > 3 || (behaviorBalance < 0.3f && academicAvg < 0.6f) -> Vibe.VOID_RUNNER

            // Priority 3: High performers
            behaviorBalance > 0.8f && academicAvg > 0.85f -> Vibe.NEON_DREAM

            // Priority 4: High activity / chaotic
            totalLogs > 10 -> Vibe.CYBER_PUNK

            // Default: Stable/Quiet
            else -> Vibe.ZEN_GARDEN
        }
    }
}
