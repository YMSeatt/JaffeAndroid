package com.example.myapplication.labs.ghost.osmosis

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostOsmosisEngineTest {

    @Test
    fun testCalculateStudentPotentials_EmptyLogs() {
        val (k, b) = GhostOsmosisEngine.calculateStudentPotentials(emptyList(), emptyList(), emptyList())
        assertEquals(0.5f, k, 0.01f)
        assertEquals(0.0f, b, 0.01f)
    }

    @Test
    fun testCalculateStudentPotentials_Positive() {
        val behavior = listOf(
            BehaviorEvent(studentId = 1L, type = "Positive Participation", timestamp = System.currentTimeMillis(), comment = null),
            BehaviorEvent(studentId = 1L, type = "Good Work", timestamp = System.currentTimeMillis(), comment = null)
        )
        val quizzes = listOf(
            QuizLog(studentId = 1L, quizName = "Test", markValue = 10.0, markType = "Test", maxMarkValue = 10.0, loggedAt = System.currentTimeMillis(), comment = null, marksData = "{}", numQuestions = 10)
        )
        val (k, b) = GhostOsmosisEngine.calculateStudentPotentials(behavior, quizzes, emptyList())

        // k = (1.0 (quiz) + 0.5 (hwork fallback)) / 2 = 0.75
        assertEquals(0.75f, k, 0.01f)
        // b = (2 - 0) / 2 = 1.0
        assertEquals(1.0f, b, 0.01f)
    }

    @Test
    fun testCalculateOsmosis_GradientGeneration() {
        val students = listOf(
            GhostOsmosisEngine.OsmoticNode(1, 100f, 100f, 0.9f, 0.8f),
            GhostOsmosisEngine.OsmoticNode(2, 200f, 200f, 0.3f, -0.5f)
        )

        val gradients = GhostOsmosisEngine.calculateOsmosis(students, gridSize = 10)

        // Ensure some gradients are generated
        assertTrue(gradients.isNotEmpty())

        // First gradient point (close to student 1)
        val first = gradients.first()
        assertTrue(first.potential > 0f)
    }
}
