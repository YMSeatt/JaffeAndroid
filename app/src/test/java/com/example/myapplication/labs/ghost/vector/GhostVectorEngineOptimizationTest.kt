package com.example.myapplication.labs.ghost.vector

import com.example.myapplication.labs.ghost.lattice.GhostLatticeEngine
import org.junit.Assert.*
import org.junit.Test
import androidx.compose.ui.graphics.Color

class GhostVectorEngineOptimizationTest {

    @Test
    fun testCalculateVectorsParity() {
        val nodes = listOf(
            GhostLatticeEngine.LatticeNode(1L, 100f, 100f),
            GhostLatticeEngine.LatticeNode(2L, 200f, 100f),
            GhostLatticeEngine.LatticeNode(3L, 150f, 200f)
        )
        val edges = listOf(
            GhostLatticeEngine.Edge(1L, 2L, 0.8f, GhostLatticeEngine.ConnectionType.COLLABORATION, Color.Green),
            GhostLatticeEngine.Edge(2L, 3L, 0.5f, GhostLatticeEngine.ConnectionType.FRICTION, Color.Red),
            GhostLatticeEngine.Edge(1L, 3L, 0.3f, GhostLatticeEngine.ConnectionType.NEUTRAL, Color.Blue)
        )

        val vectors = GhostVectorEngine.calculateVectors(nodes, edges)

        assertEquals(3, vectors.size)

        // Node 1 forces:
        // From 2: (100, 0) normalized * (0.8 * 60) = (48, 0)
        // From 3: (50, 100) / 111.8 * (0.3 * 15) = (0.447, 0.894) * 4.5 = (2.01, 4.02)
        // Net Node 1: (50.01, 4.02), Mag: 50.17
        val v1 = vectors.find { it.studentId == 1L }!!
        assertEquals(50.17f, v1.magnitude, 0.1f)

        // Node 2 forces:
        // From 1: (-100, 0) normalized * 48 = (-48, 0)
        // From 3: (-50, 100) / 111.8 * (0.5 * -100) = (-0.447, 0.894) * -50 = (22.36, -44.72)
        // Net Node 2: (-25.64, -44.72), Mag: 51.57
        val v2 = vectors.find { it.studentId == 2L }!!
        assertEquals(51.57f, v2.magnitude, 0.1f)

        // Node 3 forces:
        // From 1: (-50, -100) / 111.8 * 4.5 = (-2.01, -4.02)
        // From 2: (50, -100) / 111.8 * -50 = (-22.36, 44.72)
        // Net Node 3: (-24.37, 40.7), Mag: 47.45
        val v3 = vectors.find { it.studentId == 3L }!!
        assertEquals(47.45f, v3.magnitude, 0.1f)
    }
}
