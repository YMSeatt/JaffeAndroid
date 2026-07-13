package com.example.myapplication.labs.ghost.phoenix

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Student
import org.junit.Assert.assertEquals
import org.junit.Test

class GhostPhoenixEngineTest {

    @Test
    fun testCalculateResilienceScores_ResilientStudent() {
        val now = System.currentTimeMillis()
        val student = Student(id = 1L, firstName = "John", lastName = "Doe")
        val students = listOf(student)

        // Historical struggle (6 hours ago) and recent recovery (1 hour ago)
        val logs = listOf(
            BehaviorEvent(studentId = 1L, type = "Positive Behavior", timestamp = now - 3600000),
            BehaviorEvent(studentId = 1L, type = "Positive Behavior", timestamp = now - 3600100),
            BehaviorEvent(studentId = 1L, type = "Positive Behavior", timestamp = now - 3600200),
            BehaviorEvent(studentId = 1L, type = "Negative Behavior", timestamp = now - 21600000),
            BehaviorEvent(studentId = 1L, type = "Negative Behavior", timestamp = now - 21600100)
        )

        val behaviorLogsByStudent = mapOf(1L to logs)
        val scores = GhostPhoenixEngine.calculateResilienceScores(students, behaviorLogsByStudent)

        // 3 positive * 0.2 + 2 negative * 0.1 = 0.6 + 0.2 = 0.8
        assertEquals(0.8f, scores[1L]!!, 0.01f)
    }

    @Test
    fun testCalculateResilienceScores_ConsistentlyGoodStudent() {
        val now = System.currentTimeMillis()
        val student = Student(id = 1L, firstName = "Jane", lastName = "Doe")
        val students = listOf(student)

        // Only positive logs, no historical struggle
        val logs = listOf(
            BehaviorEvent(studentId = 1L, type = "Positive Behavior", timestamp = now - 3600000),
            BehaviorEvent(studentId = 1L, type = "Positive Behavior", timestamp = now - 3600100)
        )

        val behaviorLogsByStudent = mapOf(1L to logs)
        val scores = GhostPhoenixEngine.calculateResilienceScores(students, behaviorLogsByStudent)

        // No historical negative logs, score should be 0.0
        assertEquals(0.0f, scores[1L]!!, 0.01f)
    }
}
