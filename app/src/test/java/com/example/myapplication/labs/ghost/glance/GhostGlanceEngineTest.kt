package com.example.myapplication.labs.ghost.glance

import com.example.myapplication.data.BehaviorEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class GhostGlanceEngineTest {

    @Test
    fun testSynthesizeQuiet() {
        val state = GhostGlanceEngine.synthesize(emptyList(), emptyList(), emptyList())
        assertEquals(GhostGlanceEngine.NeuralSignature.QUIET, state.signature)
        assertEquals(0f, state.momentum)
        assertEquals(1f, state.stability)
    }

    @Test
    fun testSynthesizeTurbulent() {
        val now = System.currentTimeMillis()
        val logs = listOf(
            BehaviorEvent(studentId = 1, type = "Negative - Disruption", timestamp = now),
            BehaviorEvent(studentId = 1, type = "Negative - Disrespect", timestamp = now - 1000),
            BehaviorEvent(studentId = 1, type = "Negative - Off Task", timestamp = now - 2000)
        )
        val state = GhostGlanceEngine.synthesize(logs, emptyList(), emptyList())
        assertEquals(GhostGlanceEngine.NeuralSignature.TURBULENT, state.signature)
        assertEquals(0f, state.stability)
    }

    @Test
    fun testSynthesizePeak() {
        val now = System.currentTimeMillis()
        val bLogs = List(4) { BehaviorEvent(studentId = 1, type = "Positive", timestamp = now) }
        val qLogs = List(4) { com.example.myapplication.data.QuizLog(studentId = 1, quizName = "Test", timestamp = now) }
        val state = GhostGlanceEngine.synthesize(bLogs, qLogs, emptyList())
        assertEquals(GhostGlanceEngine.NeuralSignature.PEAK, state.signature)
        assertEquals(1f, state.stability)
    }
}
