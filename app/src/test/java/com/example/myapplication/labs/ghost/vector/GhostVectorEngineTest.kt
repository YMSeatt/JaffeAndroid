package com.example.myapplication.labs.ghost.vector

import com.example.myapplication.labs.ghost.lattice.GhostLatticeEngine
import org.junit.Assert.*
import org.junit.Test
import androidx.compose.ui.graphics.Color

class GhostVectorEngineTest {

    private val engine = GhostVectorEngine()

    @Test
    fun `test student status categorization`() {
        val nodes = listOf(
            GhostLatticeEngine.LatticeNode(1L, 100f, 100f),
            GhostLatticeEngine.LatticeNode(2L, 200f, 100f)
        )

        // High Turbulence (> 85)
        val edgesTurbulence = listOf(
            GhostLatticeEngine.Edge(1L, 2L, 1.0f, GhostLatticeEngine.ConnectionType.FRICTION, Color.Red)
        )
        val vectorsTurbulence = engine.calculateVectors(nodes, edgesTurbulence)
        assertEquals(GhostVectorEngine.SocialStatus.HIGH_TURBULENCE, vectorsTurbulence.find { it.studentId == 1L }?.status)
        assertEquals(100f, vectorsTurbulence[0].magnitude, 0.01f)

        // Active Synergy (> 40)
        val edgesSynergy = listOf(
            GhostLatticeEngine.Edge(1L, 2L, 0.8f, GhostLatticeEngine.ConnectionType.COLLABORATION, Color.Green)
        )
        val vectorsSynergy = engine.calculateVectors(nodes, edgesSynergy)
        assertEquals(GhostVectorEngine.SocialStatus.ACTIVE_SYNERGY, vectorsSynergy.find { it.studentId == 1L }?.status)
        assertEquals(48f, vectorsSynergy[0].magnitude, 0.01f)

        // Nominal (between 5 and 40)
        val edgesNominal = listOf(
            GhostLatticeEngine.Edge(1L, 2L, 1.0f, GhostLatticeEngine.ConnectionType.NEUTRAL, Color.Blue)
        )
        val vectorsNominal = engine.calculateVectors(nodes, edgesNominal)
        assertEquals(GhostVectorEngine.SocialStatus.NOMINAL, vectorsNominal.find { it.studentId == 1L }?.status)
        assertEquals(15f, vectorsNominal[0].magnitude, 0.01f)

        // Isolated (< 5)
        val vectorsIsolated = engine.calculateVectors(nodes, emptyList())
        assertEquals(GhostVectorEngine.SocialStatus.ISOLATED, vectorsIsolated.find { it.studentId == 1L }?.status)
        assertEquals(0f, vectorsIsolated[0].magnitude, 0.01f)
    }

    @Test
    fun `test classroom cohesion index`() {
        val vectors = listOf(
            createMockVector(1L, 100f),
            createMockVector(2L, 20f)
        )
        val analysis = engine.analyzeClassroomCohesion(vectors)
        assertEquals(60f, analysis.cohesionIndex, 0.01f)
        assertEquals("DYNAMIC", analysis.globalStatus)

        val vectorsStable = listOf(
            createMockVector(1L, 40f),
            createMockVector(2L, 20f)
        )
        val analysisStable = engine.analyzeClassroomCohesion(vectorsStable)
        assertEquals(30f, analysisStable.cohesionIndex, 0.01f)
        assertEquals("STABLE", analysisStable.globalStatus)
    }

    @Test
    fun `test report generation`() {
        val vectors = listOf(
            createMockVector(1L, 90f, GhostVectorEngine.SocialStatus.HIGH_TURBULENCE)
        )
        val analysis = GhostVectorEngine.SocialAnalysis(90f, "DYNAMIC")
        val names = mapOf(1L to "Alpha Student")

        val report = engine.generateSocialReport(analysis, vectors, names)
        println("Report output:\n$report")

        assertTrue(report.contains("# 👻 GHOST VECTOR: SOCIAL COHESION ANALYSIS"))
        assertTrue(report.contains("Classroom Cohesion Index:** 90.00"))
        assertTrue(report.contains("Global Status:** DYNAMIC"))
        assertTrue(report.contains("| Alpha Student | 90.00 | HIGH TURBULENCE |"))
    }

    private fun createMockVector(
        id: Long,
        mag: Float,
        status: GhostVectorEngine.SocialStatus = GhostVectorEngine.SocialStatus.NOMINAL
    ) = GhostVectorEngine.SocialVector(id, 0f, 0f, mag, 0f, status)
}
