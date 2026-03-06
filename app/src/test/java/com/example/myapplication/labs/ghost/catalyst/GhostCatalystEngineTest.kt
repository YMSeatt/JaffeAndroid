package com.example.myapplication.labs.ghost.catalyst

import com.example.myapplication.data.BehaviorEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostCatalystEngineTest {

    @Test
    fun `test reaction detection within spatio-temporal window`() {
        val engine = GhostCatalystEngine()

        val student1 = GhostCatalystEngine.StudentPos(1, 100f, 100f)
        val student2 = GhostCatalystEngine.StudentPos(2, 150f, 150f)

        val students = listOf(student1, student2)

        val events = listOf(
            BehaviorEvent(studentId = 1, type = "Positive", timestamp = 1000L),
            BehaviorEvent(studentId = 2, type = "Positive", timestamp = 2000L)
        )

        val reactions = engine.calculateReactions(students, events)

        assertEquals(1, reactions.size)
        assertEquals(1L, reactions[0].catalystId)
        assertEquals(2L, reactions[0].reactantId)
        assertTrue(reactions[0].intensity > 0.9f)
    }

    @Test
    fun `test temporal pruning of reactions`() {
        val engine = GhostCatalystEngine()

        val student1 = GhostCatalystEngine.StudentPos(1, 100f, 100f)
        val student2 = GhostCatalystEngine.StudentPos(2, 150f, 150f)

        val students = listOf(student1, student2)

        val events = listOf(
            BehaviorEvent(studentId = 1, type = "Positive", timestamp = 1000L),
            BehaviorEvent(studentId = 2, type = "Positive", timestamp = 1000L + 400_000L) // Outside 300s window
        )

        val reactions = engine.calculateReactions(students, events)

        assertTrue(reactions.isEmpty())
    }

    @Test
    fun `test kinetic metrics calculation`() {
        val engine = GhostCatalystEngine()
        val reactions = listOf(
            GhostCatalystEngine.Reaction(1L, 2L, 0.8f, 2000L),
            GhostCatalystEngine.Reaction(1L, 3L, 0.7f, 3000L)
        )
        val events = listOf(
            BehaviorEvent(studentId = 1, type = "Positive", timestamp = 1000L),
            BehaviorEvent(studentId = 1, type = "Positive", timestamp = 5000L)
        )

        val metrics = engine.calculateMetrics(1L, reactions, events)

        assertEquals(0.4f, metrics.reactionRate, 0.01f)
        assertTrue(metrics.activationEnergy > 0f)
    }
}
