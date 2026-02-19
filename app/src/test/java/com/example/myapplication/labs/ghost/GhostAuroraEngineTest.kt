package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostAuroraEngineTest {

    @Test
    fun `test aurora params with no events`() {
        val params = GhostAuroraEngine.calculateAuroraParams(emptyList(), emptyList(), emptyList())

        println("No events params: $params")
        assertEquals(0.2f, params.intensity, 0.01f)
        assertEquals(0.5f + (0.2f * 1.5f), params.speed, 0.01f)
        // Neutral color should be deep blue
        assertEquals(0.0f, params.colorPrimary.first, 0.01f)
        assertEquals(0.5f, params.colorPrimary.second, 0.01f)
        assertEquals(0.8f, params.colorPrimary.third, 0.01f)
    }

    @Test
    fun `test aurora params with positive events`() {
        val now = System.currentTimeMillis()
        val events = listOf(
            BehaviorEvent(studentId = 1L, type = "Participation", timestamp = now, comment = null),
            BehaviorEvent(studentId = 1L, type = "Helpful", timestamp = now, comment = null)
        )

        val params = GhostAuroraEngine.calculateAuroraParams(events, emptyList(), emptyList())
        println("Positive events params: $params")

        assertTrue(params.intensity >= 0.2f)
        // Primary color should be Cyan (Positive)
        assertEquals(0.0f, params.colorPrimary.first, 0.01f)
        assertEquals(0.8f, params.colorPrimary.second, 0.01f)
        assertEquals(1.0f, params.colorPrimary.third, 0.01f)
    }

    @Test
    fun `test aurora params with negative events`() {
        val now = System.currentTimeMillis()
        val events = listOf(
            BehaviorEvent(studentId = 1L, type = "Disruptive Negative", timestamp = now, comment = null)
        )

        val params = GhostAuroraEngine.calculateAuroraParams(events, emptyList(), emptyList())

        // Primary color should be Red-ish (Negative)
        assertEquals(1.0f, params.colorPrimary.first)
        assertEquals(0.2f, params.colorPrimary.second)
        assertEquals(0.1f, params.colorPrimary.third)
    }

    @Test
    fun `test intensity scaling`() {
        val now = System.currentTimeMillis()
        val manyEvents = (1L..25L).map {
            BehaviorEvent(studentId = it, type = "Test", timestamp = now, comment = null)
        }

        val params = GhostAuroraEngine.calculateAuroraParams(manyEvents, emptyList(), emptyList())

        assertEquals(1.0f, params.intensity)
    }
}
