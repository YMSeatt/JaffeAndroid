package com.example.myapplication.labs.ghost.strategist

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.labs.ghost.GhostOracle
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostStrategistEngineTest {

    @Test
    fun testGenerateInterventionsSocialFriction() = runBlocking {
        val prophecies = listOf(
            GhostOracle.Prophecy(
                studentId = 1L,
                type = GhostOracle.ProphecyType.SOCIAL_FRICTION,
                description = "Predicted tension",
                confidence = 0.9f
            )
        )

        val interventions = GhostStrategistEngine.generateInterventions(
            students = emptyList(),
            behaviorLogs = emptyList(),
            quizLogs = emptyList(),
            prophecies = prophecies,
            goal = GhostStrategistEngine.StrategistGoal.HARMONY
        )

        assertEquals(1, interventions.size)
        assertEquals("Proactive Buffer Insertion", interventions[0].title)
        assertEquals(GhostStrategistEngine.InterventionCategory.SOCIAL_DYNAMICS, interventions[0].category)
        assertTrue(interventions[0].urgency >= 0.9f)
    }

    @Test
    fun testGenerateInterventionsNegativeLoop() = runBlocking {
        val now = System.currentTimeMillis()
        val behaviorLogs = listOf(
            BehaviorEvent(1L, "Negative behavior", now - 1000, null),
            BehaviorEvent(1L, "Negative behavior", now - 2000, null)
        )

        val interventions = GhostStrategistEngine.generateInterventions(
            students = emptyList(),
            behaviorLogs = behaviorLogs,
            quizLogs = emptyList(),
            prophecies = emptyList(),
            goal = GhostStrategistEngine.StrategistGoal.STABILITY
        )

        // Should find "Atmospheric Reset"
        val resetIntervention = interventions.find { it.title == "Atmospheric Reset" }
        assertTrue(resetIntervention != null)
        assertEquals(GhostStrategistEngine.InterventionCategory.TEMPORAL_ADJUSTMENT, resetIntervention?.category)
        assertEquals(0.95f, resetIntervention?.urgency ?: 0f, 0.01f)
    }
}
