package com.example.myapplication.labs.ghost.synapse

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import kotlinx.coroutines.delay
import java.util.*

/**
 * GhostSynapseEngine: A Proof of Concept for On-Device AI Narrative Synthesis.
 *
 * This engine simulates the capabilities of Gemini Nano (via AICore) by transforming
 * raw classroom data points into high-fidelity behavioral "narratives". It moves beyond
 * simple rule-based insights into a more generative approach.
 */
object GhostSynapseEngine {

    /**
     * Generates a "Neural Narrative" for a specific student.
     * In a production environment, this would invoke a GenerativeModel with a prompt.
     *
     * @param studentName Display name of the student.
     * @param behaviorLogs Historical behavior logs.
     * @param quizLogs Historical quiz logs.
     * @param homeworkLogs Historical homework logs.
     * @return A synthesized narrative string.
     */
    suspend fun generateNarrative(
        studentName: String,
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>,
        homeworkLogs: List<HomeworkLog>
    ): String {
        // Simulate "Thinking" time of an on-device LLM
        delay(1500)

        val positiveCount = behaviorLogs.count { !it.type.contains("Negative", ignoreCase = true) }
        val negativeCount = behaviorLogs.count { it.type.contains("Negative", ignoreCase = true) }
        val lastBehavior = behaviorLogs.maxByOrNull { it.timestamp }

        val avgQuiz = if (quizLogs.isNotEmpty()) {
            quizLogs.mapNotNull { it.markValue?.let { v -> it.maxMarkValue?.let { m -> v / m } } }.average()
        } else 0.7

        val homeworkRate = if (homeworkLogs.isNotEmpty()) {
            homeworkLogs.count { it.status.contains("Done", ignoreCase = true) }.toDouble() / homeworkLogs.size
        } else 0.9

        // Narrative construction logic (Generative Mock)
        val intro = when {
            avgQuiz > 0.9 -> "Neural patterns for $studentName indicate peak cognitive performance."
            negativeCount > positiveCount -> "Data streams suggest a period of behavioral turbulence for $studentName."
            else -> "A stable and consistent neural signature is detected for $studentName."
        }

        val behaviorDetail = when {
            negativeCount > 0 -> "The current session highlights $negativeCount points of friction, contrasting with $positiveCount instances of positive synergy."
            positiveCount > 5 -> "Exceptional participation density observed, with $positiveCount positive logs in the current cycle."
            else -> "Social engagement remains within nominal parameters."
        }

        val academicDetail = when {
            avgQuiz < 0.6 -> "Assessment telemetry shows a significant drop in retention (avg: ${(avgQuiz * 100).toInt()}%). Immediate cognitive scaffolding is recommended."
            homeworkRate < 0.7 -> "Homework consistency is decaying, which may impact future performance arcs."
            else -> "Academic trajectory is on an upward vector, supported by reliable homework completion ($homeworkRate)."
        }

        val conclusion = if (lastBehavior != null && lastBehavior.type.contains("Negative", ignoreCase = true)) {
            "Action Item: Address the most recent '${lastBehavior.type}' event to prevent negative feedback loops."
        } else {
            "Recommendation: Continue with current reinforcement protocols to maintain momentum."
        }

        return "$intro $behaviorDetail $academicDetail $conclusion"
    }
}
