package com.example.myapplication.labs.ghost.catalyst

import com.example.myapplication.data.BehaviorEvent
import kotlin.math.sqrt

/**
 * GhostCatalystEngine: Detects behavioral chain reactions in the classroom.
 *
 * This engine treats students as chemical components. When a "Catalyst" student
 * performs an action, it can trigger "Reactions" in nearby students within a
 * specific spatio-temporal window.
 *
 * ### Kinetics Metrics:
 * - **Reaction Rate**: Frequency of triggered events per time unit.
 * - **Activation Energy**: The behavioral threshold required to initiate a chain reaction.
 */
class GhostCatalystEngine {
    data class Reaction(
        val catalystId: Long,
        val reactantId: Long,
        val intensity: Float, // 0.0 to 1.0 based on temporal proximity
        val timestamp: Long
    )

    data class CatalystMetrics(
        val reactionRate: Float, // reactions per unit
        val activationEnergy: Float // 0.0 to 1.0 (lower is more volatile)
    )

    /**
     * Minimal spatial info for students to avoid complex dependencies in tests.
     */
    data class StudentPos(val id: Long, val x: Float, val y: Float)

    /**
     * Identifies reactions where one student's log precedes another's in spatial proximity.
     */
    fun calculateReactions(
        students: List<StudentPos>,
        events: List<BehaviorEvent>,
        timeWindowMs: Long = 300_000L,
        radius: Float = 800f
    ): List<Reaction> {
        val reactions = mutableListOf<Reaction>()
        val radiusSq = radius * radius

        // BOLT: Removed redundant sortedBy call as DAO provides logs in sorted order (DESC).
        // Using asReversed() to process chronologically without O(N log N) overhead.
        val chronologicalEvents = events.asReversed()
        val studentMap = students.associateBy { it.id }

        for (i in chronologicalEvents.indices) {
            val catalystEvent = chronologicalEvents[i]
            val catalystStudent = studentMap[catalystEvent.studentId] ?: continue

            for (j in i + 1 until chronologicalEvents.size) {
                val reactantEvent = chronologicalEvents[j]

                // Temporal Pruning
                if (reactantEvent.timestamp > catalystEvent.timestamp + timeWindowMs) break

                // Self-reaction is ignored
                if (catalystEvent.studentId == reactantEvent.studentId) continue

                val reactantStudent = studentMap[reactantEvent.studentId] ?: continue

                // Spatial Pruning (Squared distance for BOLT performance)
                val dx = catalystStudent.xPosition - reactantStudent.xPosition
                val dy = catalystStudent.yPosition - reactantStudent.yPosition
                val distSq = dx * dx + dy * dy

                if (distSq < radiusSq) {
                    val timeDiff = (reactantEvent.timestamp - catalystEvent.timestamp).toFloat() / timeWindowMs
                    val intensity = (1.0f - timeDiff).coerceIn(0.1f, 1.0f)
                    reactions.add(
                        Reaction(
                            catalystId = catalystEvent.studentId,
                            reactantId = reactantEvent.studentId,
                            intensity = intensity,
                            timestamp = reactantEvent.timestamp
                        )
                    )
                }
            }
        }
        return reactions.distinctBy { it.catalystId to it.reactantId }.take(100)
    }

    // Overload for StudentUiItem if needed in UI, but keep core logic testable
    fun calculateReactionsUI(
        students: List<com.example.myapplication.ui.model.StudentUiItem>,
        events: List<BehaviorEvent>,
        timeWindowMs: Long = 300_000L,
        radius: Float = 800f
    ): List<Reaction> {
        val posList = students.map { StudentPos(it.id.toLong(), it.xPosition.value, it.yPosition.value) }
        return calculateReactions(posList, events, timeWindowMs, radius)
    }

    private val StudentPos.xPosition get() = x
    private val StudentPos.yPosition get() = y

    /**
     * Calculates kinetic metrics for a specific student.
     */
    fun calculateMetrics(
        studentId: Long,
        reactions: List<Reaction>,
        allEvents: List<BehaviorEvent>
    ): CatalystMetrics {
        val studentReactions = reactions.filter { it.catalystId == studentId }
        val reactionRate = studentReactions.size.toFloat() / 5.0f // Normalized to 5-min window

        val studentEvents = allEvents.filter { it.studentId == studentId }
        val avgFrequency = if (studentEvents.size < 2) 0.05f else {
            val spanSec = (studentEvents.last().timestamp - studentEvents.first().timestamp).coerceAtLeast(1000L) / 1000f
            studentEvents.size / spanSec
        }

        // Activation Energy: High frequency students are 'hotter' and require less energy to react.
        val activationEnergy = (1.0f - (avgFrequency * 2.0f).coerceIn(0.0f, 0.9f))

        return CatalystMetrics(reactionRate, activationEnergy)
    }
}
