package com.example.myapplication.labs.ghost.kinetic

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostKineticEngineTest {

    @Test
    fun testTargetOffsetCalculation() {
        val initialPos = Offset(100f, 100f)
        val initialVelocity = Offset(200f, 0f)

        val target = GhostKineticEngine.calculateTargetOffset(initialVelocity, initialPos)

        // targetX = 100 + (200 / 1.1) = 100 + 181.818 = 281.818
        assertEquals(281.818f, target.x, 0.01f)
        assertEquals(100f, target.y, 0.01f)
    }

    @Test
    fun testVelocityThreshold() {
        assertTrue(GhostKineticEngine.VELOCITY_THRESHOLD >= 100f)
    }
}
