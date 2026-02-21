package com.example.myapplication.labs.ghost

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.sin

class GhostFluxEngineTest {

    @Test
    fun testCalculateFlowIntensity_EmptyLogs() {
        val intensity = GhostFluxEngine.calculateFlowIntensity(20, 0)
        assertEquals(0.1f, intensity, 0.001f)
    }

    @Test
    fun testCalculateFlowIntensity_NormalActivity() {
        val studentCount = 20
        val logCount = 50
        // baseIntensity = 50 / (20 * 5) = 0.5
        // tempo = 1.0 + 0.2 * sin(50 / 5) = 1.0 + 0.2 * sin(10)
        val expectedTempo = 1.0f + 0.2f * sin(50f / 5f)
        val expectedIntensity = (0.5f * expectedTempo).coerceIn(0.1f, 1.0f)

        val intensity = GhostFluxEngine.calculateFlowIntensity(studentCount, logCount)
        assertEquals(expectedIntensity, intensity, 0.001f)
    }

    @Test
    fun testCalculateFlowIntensity_HighActivity() {
        val studentCount = 10
        val logCount = 200
        // baseIntensity = 200 / (10 * 5) = 4.0
        // tempo = 1.0 + 0.2 * sin(40)
        // expectedIntensity should be capped at 1.0

        val intensity = GhostFluxEngine.calculateFlowIntensity(studentCount, logCount)
        assertEquals(1.0f, intensity, 0.001f)
    }

    @Test
    fun testCalculateFlowIntensity_ZeroStudentsSafety() {
        val intensity = GhostFluxEngine.calculateFlowIntensity(0, 10)
        // effectiveStudentCount = 1
        // baseIntensity = 10 / (1 * 5) = 2.0
        // capped at 1.0
        assertEquals(1.0f, intensity, 0.001f)
    }

    @Test
    fun testCalculateFlowIntensity_MinimumValue() {
        val studentCount = 100
        val logCount = 1
        // baseIntensity = 1 / (100 * 5) = 0.002
        // tempo ~ 1.0
        // (baseIntensity * tempo) will be around 0.002
        // Result should be coerced to 0.1
        val intensity = GhostFluxEngine.calculateFlowIntensity(studentCount, logCount)
        assertEquals(0.1f, intensity, 0.001f)
    }
}
