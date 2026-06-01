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
        // BOLT: Optimized using manual index-based loops and single-pass analysis to eliminate
        // iterator allocations and intermediate list creation (count/mapNotNull/average).

        var positiveBehavior = 0
        var negativeBehavior = 0
        for (i in 0 until behaviorLogs.size) {
            if (behaviorLogs[i].type.contains("Negative", ignoreCase = true)) {
                negativeBehavior++
            } else {
                positiveBehavior++
            }
        }

        var quizSum = 0.0
        var quizCount = 0
        for (i in 0 until quizLogs.size) {
            val log = quizLogs[i]
            val v = log.markValue
            val m = log.maxMarkValue
            if (v != null && m != null && m > 0.0) {
                quizSum += v / m
                quizCount++
            }
        }
        val avgQuiz = if (quizCount > 0) quizSum / quizCount else 0.5

        var homeworkDoneCount = 0
        for (i in 0 until homeworkLogs.size) {
            if (homeworkLogs[i].status.contains("Done", ignoreCase = true)) {
                homeworkDoneCount++
            }
        }
        val homeworkCompletion = if (homeworkLogs.isNotEmpty()) {
            homeworkDoneCount.toDouble() / homeworkLogs.size
        } else 1.0

        val status = calculateInsightStatus(
            avgQuiz = avgQuiz,
            homeworkCompletion = homeworkCompletion,
            positiveBehavior = positiveBehavior,
            negativeBehavior = negativeBehavior
        )

        return when (status) {
            InsightStatus.OPTIMAL -> {
                GhostInsight(
                    title = "Peak Performer",
                    summary = "$studentName is excelling across all metrics. Neural trends indicate high engagement and consistency.",
                    recommendation = "Consider peer-mentoring opportunities to further challenge this student.",
                    status = InsightStatus.OPTIMAL
                )
            }
            InsightStatus.CONCERNING -> {
                if (negativeBehavior > positiveBehavior) {
                    GhostInsight(
                        title = "Attention Required",
                        summary = "Recent behavioral fluctuations detected. Data suggests a potential disconnect with classroom expectations.",
                        recommendation = "Schedule a one-on-one check-in to identify underlying stressors.",
                        status = InsightStatus.CONCERNING
                    )
                } else {
                    GhostInsight(
                        title = "Academic Support Needed",
                        summary = "Downward trend in assessment outcomes. Homework consistency is below the class average.",
                        recommendation = "Provide supplementary resources or a targeted revision plan.",
                        status = InsightStatus.CONCERNING
                    )
                }
            }
            InsightStatus.IMPROVING -> {
                GhostInsight(
                    title = "Steady Progress",
                    summary = "Maintaining stable performance. Interactions are within expected neural parameters.",
                    recommendation = "Continue with current reinforcement strategies.",
                    status = InsightStatus.IMPROVING
                )
            }
            InsightStatus.UNKNOWN -> {
                GhostInsight(
                    title = "Data Incomplete",
                    summary = "Insufficient neural data points to synthesize a comprehensive insight for $studentName.",
                    recommendation = "Continue logging behavioral and academic interactions.",
                    status = InsightStatus.UNKNOWN
                )
            }
        }
    }

    /**
     * BOLT: Lightweight insight status calculation.
     *
     * Determines the [InsightStatus] directly from pre-calculated metrics, avoiding
     * list scans and full [GhostInsight] object allocations in the background update pipeline.
     */
    fun calculateInsightStatus(
        avgQuiz: Double,
        homeworkCompletion: Double,
        positiveBehavior: Int,
        negativeBehavior: Int
    ): InsightStatus {
        return when {
            avgQuiz > 0.8 && homeworkCompletion > 0.8 && negativeBehavior == 0 -> InsightStatus.OPTIMAL
            negativeBehavior > positiveBehavior -> InsightStatus.CONCERNING
            avgQuiz < 0.6 || homeworkCompletion < 0.6 -> InsightStatus.CONCERNING
            else -> InsightStatus.IMPROVING
        }
    }
}
