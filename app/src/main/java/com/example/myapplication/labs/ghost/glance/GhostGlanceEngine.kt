package com.example.myapplication.labs.ghost.glance

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog

/**
 * GhostGlanceEngine: Synthesizes student logs into a concise "Neural State" for the Glance overlay.
 *
 * This engine provides a rapid, high-fidelity overview of a student's current classroom standing.
 */
object GhostGlanceEngine {

    enum class NeuralSignature {
        STABLE,     // Balanced positive/academic activity
        PEAK,       // Exceptional engagement and performance
        TURBULENT,  // Frequent negative behavioral triggers
        QUIET       // Minimal recent log activity
    }

    data class GlanceState(
        val signature: NeuralSignature,
        val momentum: Float, // 0.0 to 1.0 (activity intensity)
        val stability: Float, // 0.0 to 1.0 (ratio of positive/negative)
        val engagement: Float // 0.0 to 1.0 (academic frequency)
    )

    /**
     * Generates a [GlanceState] based on a sliding window of recent logs.
     *
     * BOLT ⚡ Optimization: Single-pass analysis with zero allocations.
     * Replaced filter/count chains with manual index loops and pre-calculated totals.
     */
    fun synthesize(
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>,
        homeworkLogs: List<HomeworkLog>
    ): GlanceState {
        val now = System.currentTimeMillis()
        val window = 86400000L * 7
        val cutoff = now - window

        var recentBehaviorCount = 0
        var recentQuizCount = 0
        var recentHomeworkCount = 0
        var negativeCount = 0
        var positiveCount = 0

        // Single pass over behavior logs
        for (i in 0 until behaviorLogs.size) {
            val log = behaviorLogs[i]
            if (log.timestamp > cutoff) {
                recentBehaviorCount++
                if (log.type.contains("Negative", ignoreCase = true)) {
                    negativeCount++
                } else if (log.type.contains("Positive", ignoreCase = true)) {
                    positiveCount++
                }
            }
        }

        // Single pass over quiz logs
        for (i in 0 until quizLogs.size) {
            if (quizLogs[i].loggedAt > cutoff) {
                recentQuizCount++
            }
        }

        // Single pass over homework logs
        for (i in 0 until homeworkLogs.size) {
            if (homeworkLogs[i].loggedAt > cutoff) {
                recentHomeworkCount++
            }
        }

        val totalLogs = recentBehaviorCount + recentQuizCount + recentHomeworkCount
        if (totalLogs == 0) {
            return GlanceState(NeuralSignature.QUIET, 0f, 1f, 0f)
        }

        val academicCount = recentQuizCount + recentHomeworkCount
        val stability = if (recentBehaviorCount == 0) 1f else positiveCount.toFloat() / recentBehaviorCount
        val momentum = (totalLogs.toFloat() / 15f).coerceAtMost(1f)
        val engagement = (academicCount.toFloat() / 5f).coerceAtMost(1f)

        val signature = when {
            negativeCount > 2 -> NeuralSignature.TURBULENT
            academicCount > 3 && positiveCount > 3 -> NeuralSignature.PEAK
            totalLogs < 2 -> NeuralSignature.QUIET
            else -> NeuralSignature.STABLE
        }

        return GlanceState(signature, momentum, stability, engagement)
    }
}
