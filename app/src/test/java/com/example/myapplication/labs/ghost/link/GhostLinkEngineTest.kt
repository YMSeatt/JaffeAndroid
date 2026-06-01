package com.example.myapplication.labs.ghost.link

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Student
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostLinkEngineTest {

    @Test
    fun testIdentifyNeuralLinks() {
        val now = System.currentTimeMillis()

        // Case 1: Proximity + Synergy (Active)
        val s1 = Student(id = 1, firstName = "S1", lastName = "Test", xPosition = 100f, yPosition = 100f)
        val s2 = Student(id = 2, firstName = "S2", lastName = "Test", xPosition = 200f, yPosition = 200f) // Dist approx 141 < 600
        val logs = mapOf(
            1L to listOf(BehaviorEvent(id = 1, studentId = 1, type = "Positive", timestamp = now)),
            2L to listOf(BehaviorEvent(id = 2, studentId = 2, type = "Positive", timestamp = now))
        )

        val links = GhostLinkEngine.identifyNeuralLinks(listOf(s1, s2), logs)
        println("Case 1 links: ${links.size}")
        assertEquals(1, links.size)
        assertTrue(links[0].synergy > 0.5f)

        // Case 2: Too far
        val s3 = Student(id = 3, firstName = "S3", lastName = "Test", xPosition = 100f, yPosition = 100f)
        val s4 = Student(id = 4, firstName = "S4", lastName = "Test", xPosition = 1000f, yPosition = 1000f)
        val linksFar = GhostLinkEngine.identifyNeuralLinks(listOf(s3, s4), emptyMap())
        assertEquals(0, linksFar.size)
    }
}
