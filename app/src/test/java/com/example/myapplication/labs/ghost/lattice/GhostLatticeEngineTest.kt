package com.example.myapplication.labs.ghost.lattice

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostLatticeEngineTest {

    private val engine = GhostLatticeEngine()

    @Test
    fun testFindSocialClusters_EmptyInput() {
        val clusters = engine.findSocialClusters(emptyList(), emptyList())
        assertTrue(clusters.isEmpty())
    }

    @Test
    fun testFindSocialClusters_SingleNode() {
        val nodes = listOf(GhostLatticeEngine.LatticeNode(1, 100f, 100f))
        val clusters = engine.findSocialClusters(nodes, emptyList())
        assertEquals(1, clusters.size)
        assertEquals(setOf(1L), clusters[0].studentIds)
        assertEquals(0f, clusters[0].avgStrength)
    }

    @Test
    fun testFindSocialClusters_DisconnectedNodes() {
        val nodes = listOf(
            GhostLatticeEngine.LatticeNode(1, 100f, 100f),
            GhostLatticeEngine.LatticeNode(2, 2000f, 2000f)
        )
        val clusters = engine.findSocialClusters(nodes, emptyList())
        assertEquals(2, clusters.size)
        assertTrue(clusters.any { it.studentIds == setOf(1L) })
        assertTrue(clusters.any { it.studentIds == setOf(2L) })
    }

    @Test
    fun testFindSocialClusters_ConnectedComponent() {
        val nodes = listOf(
            GhostLatticeEngine.LatticeNode(1, 100f, 100f),
            GhostLatticeEngine.LatticeNode(2, 150f, 150f),
            GhostLatticeEngine.LatticeNode(3, 200f, 200f)
        )
        val edges = listOf(
            GhostLatticeEngine.Edge(1, 2, 0.8f, GhostLatticeEngine.ConnectionType.COLLABORATION, Color.Green),
            GhostLatticeEngine.Edge(2, 3, 0.6f, GhostLatticeEngine.ConnectionType.NEUTRAL, Color.Blue)
        )

        val clusters = engine.findSocialClusters(nodes, edges)
        assertEquals(1, clusters.size)
        assertEquals(setOf(1L, 2L, 3L), clusters[0].studentIds)
        assertEquals(0.7f, clusters[0].avgStrength, 0.001f)
    }

    @Test
    fun testFindSocialClusters_MultipleClusters() {
        val nodes = listOf(
            GhostLatticeEngine.LatticeNode(1, 100f, 100f),
            GhostLatticeEngine.LatticeNode(2, 150f, 150f),
            GhostLatticeEngine.LatticeNode(3, 1000f, 1000f),
            GhostLatticeEngine.LatticeNode(4, 1050f, 1050f)
        )
        val edges = listOf(
            GhostLatticeEngine.Edge(1, 2, 0.9f, GhostLatticeEngine.ConnectionType.COLLABORATION, Color.Green),
            GhostLatticeEngine.Edge(3, 4, 0.5f, GhostLatticeEngine.ConnectionType.NEUTRAL, Color.Blue)
        )

        val clusters = engine.findSocialClusters(nodes, edges)
        assertEquals(2, clusters.size)
        assertTrue(clusters.any { it.studentIds == setOf(1L, 2L) && it.avgStrength == 0.9f })
        assertTrue(clusters.any { it.studentIds == setOf(3L, 4L) && it.avgStrength == 0.5f })
    }
}
