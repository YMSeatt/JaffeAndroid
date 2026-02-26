package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.random.Random

/**
 * GhostFutureEngine: Stochastic engine for simulating future classroom behavior.
 *
 * It uses heuristics and predictions from [GhostOracle] to generate a series of
 * "Simulated Events" that represent a likely future trajectory of the classroom.
 */
object GhostFutureEngine {

    /**
     * Generates simulated behavior events for the next X hours.
     *
     * @param students Current students and their positions.
     * @param historicalLogs Past behavior events.
     * @param hoursIntoFuture Duration of the simulation.
     * @return A list of simulated [BehaviorEvent] objects.
     */
    fun generateFutureEvents(
        students: List<StudentUiItem>,
        historicalLogs: List<BehaviorEvent>,
        hoursIntoFuture: Int = 1
    ): List<BehaviorEvent> {
        val simulatedEvents = mutableListOf<BehaviorEvent>()
        val prophecies = GhostOracle.consult(students, historicalLogs)
        val currentTime = System.currentTimeMillis()
        val random = Random(currentTime)

        students.forEach { student ->
            // Base probability of an event happening in the next hour
            var eventProbability = 0.2f

            // Influence from Prophecies
            val studentProphecies = prophecies.filter { it.studentId == student.id.toLong() }
            studentProphecies.forEach { prophecy ->
                when (prophecy.type) {
                    GhostOracle.ProphecyType.SOCIAL_FRICTION -> eventProbability += 0.3f
                    GhostOracle.ProphecyType.ENGAGEMENT_DROP -> eventProbability += 0.1f
                    GhostOracle.ProphecyType.LEADERSHIP_POTENTIAL -> eventProbability += 0.05f
                    GhostOracle.ProphecyType.ACADEMIC_SYNERGY -> eventProbability += 0.05f
                }
            }

            // High historical negative count increases event probability
            val negativeCount = historicalLogs.count {
                it.studentId == student.id.toLong() && it.type.contains("Negative", ignoreCase = true)
            }
            eventProbability += (negativeCount * 0.05f).coerceAtMost(0.4f)

            // Simulation loop for each hour
            for (h in 1..hoursIntoFuture) {
                if (random.nextFloat() < eventProbability) {
                    // Determine event type
                    val isNegative = if (studentProphecies.any { it.type == GhostOracle.ProphecyType.SOCIAL_FRICTION }) {
                        random.nextFloat() < 0.7f // 70% chance of negative if friction is predicted
                    } else {
                        random.nextFloat() < 0.3f // 30% baseline negative chance
                    }

                    val type = if (isNegative) {
                        listOf("Disruptive", "Off Task", "Conflict").random(random) + " (Simulated)"
                    } else {
                        listOf("Participating", "Focused", "Helping").random(random) + " (Simulated)"
                    }

                    simulatedEvents.add(
                        BehaviorEvent(
                            id = 0, // Mock ID
                            studentId = student.id.toLong(),
                            type = type,
                            timestamp = currentTime + (h * 60 * 60 * 1000L * random.nextFloat()).toLong(),
                            comment = "AI Predicted Event"
                        )
                    )
                }
            }
        }

        return simulatedEvents.sortedBy { it.timestamp }
    }
}
