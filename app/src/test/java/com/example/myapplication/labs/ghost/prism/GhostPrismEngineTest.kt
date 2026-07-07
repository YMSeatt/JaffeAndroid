package com.example.myapplication.labs.ghost.prism

import com.example.myapplication.data.BehaviorEvent
import org.junit.Assert.*
import org.junit.Test

class GhostPrismEngineTest {

    @Test
    fun testCalculateVibe_ZenGarden() {
        val vibe = GhostPrismEngine.calculateVibe(1L, emptyList(), emptyList(), emptyList())
        assertEquals(GhostPrismEngine.Vibe.ZEN_GARDEN, vibe)
    }

    @Test
    fun testCalculateVibe_SolarFlare() {
        val now = System.currentTimeMillis()
        val bLogs = listOf(
            BehaviorEvent(1, 1L, "Positive", now - 1000, null),
            BehaviorEvent(2, 1L, "Positive", now - 2000, null)
        )
        val vibe = GhostPrismEngine.calculateVibe(1L, bLogs, emptyList(), emptyList(), now)
        assertEquals(GhostPrismEngine.Vibe.SOLAR_FLARE, vibe)
    }

    @Test
    fun testCalculateVibe_VoidRunner() {
        val bLogs = List(4) { i -> BehaviorEvent(i.toLong(), 1L, "Negative", 0L, null) }
        val vibe = GhostPrismEngine.calculateVibe(1L, bLogs, emptyList(), emptyList())
        assertEquals(GhostPrismEngine.Vibe.VOID_RUNNER, vibe)
    }

    @Test
    fun testCalculateVibe_NeonDream() {
        val bLogs = List(5) { i -> BehaviorEvent(i.toLong(), 1L, "Positive", 0L, null) }
        val qLogs = listOf(com.example.myapplication.data.QuizLog(1L, "Quiz", 95.0, 100.0, 0L))
        val vibe = GhostPrismEngine.calculateVibe(1L, bLogs, qLogs, emptyList())
        assertEquals(GhostPrismEngine.Vibe.NEON_DREAM, vibe)
    }

    @Test
    fun testCalculateVibe_CyberPunk() {
        val bLogs = List(8) { i -> BehaviorEvent(i.toLong(), 1L, "Positive", 0L, null) } +
                    List(3) { i -> BehaviorEvent((i + 8).toLong(), 1L, "Negative", 0L, null) }
        val vibe = GhostPrismEngine.calculateVibe(1L, bLogs, emptyList(), emptyList())
        assertEquals(GhostPrismEngine.Vibe.CYBER_PUNK, vibe)
    }
}
