package com.example.myapplication.labs.ghost.vortex

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Student
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostVortexEngineTest {

    @Test
    fun `identifyVortices should return empty list for empty students`() {
        val result = GhostVortexEngine.identifyVortices(emptyList(), emptyMap())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `identifyVortices should identify a vortex for high activity cluster`() {
        val currentTime = System.currentTimeMillis()
        val s1 = Student(id = 1, firstName = "A", lastName = "1", xPosition = 100f, yPosition = 100f)
        val s2 = Student(id = 2, firstName = "B", lastName = "2", xPosition = 150f, yPosition = 150f)

        val logs = listOf(
            BehaviorEvent(1, "Negative", currentTime, null),
            BehaviorEvent(1, "Negative", currentTime - 1000, null),
            BehaviorEvent(1, "Negative", currentTime - 2000, null),
            BehaviorEvent(1, "Negative", currentTime - 3000, null),
            BehaviorEvent(1, "Negative", currentTime - 4000, null),
            BehaviorEvent(2, "Negative", currentTime, null),
            BehaviorEvent(2, "Negative", currentTime - 1000, null),
            BehaviorEvent(2, "Negative", currentTime - 2000, null),
            BehaviorEvent(2, "Negative", currentTime - 3000, null),
            BehaviorEvent(2, "Negative", currentTime - 4000, null)
        )

        val logsByStudent = logs.groupBy { it.studentId }
        val result = GhostVortexEngine.identifyVortices(listOf(s1, s2), logsByStudent)

        assertEquals(1, result.size)
        assertEquals(100f, result[0].x)
        assertEquals(100f, result[0].y)
        assertEquals(-1.0f, result[0].polarity)
        assertTrue(result[0].momentum > 0.5f)
    }

    @Test
    fun `identifyVortices should ignore old logs`() {
        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - 600_000L
        val oldTime = startTime - 1000L

        val s1 = Student(id = 1, firstName = "A", lastName = "1", xPosition = 100f, yPosition = 100f)
        val s2 = Student(id = 2, firstName = "B", lastName = "2", xPosition = 150f, yPosition = 150f)

        val logs = listOf(
            BehaviorEvent(1, "Negative", oldTime, null),
            BehaviorEvent(2, "Negative", oldTime, null)
        )

        val logsByStudent = logs.groupBy { it.studentId }
        val result = GhostVortexEngine.identifyVortices(listOf(s1, s2), logsByStudent)

        assertTrue("Should not identify vortex with only old logs", result.isEmpty())
    }
}
