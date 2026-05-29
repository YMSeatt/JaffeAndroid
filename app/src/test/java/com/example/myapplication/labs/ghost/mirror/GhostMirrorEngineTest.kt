package com.example.myapplication.labs.ghost.mirror

import org.junit.Assert.assertEquals
import org.junit.Test

class GhostMirrorEngineTest {

    @Test
    fun testTogglePerspective() {
        val engine = GhostMirrorEngine()

        assertEquals(GhostMirrorEngine.Perspective.NORMAL, engine.perspective.value)

        engine.togglePerspective()
        assertEquals(GhostMirrorEngine.Perspective.FLIPPED, engine.perspective.value)
        assertEquals(180f, engine.perspective.value.rotationZ)
        assertEquals(1f, engine.perspective.value.scaleX)

        engine.togglePerspective()
        assertEquals(GhostMirrorEngine.Perspective.MIRROR, engine.perspective.value)
        assertEquals(0f, engine.perspective.value.rotationZ)
        assertEquals(-1f, engine.perspective.value.scaleX)

        engine.togglePerspective()
        assertEquals(GhostMirrorEngine.Perspective.FLIPPED_MIRROR, engine.perspective.value)
        assertEquals(180f, engine.perspective.value.rotationZ)
        assertEquals(-1f, engine.perspective.value.scaleX)

        engine.togglePerspective()
        assertEquals(GhostMirrorEngine.Perspective.NORMAL, engine.perspective.value)
    }

    @Test
    fun testReset() {
        val engine = GhostMirrorEngine()
        engine.togglePerspective()
        engine.reset()
        assertEquals(GhostMirrorEngine.Perspective.NORMAL, engine.perspective.value)
    }
}
