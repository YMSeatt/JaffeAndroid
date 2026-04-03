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
     */
    fun synthesize(
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>,
        homeworkLogs: List<HomeworkLog>
    ): GlanceState {
        val recentBehavior = behaviorLogs.filter { it.timestamp > System.currentTimeMillis() - 86400000 * 7 }
        val recentQuiz = quizLogs.filter { it.timestamp > System.currentTimeMillis() - 86400000 * 7 }
        val recentHomework = homeworkLogs.filter { it.loggedAt > System.currentTimeMillis() - 86400000 * 7 }

        val totalLogs = recentBehavior.size + recentQuiz.size + recentHomework.size
        if (totalLogs == 0) {
            return GlanceState(NeuralSignature.QUIET, 0f, 1f, 0f)
        }

        val negativeCount = recentBehavior.count { it.type.contains("Negative", ignoreCase = true) }
        val positiveCount = recentBehavior.count { it.type.contains("Positive", ignoreCase = true) }
        val academicCount = recentQuiz.size + recentHomework.size

        val stability = if (recentBehavior.isEmpty()) 1f else positiveCount.toFloat() / recentBehavior.size
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
