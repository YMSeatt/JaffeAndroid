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

    @Test
    fun `test global kinetics analysis parity with python`() {
        val engine = GhostCatalystEngine()

        // Mock events spanning 10 seconds (globalFreq = 2/10 = 0.2)
        val events = listOf(
            BehaviorEvent(studentId = 1, type = "Positive", timestamp = 1000L),
            BehaviorEvent(studentId = 2, type = "Positive", timestamp = 11000L)
        )

        // 5 unique reactions
        val reactions = listOf(
            GhostCatalystEngine.Reaction(1L, 2L, 1.0f, 2000L),
            GhostCatalystEngine.Reaction(2L, 3L, 1.0f, 3000L),
            GhostCatalystEngine.Reaction(3L, 4L, 1.0f, 4000L),
            GhostCatalystEngine.Reaction(4L, 5L, 1.0f, 5000L),
            GhostCatalystEngine.Reaction(5L, 6L, 1.0f, 6000L)
        )

        val kinetics = engine.analyzeCatalystKinetics(events, reactions)

        // reactionRate = 5 / 5.0 = 1.0
        assertEquals(1.0f, kinetics.reactionRate, 0.01f)

        // activationEnergy = 1.0 - (0.2 * 10.0) = -1.0 -> coerced to 0.1
        assertEquals(0.1f, kinetics.activationEnergy, 0.01f)

        // equilibriumConstant = 1.0 / 0.1 = 10.0
        assertEquals(10.0f, kinetics.equilibriumConstant, 0.01f)

        val report = engine.generateCatalystReport(kinetics)
        assertTrue(report.contains("# 🧪 GHOST CATALYST: KINETICS ANALYSIS REPORT"))
        assertTrue(report.contains("| Reaction Rate | 1.00 | r/5min |"))
    }
}
