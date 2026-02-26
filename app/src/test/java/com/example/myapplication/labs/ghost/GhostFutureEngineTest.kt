package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import androidx.compose.runtime.mutableStateOf
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostFutureEngineTest {

    @Test
    fun testGenerateFutureEvents() {
        // Mock students
        val student1 = mockk<StudentUiItem>()
        every { student1.id } returns 1
        every { student1.fullName } returns mutableStateOf("Alice")
        every { student1.xPosition } returns mutableStateOf(100f)
        every { student1.yPosition } returns mutableStateOf(100f)

        val students = listOf(student1)
        val historicalLogs = emptyList<BehaviorEvent>()

        // Generate events for next 10 hours (to ensure we get some events)
        val events = GhostFutureEngine.generateFutureEvents(students, historicalLogs, hoursIntoFuture = 10)

        // Verify some events were generated
        assertTrue("Should generate at least some simulated events for a 10 hour window", events.isNotEmpty())

        // Verify event properties
        events.forEach { event ->
            assertTrue(event.type.contains("Simulated"))
            assertTrue(event.timestamp > System.currentTimeMillis())
        }
    }

    @Test
    fun testFutureEventsWithHistoricalBias() {
        val student1 = mockk<StudentUiItem>()
        every { student1.id } returns 1
        every { student1.fullName } returns mutableStateOf("Alice")
        every { student1.xPosition } returns mutableStateOf(100f)
        every { student1.yPosition } returns mutableStateOf(100f)

        val students = listOf(student1)

        // Add historical negative logs to increase probability
        val historicalLogs = List(10) {
            BehaviorEvent(studentId = 1, type = "Negative", timestamp = System.currentTimeMillis() - it * 100000, comment = null)
        }

        val events = GhostFutureEngine.generateFutureEvents(students, historicalLogs, hoursIntoFuture = 5)

        // Alice should have many simulated events due to negative bias
        assertTrue(events.any { it.studentId == 1L })
    }
}
