package com.example.myapplication.labs.ghost.lod

import org.junit.Assert.assertEquals
import org.junit.Test

class GhostLODEngineTest {

    @Test
    fun `calculateLOD should return correct levels for various scales`() {
        // Critical (> 1.5)
        assertEquals(GhostLODEngine.DetailLevel.CRITICAL, GhostLODEngine.calculateLOD(2.0f))
        assertEquals(GhostLODEngine.DetailLevel.CRITICAL, GhostLODEngine.calculateLOD(1.6f))

        // Full (0.8 to 1.5)
        assertEquals(GhostLODEngine.DetailLevel.FULL, GhostLODEngine.calculateLOD(1.5f))
        assertEquals(GhostLODEngine.DetailLevel.FULL, GhostLODEngine.calculateLOD(1.0f))
        assertEquals(GhostLODEngine.DetailLevel.FULL, GhostLODEngine.calculateLOD(0.8f))

        // Compact (0.4 to 0.8)
        assertEquals(GhostLODEngine.DetailLevel.COMPACT, GhostLODEngine.calculateLOD(0.79f))
        assertEquals(GhostLODEngine.DetailLevel.COMPACT, GhostLODEngine.calculateLOD(0.5f))
        assertEquals(GhostLODEngine.DetailLevel.COMPACT, GhostLODEngine.calculateLOD(0.4f))

        // Minimal (< 0.4)
        assertEquals(GhostLODEngine.DetailLevel.MINIMAL, GhostLODEngine.calculateLOD(0.39f))
        assertEquals(GhostLODEngine.DetailLevel.MINIMAL, GhostLODEngine.calculateLOD(0.1f))
    }
}
