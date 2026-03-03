package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Student
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.abs
import kotlin.math.sqrt

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
     * Generates a list of prophecies based on current classroom state and behavioral history.
     * In a real implementation, this would call into the On-Device AI Core (Gemini Nano).
     *
     * Heuristics:
     * 1. **Social Friction**: Identified when two or more students with multiple negative
     *    behavior logs are positioned in close physical proximity (< 150 logical units).
     * 2. **Engagement Drop**: Identified when a student has a history of logs but has not
     *    received positive reinforcement (non-negative logs) within the last 7 days.
     * 3. **Critical Intervention**: Flagged if a student has logs but zero positive baseline events.
     *
     * @param students Current UI state of all students.
     * @param behaviorLogs Historical behavior events for all students.
     * @return A list of unique [Prophecy] objects representing predicted classroom trends.
     */
    fun consult(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>
    ): List<Prophecy> {
        val rawStudents = students.map { uiItem ->
            Student(
                id = uiItem.id.toLong(),
                firstName = uiItem.fullName.value.split(" ").firstOrNull() ?: "",
                lastName = uiItem.fullName.value.split(" ").lastOrNull() ?: "",
                xPosition = uiItem.xPosition.value,
                yPosition = uiItem.yPosition.value
            )
        }
        return consult(rawStudents, behaviorLogs)
    }

    /**
     * BOLT: Optimized overload for [consult] that accepts raw Student entities and pre-grouped logs.
     * Transforms O(N^2 * L) into O(N^2 + L) by pre-calculating indices and using O(1) lookups.
     */
    fun consult(
        students: List<Student>,
        behaviorLogs: List<BehaviorEvent>,
        behaviorLogsByStudent: Map<Long, List<BehaviorEvent>>? = null
    ): List<Prophecy> {
        if (students.isEmpty()) return emptyList()

        val prophecies = mutableListOf<Prophecy>()
        val currentTime = System.currentTimeMillis()

        // 1. Pre-process logs for O(1) lookup during O(N^2) loop
        val logsByStudent = behaviorLogsByStudent ?: behaviorLogs.groupBy { it.studentId }
        val negativeCounts = logsByStudent.mapValues { (_, logs) ->
            logs.count { it.type.contains("Negative", ignoreCase = true) }
        }

        // 2. Optimized Social Friction Analysis (O(N^2))
        // Replaced nested forEach/filter with indexed loops to eliminate object churn.
        val n = students.size
        for (i in 0 until n) {
            val s1 = students[i]
            val s1NegCount = negativeCounts[s1.id] ?: 0

            // Only evaluate friction if s1 has potential for friction
            if (s1NegCount > 1) {
                for (j in i + 1 until n) {
                    val s2 = students[j]
                    val s2NegCount = negativeCounts[s2.id] ?: 0

                    if (s2NegCount > 1) {
                        val dx = s1.xPosition - s2.xPosition
                        val dy = s1.yPosition - s2.yPosition
                        val distSq = dx * dx + dy * dy

                        if (distSq < 150f * 150f) { // Close proximity (150^2)
                            prophecies.add(
                                Prophecy(
                                    studentId = s1.id,
                                    type = ProphecyType.SOCIAL_FRICTION,
                                    description = "Predicted tension between ${s1.firstName} ${s1.lastName} and ${s2.firstName} ${s2.lastName}. High probability of disruptive interaction.",
                                    confidence = 0.85f
                                )
                            )
                        }
                    }
                }
            }

            // 3. Optimized Engagement Drop Analysis (O(L_student))
            val myLogs = logsByStudent[s1.id] ?: emptyList()
            if (myLogs.isNotEmpty()) {
                // Find last positive log using manual loop to avoid multiple filter/sort cycles
                var lastPositiveTimestamp = -1L
                for (log in myLogs) {
                    if (!log.type.contains("Negative", ignoreCase = true)) {
                        if (log.timestamp > lastPositiveTimestamp) {
                            lastPositiveTimestamp = log.timestamp
                        }
                    }
                }

                if (lastPositiveTimestamp != -1L) {
                    val daysSince = (currentTime - lastPositiveTimestamp) / (1000 * 60 * 60 * 24)
                    if (daysSince > 7) {
                        prophecies.add(
                            Prophecy(
                                studentId = s1.id,
                                type = ProphecyType.ENGAGEMENT_DROP,
                                description = "Neural trends indicate fading engagement. Student hasn't received positive reinforcement in $daysSince days.",
                                confidence = 0.7f
                            )
                        )
                    }
                } else {
                    prophecies.add(
                        Prophecy(
                            studentId = s1.id,
                            type = ProphecyType.ENGAGEMENT_DROP,
                            description = "Critical: No positive baseline established. Early intervention recommended.",
                            confidence = 0.9f
                        )
                    )
                }
            }
        }

        return prophecies.distinctBy { it.description }
    }
}
