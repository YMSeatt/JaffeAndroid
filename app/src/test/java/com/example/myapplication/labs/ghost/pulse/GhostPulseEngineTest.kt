package com.example.myapplication.labs.ghost.pulse

import com.example.myapplication.data.BehaviorEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostPulseEngineTest {

    @Test
    fun testCalculateResonance_EmptyEvents() {
        val pulses = GhostPulseEngine.calculateResonance(emptyList(), System.currentTimeMillis())
        assertTrue(pulses.isEmpty())
    }

    @Test
    fun testCalculateResonance_SingleRecentEvent() {
        val currentTime = System.currentTimeMillis()
        val events = listOf(
            BehaviorEvent(id = 1, studentId = 1, type = "Positive", timestamp = currentTime - 1000, comment = null)
        )

        val pulses = GhostPulseEngine.calculateResonance(events, currentTime)

        assertEquals(1, pulses.size)
        assertEquals(1L, pulses[0].studentId)
        assertTrue(pulses[0].intensity > 0.7f)
        assertEquals(0.2f, pulses[0].r, 0.01f) // Green
        assertEquals(1.0f, pulses[0].g, 0.01f)
        assertEquals(0.4f, pulses[0].b, 0.01f)
    }

    @Test
    fun testCalculateResonance_MultipleEvents() {
        val currentTime = System.currentTimeMillis()
        val events = listOf(
            BehaviorEvent(id = 1, studentId = 1, type = "Positive", timestamp = currentTime - 500, comment = null),
            BehaviorEvent(id = 2, studentId = 2, type = "Negative", timestamp = currentTime - 2000, comment = null)
        )

        val pulses = GhostPulseEngine.calculateResonance(events, currentTime)

        assertEquals(2, pulses.size)
        val pulse1 = pulses.find { it.studentId == 1L }!!
        val pulse2 = pulses.find { it.studentId == 2L }!!

        assertTrue(pulse1.intensity > pulse2.intensity)
        assertEquals(1.0f, pulse2.r, 0.01f) // Red
    }

    @Test
    fun testCalculateResonance_OutsideWindow() {
        val currentTime = System.currentTimeMillis()
        val events = listOf(
            BehaviorEvent(id = 1, studentId = 1, type = "Positive", timestamp = currentTime - 10000, comment = null)
        )

        val pulses = GhostPulseEngine.calculateResonance(events, currentTime)
        assertTrue(pulses.isEmpty())
    }
}
