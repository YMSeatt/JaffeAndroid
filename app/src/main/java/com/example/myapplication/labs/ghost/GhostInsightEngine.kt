package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog

data class GhostInsight(
    val title: String,
    val summary: String,
    val recommendation: String,
    val status: InsightStatus
)

/**
 * Represents the health status of a student's engagement and performance.
 */
enum class InsightStatus {
    /** Excellent academic and behavioral standing. */
    OPTIMAL,
    /** Showing stable or slightly positive trends. */
    IMPROVING,
    /** Critical negative trends detected in behavior or grades. */
    CONCERNING,
    /** Insufficient data to determine status. */
    UNKNOWN
}

/**
 * GhostInsightEngine: A rule-based analysis engine that synthesizes student data into human-readable insights.
 */
object GhostInsightEngine {
    /**
     * Analyzes behavioral and academic logs to generate a comprehensive [GhostInsight].
     *
     * The engine evaluates:
     * - Positive vs. Negative behavior ratios.
     * - Quiz performance (normalized average).
     * - Homework completion rates.
     *
     * @param studentName The display name of the student.
     * @param behaviorLogs List of behavior events for the student.
     * @param quizLogs List of quiz results for the student.
     * @param homeworkLogs List of homework completion logs for the student.
     * @return A [GhostInsight] containing a title, summary, recommendation, and status.
     */
    fun generateInsight(
        studentName: String,
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>,
        homeworkLogs: List<HomeworkLog>
    ): GhostInsight {
        // Simple analysis logic
        val positiveBehavior = behaviorLogs.count { !it.type.contains("Negative", ignoreCase = true) }
        val negativeBehavior = behaviorLogs.count { it.type.contains("Negative", ignoreCase = true) }

        val quizScores = quizLogs.mapNotNull { it.markValue?.let { v -> it.maxMarkValue?.let { m -> v / m } } }
        val avgQuiz = if (quizScores.isNotEmpty()) quizScores.average() else 0.5

        val homeworkCompletion = if (homeworkLogs.isNotEmpty()) {
            homeworkLogs.count { it.status.contains("Done", ignoreCase = true) }.toDouble() / homeworkLogs.size
        } else 1.0

        return when {
            avgQuiz > 0.8 && homeworkCompletion > 0.8 && negativeBehavior == 0 -> {
                GhostInsight(
                    title = "Peak Performer",
                    summary = "$studentName is excelling across all metrics. Neural trends indicate high engagement and consistency.",
                    recommendation = "Consider peer-mentoring opportunities to further challenge this student.",
                    status = InsightStatus.OPTIMAL
                )
            }
            negativeBehavior > positiveBehavior -> {
                GhostInsight(
                    title = "Attention Required",
                    summary = "Recent behavioral fluctuations detected. Data suggests a potential disconnect with classroom expectations.",
                    recommendation = "Schedule a one-on-one check-in to identify underlying stressors.",
                    status = InsightStatus.CONCERNING
                )
            }
            avgQuiz < 0.6 || homeworkCompletion < 0.6 -> {
                GhostInsight(
                    title = "Academic Support Needed",
                    summary = "Downward trend in assessment outcomes. Homework consistency is below the class average.",
                    recommendation = "Provide supplementary resources or a targeted revision plan.",
                    status = InsightStatus.CONCERNING
                )
            }
            else -> {
                GhostInsight(
                    title = "Steady Progress",
                    summary = "Maintaining stable performance. Interactions are within expected neural parameters.",
                    recommendation = "Continue with current reinforcement strategies.",
                    status = InsightStatus.IMPROVING
                )
            }
        }
    }
}
