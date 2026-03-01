package com.example.myapplication.labs.ghost.entanglement

import org.junit.Assert.*
import org.junit.Test

class GhostEntanglementEngineTest {

    @Test
    fun testCalculateCoherence() {
        val nodeA = GhostEntanglementEngine.EntangledNode(1L, 100f, 100f, 0.8f, 0.9f)
        val nodeB = GhostEntanglementEngine.EntangledNode(2L, 110f, 110f, 0.7f, 0.8f)

        val coherence = GhostEntanglementEngine.calculateCoherence(nodeA, nodeB, sharingGroup = true)

        // With high proximity, high sync, and group multiplier, coherence should be high
        assertTrue("Coherence should be high for close students in same group", coherence > 0.8f)
    }

    @Test
    fun testAnalyzeEntanglement() {
        val nodes = listOf(
            GhostEntanglementEngine.EntangledNode(1L, 100f, 100f, 0.9f, 0.9f),
            GhostEntanglementEngine.EntangledNode(2L, 110f, 110f, 0.8f, 0.8f),
            GhostEntanglementEngine.EntangledNode(3L, 3000f, 3000f, 0.1f, 0.1f)
        )
        val groupMap = mapOf(1L to 10L, 2L to 10L, 3L to 20L)

        val analysis = GhostEntanglementEngine.analyzeEntanglement(nodes, groupMap)

        assertEquals(1, analysis.activeLinks) // Only node 1 and 2 should be entangled
        assertTrue(analysis.maxCoherence > 0.8f)
    }

    @Test
    fun testGenerateReport() {
        val analysis = GhostEntanglementEngine.EntanglementAnalysis(
            state = GhostEntanglementEngine.CoherenceState.ENTANGLED,
            avgCoherence = 0.95f,
            maxCoherence = 0.99f,
            activeLinks = 5
        )
        val report = GhostEntanglementEngine.generateEntanglementReport(analysis, "2027-01-01 12:00:00")

        assertTrue(report.contains("# 👻 GHOST ENTANGLEMENT"))
        assertTrue(report.contains("ENTANGLED"))
        assertTrue(report.contains("95.0%"))
        assertTrue(report.contains("2027-01-01 12:00:00"))
    }
}
