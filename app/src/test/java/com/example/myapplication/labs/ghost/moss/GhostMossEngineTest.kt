package com.example.myapplication.labs.ghost.moss

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.Student
import org.junit.Assert.assertEquals
import org.junit.Test

class GhostMossEngineTest {

    @Test
    fun testCalculateDormancyScores() {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L

        val students = listOf(
            Student(id = 1, firstName = "Active", lastName = "Student", xPosition = 0f, yPosition = 0f, gender = "M"),
            Student(id = 2, firstName = "New", lastName = "Student", xPosition = 100f, yPosition = 100f, gender = "F"),
            Student(id = 3, firstName = "Seven", lastName = "Days", xPosition = 200f, yPosition = 200f, gender = "M"),
            Student(id = 4, firstName = "Fourteen", lastName = "Days", xPosition = 300f, yPosition = 300f, gender = "F"),
            Student(id = 5, firstName = "Fully", lastName = "Dormant", xPosition = 400f, yPosition = 400f, gender = "M")
        )

        val behaviorLogs = mapOf(
            1L to listOf(BehaviorEvent(studentId = 1, type = "Positive", timestamp = now - 1 * 1000 * 60 * 60)), // 1 hour ago
            3L to listOf(BehaviorEvent(studentId = 3, type = "Positive", timestamp = now - 7 * dayMs)),
            4L to listOf(BehaviorEvent(studentId = 4, type = "Positive", timestamp = now - 14 * dayMs)),
            5L to listOf(BehaviorEvent(studentId = 5, type = "Positive", timestamp = now - 21 * dayMs))
        )

        val quizLogs = emptyMap<Long, List<QuizLog>>()
        val homeworkLogs = emptyMap<Long, List<HomeworkLog>>()

        val scores = GhostMossEngine.calculateDormancyScores(
            students,
            behaviorLogs,
            quizLogs,
            homeworkLogs,
            now
        )

        assertEquals(0.0f, scores.get(1L), 0.001f)
        assertEquals(0.2f, scores.get(2L), 0.001f) // No logs
        assertEquals(0.0f, scores.get(3L), 0.001f) // Exactly 7 days
        assertEquals(0.5f, scores.get(4L), 0.001f) // 14 days: (14-7)/(21-7) = 0.5
        assertEquals(1.0f, scores.get(5L), 0.001f) // 21 days
    }
}
