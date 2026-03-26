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
        val params1 = GhostIrisEngine.calculateIris(studentId, 0.5f, 0.75f, 0)
        val params2 = GhostIrisEngine.calculateIris(studentId, 0.5f, 0.75f, 0)

        assertEquals(params1.seed, params2.seed)
        assertEquals(params1.colorA, params2.colorA)
        assertEquals(params1.colorB, params2.colorB)
        assertEquals(params1.complexity, params2.complexity, 0.001f)
    }

    @Test
    fun `calculateIris should produce different seeds for different student IDs`() {
        val params1 = GhostIrisEngine.calculateIris(1L, 0.5f, 0.75f, 0)
        val params2 = GhostIrisEngine.calculateIris(2L, 0.5f, 0.75f, 0)

        assertNotEquals(params1.seed, params2.seed)
    }

    @Test
    fun `calculateIris complexity should increase with more logs`() {
        val studentId = 1L
        val paramsLow = GhostIrisEngine.calculateIris(studentId, 0.5f, 0.75f, 0)
        val paramsHigh = GhostIrisEngine.calculateIris(studentId, 0.5f, 0.75f, 10)

        assertTrue("Complexity should increase with logs", paramsHigh.complexity > paramsLow.complexity)
    }

    @Test
    fun `calculateIris colorA should reflect behavior balance`() {
        val studentId = 1L

        val paramsPositive = GhostIrisEngine.calculateIris(studentId, 0.8f, 0.75f, 1)
        val paramsNegative = GhostIrisEngine.calculateIris(studentId, 0.2f, 0.75f, 1)

        assertNotEquals("Colors should differ based on behavior", paramsPositive.colorA, paramsNegative.colorA)
    }
}
