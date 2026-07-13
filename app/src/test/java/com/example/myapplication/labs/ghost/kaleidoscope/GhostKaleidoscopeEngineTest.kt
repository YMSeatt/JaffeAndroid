package com.example.myapplication.labs.ghost.kaleidoscope

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Student
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostKaleidoscopeEngineTest {

    @Test
    fun `synthesizeFragments should create fragments from recent logs`() {
        val now = System.currentTimeMillis()
        val studentId = 1L

        val student = Student(
            id = studentId,
            firstName = "John",
            lastName = "Doe",
            xPosition = 1000f,
            yPosition = 2000f,
            gender = "M"
        )

        val logs = listOf(
            BehaviorEvent(studentId = studentId, type = "Positive Participation", timestamp = now - 1000),
            BehaviorEvent(studentId = studentId, type = "Disruptive Behavior (Negative)", timestamp = now - 5000)
        )

        val behaviorLogsByStudent = mapOf(studentId to logs)
        val students = listOf(student)

        val fragments = GhostKaleidoscopeEngine.synthesizeFragments(students, behaviorLogsByStudent, now)

        assertEquals(1, fragments.size)
        assertEquals(1000f, fragments[0].x)
        assertEquals(2000f, fragments[0].y)
        assertEquals(0f, fragments[0].polarity) // (1 pos - 1 neg) / 2 = 0
        assertTrue(fragments[0].intensity > 0f)
    }

    @Test
    fun `calculateHarmony should return high value for positive balance`() {
        val fragments = listOf(
            GhostKaleidoscopeEngine.NeuralFragment(0f, 0f, 1.0f, 1.0f),
            GhostKaleidoscopeEngine.NeuralFragment(10f, 10f, 0.8f, 0.5f)
        )

        val harmony = GhostKaleidoscopeEngine.calculateHarmony(fragments)
        // avg polarity = 0.9. harmony = 0.9 * 0.5 + 0.5 = 0.95
        assertTrue(harmony > 0.9f)
    }

    @Test
    fun `calculateHarmony should return low value for negative balance`() {
        val fragments = listOf(
            GhostKaleidoscopeEngine.NeuralFragment(0f, 0f, -1.0f, 1.0f),
            GhostKaleidoscopeEngine.NeuralFragment(10f, 10f, -0.6f, 0.5f)
        )

        val harmony = GhostKaleidoscopeEngine.calculateHarmony(fragments)
        // avg polarity = -0.8. harmony = -0.8 * 0.5 + 0.5 = 0.1
        assertTrue(harmony < 0.2f)
    }
}
