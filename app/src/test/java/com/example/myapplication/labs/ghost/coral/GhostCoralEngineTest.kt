package com.example.myapplication.labs.ghost.coral

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Student
import org.junit.Assert.*
import org.junit.Test

class GhostCoralEngineTest {

    @Test
    fun testSocialReefSynthesis() {
        val students = listOf(
            Student(id = 1, xPosition = 1000f, yPosition = 1000f, firstName = "Alice", lastName = "A"),
            Student(id = 2, xPosition = 1200f, yPosition = 1000f, firstName = "Bob", lastName = "B"),
            Student(id = 3, xPosition = 3000f, yPosition = 3000f, firstName = "Charlie", lastName = "C")
        )

        val behaviorLogsByStudent = mapOf(
            1L to listOf(BehaviorEvent(studentId = 1, type = "Positive Participation", timestamp = System.currentTimeMillis())),
            2L to listOf(BehaviorEvent(studentId = 2, type = "Great Leadership", timestamp = System.currentTimeMillis()))
        )

        val branches = GhostCoralEngine.calculateSocialReef(students, behaviorLogsByStudent)

        // Branch between Alice (1) and Bob (2) should exist due to proximity and positive logs
        val branch12 = branches.find { (it.idA == 1L && it.idB == 2L) || (it.idA == 2L && it.idB == 1L) }
        assertNotNull("Social Reef branch between Alice and Bob should exist", branch12)
        assertTrue("Branch density should be positive", branch12!!.density > 0)
        assertTrue("Branch vitality should be high for similar positive engagement", branch12.vitality > 0.5f)

        // Charlie (3) is too far away
        val branch13 = branches.find { (it.idA == 1L && it.idB == 3L) || (it.idA == 3L && it.idB == 1L) }
        assertNull("Social Reef branch between Alice and Charlie should not exist (too far)", branch13)
    }

    @Test
    fun testNegativeBehaviorExclusion() {
        val students = listOf(
            Student(id = 1, xPosition = 1000f, yPosition = 1000f, firstName = "Alice", lastName = "A"),
            Student(id = 2, xPosition = 1200f, yPosition = 1000f, firstName = "Bob", lastName = "B")
        )

        val behaviorLogsByStudent = mapOf(
            1L to listOf(BehaviorEvent(studentId = 1, type = "Disruptive", timestamp = System.currentTimeMillis())),
            2L to listOf(BehaviorEvent(studentId = 2, type = "Talking", timestamp = System.currentTimeMillis()))
        )

        val branches = GhostCoralEngine.calculateSocialReef(students, behaviorLogsByStudent)

        // No positive behavior, no calcification, no branches
        assertTrue("Social Reef should be empty when no positive behaviors are present", branches.isEmpty())
    }
}
