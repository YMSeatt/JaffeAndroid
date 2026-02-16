package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostSpectraEngineTest {

    @Test
    fun testCalculateSpectralDensity_EmptyLogs() {
        val density = GhostSpectraEngine.calculateSpectralDensity(emptyList())
        assertEquals(0.2f, density, 0.01f)
    }

    @Test
    fun testCalculateSpectralDensity_HighDiversity() {
        val logs = listOf(
            BehaviorEvent(studentId = 1L, type = "Type 1", timestamp = 1000L, comment = null),
            BehaviorEvent(studentId = 1L, type = "Type 2", timestamp = 1000L, comment = null),
            BehaviorEvent(studentId = 1L, type = "Type 3", timestamp = 1000L, comment = null),
            BehaviorEvent(studentId = 1L, type = "Type 4", timestamp = 1000L, comment = null),
            BehaviorEvent(studentId = 1L, type = "Type 5", timestamp = 1000L, comment = null)
        )
        val density = GhostSpectraEngine.calculateSpectralDensity(logs)
        // diversityRatio = 5/10 = 0.5. volumeFactor = 5/100 = 0.05
        // expected = 0.5 * 0.7 + 0.05 * 0.3 = 0.35 + 0.015 = 0.365
        assertEquals(0.365f, density, 0.01f)
    }

    @Test
    fun testCalculateAgitation_RecentNegative() {
        val now = System.currentTimeMillis()
        val logs = listOf(
            BehaviorEvent(studentId = 1L, type = "Negative Behavior", timestamp = now - 1000, comment = null),
            BehaviorEvent(studentId = 1L, type = "Negative Behavior", timestamp = now - 2000, comment = null)
        )
        val agitation = GhostSpectraEngine.calculateAgitation(logs)
        // count = 2. expected = 2/10 = 0.2
        assertEquals(0.2f, agitation, 0.01f)
    }

    @Test
    fun testCalculateAgitation_OldNegative() {
        val now = System.currentTimeMillis()
        val oldTime = now - (48 * 60 * 60 * 1000L) // 48 hours ago
        val logs = listOf(
            BehaviorEvent(studentId = 1L, type = "Negative Behavior", timestamp = oldTime, comment = null)
        )
        val agitation = GhostSpectraEngine.calculateAgitation(logs)
        // Should be ignored as it's outside 24h window
        assertEquals(0.1f, agitation, 0.01f)
    }
}
