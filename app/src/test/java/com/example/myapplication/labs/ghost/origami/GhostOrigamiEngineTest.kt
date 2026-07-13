package com.example.myapplication.labs.ghost.origami

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * GhostOrigamiEngineTest: Unit tests for the Neural Origami folding logic.
 */
class GhostOrigamiEngineTest {

    @Test
    fun testInitialState() {
        val engine = GhostOrigamiEngine()
        assertEquals(0f, engine.foldProgress.value, 0.001f)
    }

    @Test
    fun testToggleFold() {
        val engine = GhostOrigamiEngine()

        // Toggle to folded (initial is 0.0, so should go to 1.0)
        engine.toggleFold()
        assertEquals(1f, engine.foldProgress.value, 0.001f)

        // Toggle back to flat (current is 1.0, so should go to 0.0)
        engine.toggleFold()
        assertEquals(0f, engine.foldProgress.value, 0.001f)
    }

    @Test
    fun testSetFoldProgress() {
        val engine = GhostOrigamiEngine()

        engine.setFoldProgress(0.75f)
        assertEquals(0.75f, engine.foldProgress.value, 0.001f)

        // Clamping tests: Above 1.0
        engine.setFoldProgress(1.5f)
        assertEquals(1f, engine.foldProgress.value, 0.001f)

        // Clamping tests: Below 0.0
        engine.setFoldProgress(-0.5f)
        assertEquals(0f, engine.foldProgress.value, 0.001f)
    }

    @Test
    fun testToggleFromPartialFold() {
        val engine = GhostOrigamiEngine()

        // If progress > 0.5f, toggle should reset to 0f (flat)
        engine.setFoldProgress(0.6f)
        engine.toggleFold()
        assertEquals(0f, engine.foldProgress.value, 0.001f)

        // If progress <= 0.5f, toggle should set to 1f (folded)
        engine.setFoldProgress(0.4f)
        engine.toggleFold()
        assertEquals(1f, engine.foldProgress.value, 0.001f)

        // Boundary case: Exactly 0.5f should toggle to 1f
        engine.setFoldProgress(0.5f)
        engine.toggleFold()
        assertEquals(1f, engine.foldProgress.value, 0.001f)
    }

    @Test
    fun testReset() {
        val engine = GhostOrigamiEngine()
        engine.setFoldProgress(0.8f)
        engine.reset()
        assertEquals(0f, engine.foldProgress.value, 0.001f)
    }
}
