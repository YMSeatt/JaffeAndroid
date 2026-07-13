package com.example.myapplication.labs.ghost.flora

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostFloraEngineTest {

    @Test
    fun testCalculateFloraState_DefaultValues() {
        val state = GhostFloraEngine.calculateFloraState(1L, emptyList(), emptyList(), emptyList())

        // Growth defaults to (0.7 + 0.8) / 2 = 0.75
        assertEquals(0.75f, state.growth, 0.01f)
        // Vitality defaults to 0.9
        assertEquals(0.9f, state.vitality, 0.01f)
        // Complexity defaults to 0.1 (0 logs)
        assertEquals(0.1f, state.complexity, 0.01f)
    }

    @Test
    fun testCalculateFloraState_AcademicGrowth() {
        val quizLogs = listOf(
            QuizLog(studentId = 1L, markValue = 10.0, maxMarkValue = 10.0), // 1.0
            QuizLog(studentId = 1L, markValue = 8.0, maxMarkValue = 10.0)   // 0.8 -> Avg 0.9
        )
        val homeworkLogs = listOf(
            HomeworkLog(studentId = 1L, status = "Done"),
            HomeworkLog(studentId = 1L, status = "Missing") // 0.5 -> Avg (0.9 + 0.5) / 2 = 0.7
        )

        val state = GhostFloraEngine.calculateFloraState(1L, emptyList(), quizLogs, homeworkLogs)
        assertEquals(0.7f, state.growth, 0.01f)
    }

    @Test
    fun testCalculateFloraState_BehavioralVitality() {
        val behaviorLogs = listOf(
            BehaviorEvent(studentId = 1L, type = "Participation", timestamp = 0L),
            BehaviorEvent(studentId = 1L, type = "Negative Outburst", timestamp = 0L) // 1 pos, 1 neg -> 0.5
        )

        val state = GhostFloraEngine.calculateFloraState(1L, behaviorLogs, emptyList(), emptyList())
        assertEquals(0.5f, state.vitality, 0.01f)
    }

    @Test
    fun testCalculateFloraState_ComplexityClamping() {
        val manyLogs = List(20) {
            BehaviorEvent(studentId = 1L, type = "Log", timestamp = 0L)
        }

        val state = GhostFloraEngine.calculateFloraState(1L, manyLogs, emptyList(), emptyList())
        // 20 logs / 10 = 2.0, clamped to 1.0
        assertEquals(1.0f, state.complexity, 0.01f)
    }
}
