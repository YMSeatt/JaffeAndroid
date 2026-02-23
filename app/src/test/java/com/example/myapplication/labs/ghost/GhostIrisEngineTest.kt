package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostIrisEngineTest {

    @Test
    fun `calculateIris should be deterministic for the same student ID`() {
        val studentId = 1L
        val params1 = GhostIrisEngine.calculateIris(studentId, emptyList(), emptyList(), emptyList())
        val params2 = GhostIrisEngine.calculateIris(studentId, emptyList(), emptyList(), emptyList())

        assertEquals(params1.seed, params2.seed)
        assertEquals(params1.colorA, params2.colorA)
        assertEquals(params1.colorB, params2.colorB)
        assertEquals(params1.complexity, params2.complexity, 0.001f)
    }

    @Test
    fun `calculateIris should produce different seeds for different student IDs`() {
        val params1 = GhostIrisEngine.calculateIris(1L, emptyList(), emptyList(), emptyList())
        val params2 = GhostIrisEngine.calculateIris(2L, emptyList(), emptyList(), emptyList())

        assertNotEquals(params1.seed, params2.seed)
    }

    @Test
    fun `calculateIris complexity should increase with more logs`() {
        val studentId = 1L
        val paramsLow = GhostIrisEngine.calculateIris(studentId, emptyList(), emptyList(), emptyList())

        val behaviorLogs = List(10) { BehaviorEvent(studentId = studentId, type = "Positive", timestamp = 0L, comment = null) }
        val paramsHigh = GhostIrisEngine.calculateIris(studentId, behaviorLogs, emptyList(), emptyList())

        assertTrue("Complexity should increase with logs", paramsHigh.complexity > paramsLow.complexity)
    }

    @Test
    fun `calculateIris colorA should reflect behavior balance`() {
        val studentId = 1L

        val positiveLogs = listOf(BehaviorEvent(studentId = studentId, type = "Positive", timestamp = 0L, comment = null))
        val paramsPositive = GhostIrisEngine.calculateIris(studentId, positiveLogs, emptyList(), emptyList())

        val negativeLogs = listOf(BehaviorEvent(studentId = studentId, type = "Negative Behavior", timestamp = 0L, comment = null))
        val paramsNegative = GhostIrisEngine.calculateIris(studentId, negativeLogs, emptyList(), emptyList())

        assertNotEquals("Colors should differ based on behavior", paramsPositive.colorA, paramsNegative.colorA)
    }
}
