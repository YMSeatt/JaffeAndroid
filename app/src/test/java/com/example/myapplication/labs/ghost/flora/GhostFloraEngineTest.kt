package com.example.myapplication.labs.ghost.flora

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.QuizLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostFloraEngineTest {

    @Test
    fun `calculateFloraState with positive behavior should increase growth and vitality`() {
        val studentId = 1L
        val behaviorLogs = listOf(
            BehaviorEvent(1, 1, "Positive Participation", 1000L, null),
            BehaviorEvent(2, 1, "Positive Participation", 2000L, null)
        )
        val quizLogs = emptyList<QuizLog>()

        val state = GhostFloraEngine.calculateFloraState(studentId, behaviorLogs, quizLogs)

        // 2 positive logs / 10 = 0.2 growth
        assertEquals(0.2f, state.growth, 0.01f)
        assertEquals(1.0f, state.vitality, 0.01f) // 2 pos, 0 neg
        assertEquals(1.0f, state.colorShift, 0.01f) // vitality 1.0 -> colorShift 1.0 (Cyan)
    }

    @Test
    fun `calculateFloraState with negative behavior should decrease vitality`() {
        val studentId = 1L
        val behaviorLogs = listOf(
            BehaviorEvent(1, 1, "Negative behavior", 1000L, null),
            BehaviorEvent(2, 1, "Negative behavior", 2000L, null)
        )
        val quizLogs = emptyList<QuizLog>()

        val state = GhostFloraEngine.calculateFloraState(studentId, behaviorLogs, quizLogs)

        assertEquals(0.1f, state.growth, 0.01f) // Min growth is 0.1
        assertEquals(-1.0f, state.vitality, 0.01f) // 0 pos, 2 neg
        assertEquals(0.0f, state.colorShift, 0.01f) // vitality -1.0 -> colorShift 0.0 (Magenta)
    }

    @Test
    fun `calculateFloraState with high quiz scores should increase complexity`() {
        val studentId = 1L
        val behaviorLogs = emptyList<BehaviorEvent>()
        val quizLogs = listOf(
            QuizLog(1, 1, 1, 10.0, 10.0, 1000L) // 100%
        )

        val state = GhostFloraEngine.calculateFloraState(studentId, behaviorLogs, quizLogs)

        assertEquals(1.0f, state.complexity, 0.01f)
    }

    @Test
    fun `calculateFloraState should have deterministic petal count based on ID`() {
        val state1 = GhostFloraEngine.calculateFloraState(1L, emptyList(), emptyList())
        val state2 = GhostFloraEngine.calculateFloraState(1L, emptyList(), emptyList())
        val state3 = GhostFloraEngine.calculateFloraState(2L, emptyList(), emptyList())

        assertEquals(state1.petalCount, state2.petalCount)
        assertTrue(state1.petalCount != state3.petalCount)
    }
}
