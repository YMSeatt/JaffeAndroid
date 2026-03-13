package com.example.myapplication.labs.ghost.cortex

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.QuizLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostCortexEngineTest {

    @Test
    fun testCalculateNeuralTension_ZeroInput() {
        val engine = GhostCortexEngine(org.mockito.Mockito.mock(android.content.Context::class.java))
        val tension = engine.calculateNeuralTension(emptyList(), emptyList())
        // Default academic entropy 0.2 (from avg 0.8) and 0 turbulence
        // sqrt((0.2^2 + 0^2)/2) = sqrt(0.04/2) = sqrt(0.02) ≈ 0.1414
        assertEquals(0.1414f, tension, 0.01f)
    }

    @Test
    fun testCalculateNeuralTension_CriticalState() {
        val engine = GhostCortexEngine(org.mockito.Mockito.mock(android.content.Context::class.java))

        val behaviorLogs = listOf(
            BehaviorEvent(1, "Negative Behavior", System.currentTimeMillis()),
            BehaviorEvent(1, "Negative Behavior", System.currentTimeMillis())
        )
        val quizLogs = listOf(
            QuizLog(1, 0.0, 10.0) // 0% score
        )

        val tension = engine.calculateNeuralTension(quizLogs, behaviorLogs)
        // Academic entropy: 1.0, Turbulence: 1.0
        // sqrt((1.0^2 + 1.0^2)/2) = 1.0
        assertEquals(1.0f, tension, 0.01f)
    }

    @Test
    fun testCalculateNeuralTension_ZenState() {
        val engine = GhostCortexEngine(org.mockito.Mockito.mock(android.content.Context::class.java))

        val behaviorLogs = listOf(
            BehaviorEvent(1, "Positive Participation", System.currentTimeMillis())
        )
        val quizLogs = listOf(
            QuizLog(1, 10.0, 10.0) // 100% score
        )

        val tension = engine.calculateNeuralTension(quizLogs, behaviorLogs)
        // Academic entropy: 0.0, Turbulence: 0.0
        assertEquals(0.0f, tension, 0.01f)
    }
}
