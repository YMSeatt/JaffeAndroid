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
     * Generates a list of tactical interventions based on the current classroom state.
     * In a production environment, this would call into AICore (Gemini Nano) with
     * a complex pedagogical prompt.
     */
    suspend fun generateInterventions(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>,
        prophecies: List<GhostOracle.Prophecy>,
        goal: StrategistGoal
    ): List<TacticalIntervention> {
        // Simulate AI Synthesis Latency
        delay(1200)

        val interventions = mutableListOf<TacticalIntervention>()
        val studentMap = students.associateBy { it.id.toLong() }

        // 1. Process Prophecies into Tactics
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
        students.forEach { student ->
            val sId = student.id.toLong()
            val sLogs = behaviorLogs.filter { it.studentId == sId }
            val sQuizzes = quizLogs.filter { it.studentId == sId }

            val avgQuiz = if (sQuizzes.isNotEmpty()) {
                sQuizzes.mapNotNull { it.markValue?.let { v -> it.maxMarkValue?.let { m -> v / m } } }.average()
            } else 0.7

            // High Performer Logic (Excellence Goal)
            if (goal == StrategistGoal.EXCELLENCE && avgQuiz > 0.9 && sLogs.count { !it.type.contains("Negative", ignoreCase = true) } > 3) {
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

            // High Frequency Negative Logic
            val recentNegatives = sLogs.filter { it.type.contains("Negative", ignoreCase = true) && System.currentTimeMillis() - it.timestamp < 3600000L }
            if (recentNegatives.size > 1) {
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
