package com.example.myapplication.labs.ghost.mirage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import android.os.SystemClock

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GhostMirageEngineTest {
    private lateinit var engine: GhostMirageEngine

    @Before
    fun setup() {
        engine = GhostMirageEngine()
    }

    @Test
    fun `test record focus maps correctly`() {
        // Record focus at center (2000, 2000)
        engine.recordFocus(2000f, 2000f, 0.5f)

        val heatmap = engine.heatmap.value
        // Grid is 20x20, so (2000, 2000) is Row 10, Col 10
        val index = 10 * 20 + 10
        assertEquals(0.5f, heatmap[index], 0.01f)
    }

    @Test
    fun `test focus intensity caps at 1`() {
        engine.recordFocus(0f, 0f, 0.8f)
        engine.recordFocus(0f, 0f, 0.5f)

        val heatmap = engine.heatmap.value
        assertEquals(1.0f, heatmap[0], 0.01f)
    }

    @Test
    fun `test decay logic`() {
        engine.recordFocus(0f, 0f, 1.0f)

        // Simulate 10 seconds passing
        SystemClock.setCurrentTimeMillis(SystemClock.elapsedRealtime() + 10000)

        // Decay at 0.05 units/sec, so 0.5 units after 10s
        engine.update(decayRate = 0.05f)

        val heatmap = engine.heatmap.value
        assertEquals(0.5f, heatmap[0], 0.01f)
    }

    @Test
    fun `test clear`() {
        engine.recordFocus(100f, 100f, 1.0f)
        engine.clear()

        val heatmap = engine.heatmap.value
        heatmap.forEach { assertEquals(0f, it) }
    }
}
