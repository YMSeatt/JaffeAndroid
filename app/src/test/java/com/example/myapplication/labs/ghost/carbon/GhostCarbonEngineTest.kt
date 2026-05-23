package com.example.myapplication.labs.ghost.carbon

import com.example.myapplication.data.BehaviorEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostCarbonEngineTest {

    @Test
    fun testIdentifyTwins() {
        val studentA = 1L
        val studentB = 2L
        val studentC = 3L

        val logs = mapOf(
            studentA to listOf(
                BehaviorEvent(studentId = studentA, type = "Talking", timestamp = 1000, comment = null),
                BehaviorEvent(studentId = studentA, type = "Helping", timestamp = 2000, comment = null),
                BehaviorEvent(studentId = studentA, type = "Talking", timestamp = 3000, comment = null)
            ),
            studentB to listOf(
                BehaviorEvent(studentId = studentB, type = "Talking", timestamp = 4000, comment = null),
                BehaviorEvent(studentId = studentB, type = "Helping", timestamp = 5000, comment = null),
                BehaviorEvent(studentId = studentB, type = "Talking", timestamp = 6000, comment = null)
            ),
            studentC to listOf(
                BehaviorEvent(studentId = studentC, type = "Great Participation", timestamp = 7000, comment = null),
                BehaviorEvent(studentId = studentC, type = "Great Participation", timestamp = 8000, comment = null),
                BehaviorEvent(studentId = studentC, type = "Great Participation", timestamp = 9000, comment = null)
            )
        )

        val twins = GhostCarbonEngine.identifyTwins(logs)

        // A and B should be twins (identical proportions)
        assertEquals(1, twins.size)
        val twin = twins[0]
        assertTrue((twin.studentA == studentA && twin.studentB == studentB) || (twin.studentA == studentB && twin.studentB == studentA))
        assertTrue(twin.similarity > 0.99f)
    }

    @Test
    fun testInsufficientData() {
        val studentA = 1L
        val studentB = 2L

        val logs = mapOf(
            studentA to listOf(
                BehaviorEvent(studentId = studentA, type = "Talking", timestamp = 1000, comment = null)
            ),
            studentB to listOf(
                BehaviorEvent(studentId = studentB, type = "Talking", timestamp = 2000, comment = null)
            )
        )

        val twins = GhostCarbonEngine.identifyTwins(logs)
        assertTrue(twins.isEmpty())
    }
}
