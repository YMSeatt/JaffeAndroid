package com.example.myapplication.labs.ghost.phasing

import android.content.Context
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GhostPhasingEngineTest {

    @Test
    fun testPhaseUpdate() {
        val context = mockk<Context>(relaxed = true)
        val engine = GhostPhasingEngine(context)

        // Initial state
        assertEquals(0f, engine.phaseLevel.value)
        assertTrue(!engine.isPhased.value)

        // Transition to active
        engine.updatePhase(0.6f)
        assertEquals(0.6f, engine.phaseLevel.value)
        assertTrue(engine.isPhased.value)

        // Fully active
        engine.updatePhase(1.0f)
        assertEquals(1.0f, engine.phaseLevel.value)
        assertTrue(engine.isPhased.value)

        // Transition back to inactive
        engine.updatePhase(0.4f)
        assertEquals(0.4f, engine.phaseLevel.value)
        assertTrue(!engine.isPhased.value)
    }

    @Test
    fun testPhaseClamping() {
        val context = mockk<Context>(relaxed = true)
        val engine = GhostPhasingEngine(context)

        engine.updatePhase(1.5f)
        assertEquals(1.0f, engine.phaseLevel.value)

        engine.updatePhase(-0.5f)
        assertEquals(0f, engine.phaseLevel.value)
    }
}
