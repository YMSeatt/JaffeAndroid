package com.example.myapplication.labs.ghost.pip

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import com.example.myapplication.labs.ghost.GhostAuroraEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * GhostPipLogicTest: Verifies the core logic used by the PiP monitor.
 */
class GhostPipLogicTest {

    @Test
    fun testAuroraCalculationParity() {
        // Mock data
        val now = System.currentTimeMillis()
        val behaviorLogs = listOf(
            BehaviorEvent(1, 1, "Positive Participation", now - 1000)
        )
        val quizLogs = emptyList<QuizLog>()
        val homeworkLogs = emptyList<HomeworkLog>()

        // Simulate GhostPipActivity's data flow
        val params = GhostAuroraEngine.calculateAuroraParams(behaviorLogs, quizLogs, homeworkLogs)

        // Verify intensity and colors
        // With 1 positive log, intensity should be above minimum 0.2
        assertTrue(params.intensity > 0.2f)

        // Color should be primarily cyan (COLOR_POSITIVE is 0.0, 0.8, 1.0)
        assertTrue(params.colorPrimary.second > 0.5f)
        assertTrue(params.colorPrimary.third > 0.5f)
    }

    @Test
    fun testEmptyDataState() {
        val params = GhostAuroraEngine.calculateAuroraParams(emptyList(), emptyList(), emptyList())

        // Neutral state (Deep Blue: 0.0, 0.5, 0.8)
        assertEquals(0.0f, params.colorPrimary.first)
        assertEquals(0.5f, params.colorPrimary.second)
        assertEquals(0.8f, params.colorPrimary.third)
    }
}
