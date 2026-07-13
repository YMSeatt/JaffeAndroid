package com.example.myapplication.labs.ghost.strategist

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.QuizLog
import com.example.myapplication.labs.ghost.GhostOracle
import com.example.myapplication.ui.model.StudentUiItem
import kotlinx.coroutines.delay

/**
 * GhostStrategistEngine: A Generative AI Co-Pilot for Classroom Management.
 *
 * This engine simulates a high-fidelity AI advisor (Gemini Nano) that translates
 * raw data, historical trends, and predictive "Prophecies" into concrete,
 * pedagogical tactical interventions.
 */
object GhostStrategistEngine {

    enum class StrategistGoal {
        HARMONY,    // Focus on resolving social friction and negative loops
        EXCELLENCE, // Focus on accelerating high-performing students
        STABILITY   // Focus on maintaining engagement and preventing drop-offs
    }

    data class TacticalIntervention(
        val studentId: Long,
        val title: String,
        val description: String,
        val urgency: Float, // 0.0 to 1.0
        val category: InterventionCategory
    )

    enum class InterventionCategory {
        SOCIAL_DYNAMICS,
        ACADEMIC_ACCELERATION,
        BEHAVIORAL_REINFORCEMENT,
        TEMPORAL_ADJUSTMENT
    }

    /**
     * Generates a prioritized list of tactical interventions based on the current
     * classroom state, historical logs, and AI-predicted prophecies.
     *
     * In a production environment, this would call into Android AICore (Gemini Nano) with
     * a complex pedagogical prompt. This implementation uses high-fidelity heuristics to
     * simulate that behavior.
     *
     * @param students The current list of students for metadata lookup.
     * @param behaviorLogs Historical behavior data for longitudinal analysis.
     * @param quizLogs Academic history used to identify performance trends.
     * @param prophecies Predicted trends from [GhostOracle] (e.g., social friction).
     * @param goal The primary pedagogical focus for the current synthesis.
     * @return A list of unique [TacticalIntervention]s, sorted by urgency.
     */
    suspend fun generateInterventions(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>,
        prophecies: List<GhostOracle.Prophecy>,
        goal: StrategistGoal
    ): List<TacticalIntervention> {
        // BOLT: Simulate AI Synthesis Latency (1200ms) to provide a "Generative" UI feel
        // and match expected inference times of LLMs on mobile hardware.
        delay(1200)

        val interventions = mutableListOf<TacticalIntervention>()
        val studentMap = students.associateBy { it.id.toLong() }

        // BOLT: Pre-group logs for O(1) lookup during O(S) student loop,
        // transforming O(S * (B+Q)) into O(S + B + Q).
        val behaviorLogsByStudent = behaviorLogs.groupBy { it.studentId }
        val quizLogsByStudent = quizLogs.groupBy { it.studentId }

        // 1. Process Prophecies into Tactics:
        // Maps high-level AI predictions into concrete, wording-heavy pedagogical actions.
        prophecies.forEach { prophecy ->
            val student = studentMap[prophecy.studentId]
            val name = student?.fullName?.value ?: "Student #${prophecy.studentId}"

            when (prophecy.type) {
                GhostOracle.ProphecyType.SOCIAL_FRICTION -> {
                    if (goal == StrategistGoal.HARMONY || goal == StrategistGoal.STABILITY) {
                        interventions.add(
                            TacticalIntervention(
                                studentId = prophecy.studentId,
                                title = "Proactive Buffer Insertion",
                                description = "Social friction predicted for $name. Tactical recommendation: Introduce a 5-minute collaborative task with a neutral partner to de-escalate tension.",
                                urgency = prophecy.confidence,
                                category = InterventionCategory.SOCIAL_DYNAMICS
                            )
                        )
                    }
                }
                GhostOracle.ProphecyType.ENGAGEMENT_DROP -> {
                    if (goal != StrategistGoal.EXCELLENCE) {
                        interventions.add(
                            TacticalIntervention(
                                studentId = prophecy.studentId,
                                title = "Positive Recalibration",
                                description = "Engagement for $name is decaying. Strategic Action: Deliver immediate 'Micro-Feedback' for their current work to reset the behavioral baseline.",
                                urgency = 0.8f,
                                category = InterventionCategory.BEHAVIORAL_REINFORCEMENT
                            )
                        )
                    }
                }
                else -> {}
            }
        }

        // 2. Data-Driven Heuristics
        val currentTime = System.currentTimeMillis()
        val oneHourAgo = currentTime - java.util.concurrent.TimeUnit.HOURS.toMillis(1)

        students.forEach { student ->
            val sId = student.id.toLong()
            val sLogs = behaviorLogsByStudent[sId] ?: emptyList()
            val sQuizzes = quizLogsByStudent[sId] ?: emptyList()

            // BOLT: Single-pass avgQuiz calculation to avoid functional operator overhead and list allocations.
            var quizSum = 0.0
            var quizCount = 0
            for (log in sQuizzes) {
                val v = log.markValue
                val m = log.maxMarkValue
                if (v != null && m != null && m > 0) {
                    quizSum += (v / m)
                    quizCount++
                }
            }
            val avgQuiz = if (quizCount > 0) (quizSum / quizCount) else 0.7

            // High Performer Logic (Excellence Goal)
            // BOLT: Single-pass non-negative count to avoid O(L) list filtering.
            if (goal == StrategistGoal.EXCELLENCE && avgQuiz > 0.9) {
                var posCount = 0
                for (log in sLogs) {
                    if (!log.type.contains("Negative", ignoreCase = true)) {
                        posCount++
                    }
                }
                if (posCount > 3) {
                    interventions.add(
                        TacticalIntervention(
                            studentId = sId,
                            title = "Cognitive Acceleration",
                            description = "Neural patterns for ${student.fullName.value} indicate mastery. Suggestion: Assign a 'Peer Tutor' role or provide an 'Advanced Depth' challenge task.",
                            urgency = 0.6f,
                            category = InterventionCategory.ACADEMIC_ACCELERATION
                        )
                    )
                }
            }

            // High Frequency Negative Logic:
            // Detects behavioral "bursts" using a 1-hour temporal window.
            // A cluster of negatives within this window indicates a high probability of
            // escalation requiring an "Atmospheric Reset".
            // BOLT: Replaced functional filter/count with manual loop to avoid allocations.
            var recentNegativeCount = 0
            for (log in sLogs) {
                if (log.timestamp >= oneHourAgo && log.type.contains("Negative", ignoreCase = true)) {
                    recentNegativeCount++
                    if (recentNegativeCount > 1) break
                }
            }

            if (recentNegativeCount > 1) {
                interventions.add(
                    TacticalIntervention(
                        studentId = sId,
                        title = "Atmospheric Reset",
                        description = "High-density behavioral friction detected for ${student.fullName.value}. Recommended: Implement a private 1:1 'Neural Check-in' before the next transition.",
                        urgency = 0.95f,
                        category = InterventionCategory.TEMPORAL_ADJUSTMENT
                    )
                )
            }
        }

        return interventions.sortedByDescending { it.urgency }.distinctBy { it.title + it.studentId }
    }
}
