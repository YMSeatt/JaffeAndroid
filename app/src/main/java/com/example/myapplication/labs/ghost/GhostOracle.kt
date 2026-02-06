package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.abs

/**
 * GhostOracle: An experimental on-device AI engine (simulating Gemini Nano).
 *
 * This engine analyzes classroom state and behavioral history to generate "proactive"
 * predictions about social dynamics and academic synergy.
 */
object GhostOracle {

    data class Prophecy(
        val studentId: Long,
        val type: ProphecyType,
        val description: String,
        val confidence: Float // 0.0 to 1.0
    )

    enum class ProphecyType {
        SOCIAL_FRICTION,
        ACADEMIC_SYNERGY,
        ENGAGEMENT_DROP,
        LEADERSHIP_POTENTIAL
    }

    /**
     * Generates a list of prophecies based on current classroom state.
     * In a real implementation, this would call into the On-Device AI Core (Gemini Nano).
     */
    fun consult(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>
    ): List<Prophecy> {
        val prophecies = mutableListOf<Prophecy>()

        // Analyze Social Friction (students with negative logs sitting close together)
        val negativeLogsByStudent = behaviorLogs.filter { it.type.contains("Negative", ignoreCase = true) }
            .groupBy { it.studentId }

        students.forEach { student ->
            val myNegativeCount = negativeLogsByStudent[student.id.toLong()]?.size ?: 0

            // Check neighbors
            students.filter { it.id > student.id }.forEach { neighbor ->
                val dist = calculateDistance(student, neighbor)
                if (dist < 150) { // Close proximity
                    val neighborNegativeCount = negativeLogsByStudent[neighbor.id.toLong()]?.size ?: 0

                    if (myNegativeCount > 1 && neighborNegativeCount > 1) {
                        prophecies.add(
                            Prophecy(
                                studentId = student.id.toLong(),
                                type = ProphecyType.SOCIAL_FRICTION,
                                description = "Predicted tension between ${student.fullName} and ${neighbor.fullName}. High probability of disruptive interaction.",
                                confidence = 0.85f
                            )
                        )
                    }
                }
            }

            // Analyze Engagement Drop (long time since last positive log)
            val myLogs = behaviorLogs.filter { it.studentId == student.id.toLong() }
            val lastPositive = myLogs.filter { !it.type.contains("Negative", ignoreCase = true) }
                .maxByOrNull { it.timestamp }

            if (lastPositive != null) {
                val daysSince = (System.currentTimeMillis() - lastPositive.timestamp) / (1000 * 60 * 60 * 24)
                if (daysSince > 7) {
                    prophecies.add(
                        Prophecy(
                            studentId = student.id.toLong(),
                            type = ProphecyType.ENGAGEMENT_DROP,
                            description = "Neural trends indicate fading engagement. Student hasn't received positive reinforcement in $daysSince days.",
                            confidence = 0.7f
                        )
                    )
                }
            } else if (myLogs.isNotEmpty()) {
                prophecies.add(
                    Prophecy(
                        studentId = student.id.toLong(),
                        type = ProphecyType.ENGAGEMENT_DROP,
                        description = "Critical: No positive baseline established. Early intervention recommended.",
                        confidence = 0.9f
                    )
                )
            }
        }

        return prophecies.distinctBy { it.description }
    }

    private fun calculateDistance(s1: StudentUiItem, s2: StudentUiItem): Float {
        val dx = s1.xPosition.value - s2.xPosition.value
        val dy = s1.yPosition.value - s2.yPosition.value
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }
}
