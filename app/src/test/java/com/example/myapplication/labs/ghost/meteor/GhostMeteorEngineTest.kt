package com.example.myapplication.labs.ghost.meteor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostMeteorEngineTest {

    @Test
    fun `test meteor emission`() {
        val engine = GhostMeteorEngine()
        engine.emit(0f, 0f, 100f, 100f, 0)

        assertEquals(1, engine.meteors.size)
        val meteor = engine.meteors[0]
        assertEquals(0f, meteor.x)
        assertEquals(0f, meteor.y)
        assertTrue(meteor.vx > 0)
        assertTrue(meteor.vy > 0)
        assertEquals(0, meteor.colorType)
    }

    @Test
    fun `test meteor update and life decay`() {
        val engine = GhostMeteorEngine()
        engine.emit(0f, 0f, 1000f, 1000f, 1)

        val initialX = engine.meteors[0].x
        val initialLife = engine.meteors[0].life

        engine.update(emptyList(), deltaTime = 10f)

        assertTrue(engine.meteors[0].x > initialX)
        assertTrue(engine.meteors[0].life < initialLife)
    }

    @Test
    fun `test meteor impact creation`() {
        val engine = GhostMeteorEngine()
        // Emit a meteor that will expire immediately
        engine.emit(0f, 0f, 10f, 10f, 2)
        engine.meteors[0].life = 0.001f

        engine.update(emptyList(), deltaTime = 1.0f)

        assertEquals(0, engine.meteors.size)
        assertEquals(1, engine.impacts.size)
        assertEquals(2, engine.impacts[0].colorType)
    }

    @Test
    fun `test impact update and expiry`() {
        val engine = GhostMeteorEngine()
        engine.emit(0f, 0f, 10f, 10f, 0)
        engine.meteors[0].life = 0.001f
        engine.update(emptyList()) // Create impact

        val initialRadius = engine.impacts[0].radius
        engine.update(emptyList(), deltaTime = 100f) // Expire impact

        assertEquals(0, engine.impacts.size)
    }

    @Test
    fun `test meteor pooling`() {
        val engine = GhostMeteorEngine()
        // Fill and empty pool
        repeat(GhostMeteorEngine.MAX_METEORS) {
            engine.emit(0f, 0f, 100f, 100f, 0)
        }
        assertEquals(GhostMeteorEngine.MAX_METEORS, engine.meteors.size)

        // Force all to expire
        engine.meteors.forEach { it.life = 0.0001f }
        engine.update(emptyList())
        assertEquals(0, engine.meteors.size)

        // Re-emit
        engine.emit(0f, 0f, 100f, 100f, 0)
        assertEquals(1, engine.meteors.size)
    }
}
