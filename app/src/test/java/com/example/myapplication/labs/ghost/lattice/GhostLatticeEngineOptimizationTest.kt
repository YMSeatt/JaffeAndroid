package com.example.myapplication.labs.ghost.lattice

import org.junit.Assert.*
import org.junit.Test

class GhostLatticeEngineOptimizationTest {

    @Test
    fun testComputeLatticeHoistingParity() {
        val nodes = listOf(
            GhostLatticeEngine.LatticeNode(1L, 100f, 100f),
            GhostLatticeEngine.LatticeNode(2L, 200f, 100f)
        )
        val negativeCounts = mapOf(1L to 10, 2L to 0)

        // The optimized logic should still produce the same edges as the original O(N^2) map lookup
        val edges = GhostLatticeEngine.computeLattice(nodes, negativeCounts)

        // Distance is 100, which is < 800 threshold.
        // negA + negB = 10 + 0 = 10, which is > 5. Should be FRICTION.
        assertEquals(1, edges.size)
        assertEquals(GhostLatticeEngine.ConnectionType.FRICTION, edges[0].type)
        assertEquals(1L, edges[0].fromId)
        assertEquals(2L, edges[0].toId)
    }
}
