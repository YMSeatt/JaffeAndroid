package com.example.myapplication.labs.ghost.zenith

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.QuizLog
import org.junit.Assert.assertEquals
import org.junit.Test

class GhostZenithEngineTest {

    @Test
    fun testCalculateStudentAltitude_AcademicBuoyancy() {
        val quizLogs = listOf(
            QuizLog(studentId = 1, markValue = 10f, maxMarkValue = 10f),
            QuizLog(studentId = 1, markValue = 8f, maxMarkValue = 10f)
        )
        val behaviorLogs = emptyList<BehaviorEvent>()

        // Academic (0.9 * 0.7) + Behavior (0.5 * 0.3) = 0.63 + 0.15 = 0.78
        val altitude = GhostZenithEngine.calculateStudentAltitude(quizLogs, behaviorLogs)
        assertEquals(0.78f, altitude, 0.01f)
    }

    @Test
    fun testCalculateStudentAltitude_BehaviorStability() {
        val quizLogs = emptyList<QuizLog>()
        val behaviorLogs = listOf(
            BehaviorEvent(studentId = 1, type = "Positive Participation", timestamp = 0L),
            BehaviorEvent(studentId = 1, type = "Negative Disruption", timestamp = 1L)
        )

        // Academic (0.5 * 0.7) + Behavior (0.5 * 0.3) = 0.35 + 0.15 = 0.50
        val altitude = GhostZenithEngine.calculateStudentAltitude(quizLogs, behaviorLogs)
        assertEquals(0.50f, altitude, 0.01f)
    }

    @Test
    fun testCalculateParallaxOffset() {
        val tilt = 0.5f // radians
        val depth = 1.0f

        // 0.5 * 100 * 1.0 = 50.0
        val offset = GhostZenithEngine.calculateParallaxOffset(tilt, depth)
        assertEquals(50f, offset, 0.01f)
    }
}
