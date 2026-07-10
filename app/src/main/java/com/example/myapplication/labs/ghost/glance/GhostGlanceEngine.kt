package com.example.myapplication.labs.ghost.glance

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog

/**
 * GhostGlanceEngine: The logical core of the "Neural Glance" preview system.
 *
 * This engine is responsible for synthesizing a student's longitudinal history (Behaviors, Quizzes,
 * and Homework) into a high-fidelity "Neural State." This state represents the student's current
 * classroom Standing and is used to drive both the informational HUD and the underlying
 * AGSL visual shaders.
 *
 * ### Performance Architecture (BOLT):
 * To ensure that the Glance preview can be triggered rapidly during high-density seating chart
 * interactions, the engine implements several critical optimizations:
 * 1. **Single-Pass Synthesis**: Replaces multiple $O(L)$ collection filters and counts with a
 *    single manual iteration over the log sets.
 * 2. **Zero-Allocation Analysis**: Avoids the creation of intermediate lists or temporary
 *    objects during the analysis of the 7-day sliding window.
 */
object GhostGlanceEngine {

    /**
     * Categorizes a student's current classroom status into four distinct behavioral archetypes.
     */
    enum class NeuralSignature {
        /** Balanced positive behavior and consistent academic participation. */
        STABLE,
        /** Exceptional engagement and performance across both behavioral and academic axes. */
        PEAK,
        /** Identified by frequent negative behavioral triggers and social friction. */
        TURBULENT,
        /** Minimal recent log activity, representing a dormant or "Quiet" state. */
        QUIET
    }

    /**
     * A snapshot of a student's neural standings at a specific moment.
     *
     * @property signature The behavioral archetype derived from the logs.
     * @property momentum Normalized intensity of classroom engagement (0.0..1.0).
     * @property stability Ratio of positive to total behavioral logs (0.0..1.0).
     * @property engagement Normalized frequency of academic activity (0.0..1.0).
     */
    data class GlanceState(
        val signature: NeuralSignature,
        val momentum: Float,
        val stability: Float,
        val engagement: Float
    )

    /**
     * Synthesizes student logs into a [GlanceState] based on a 7-day sliding window.
     *
     * ### BOLT Optimization:
     * This method avoids standard Kotlin functional chains (`.filter().count()`) which would
     * require multiple passes over the collections and trigger numerous intermediate object
     * allocations. Instead, it utilizes a single-pass manual loop to aggregate all required
     * metrics in $O(L)$ time.
     *
     * @param behaviorLogs The student's behavioral history.
     * @param quizLogs The student's quiz history.
     * @param homeworkLogs The student's homework history.
     * @return A synthesized [GlanceState] for the Glance UI.
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
