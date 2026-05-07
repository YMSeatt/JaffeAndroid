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
        val spatialDensity = 0.0f
        // baseIntensity = 50 / (20 * 5) = 0.5
        // tempo = 1.0 + 0.2 * sin(50 / 5) = 1.0 + 0.2 * sin(10)
        val expectedTempo = 1.0f + 0.2f * sin(50f / 5f)
        val expectedIntensity = (0.5f * expectedTempo + spatialDensity * 0.3f).coerceIn(0.1f, 1.0f)

        val intensity = GhostFluxEngine.calculateFlowIntensity(studentCount, logCount, spatialDensity)
        assertEquals(expectedIntensity, intensity, 0.001f)
    }

    @Test
    fun testCalculateFlowIntensity_WithSpatialDensity() {
        val studentCount = 20
        val logCount = 50
        val spatialDensity = 0.5f
        // baseIntensity = 0.5
        // tempo = 1.0 + 0.2 * sin(10)
        val expectedTempo = 1.0f + 0.2f * sin(50f / 5f)
        val expectedIntensity = (0.5f * expectedTempo + spatialDensity * 0.3f).coerceIn(0.1f, 1.0f)

        val intensity = GhostFluxEngine.calculateFlowIntensity(studentCount, logCount, spatialDensity)
        assertEquals(expectedIntensity, intensity, 0.001f)
    }

    @Test
    fun testCalculateSpatialDensity() {
        val x = floatArrayOf(100f, 150f, 200f, 3000f)
        val y = floatArrayOf(100f, 150f, 200f, 3000f)
        // 100,100 is close to 150,150 and 200,200 (dist ~70, ~141) < 800
        // 3000,3000 is far from everything
        // 0: neighbors {1, 2} -> 2/3
        // 1: neighbors {0, 2} -> 2/3
        // 2: neighbors {0, 1} -> 2/3
        // 3: neighbors {} -> 0/3
        // avg = (2/3 + 2/3 + 2/3 + 0) / 4 = 2 / 4 = 0.5
        val density = GhostFluxEngine.calculateSpatialDensity(x, y)
        assertEquals(0.5f, density, 0.001f)
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
