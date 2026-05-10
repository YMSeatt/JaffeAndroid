package com.example.myapplication.labs.ghost

import androidx.compose.runtime.mutableStateOf
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import org.junit.Assert.assertEquals
import org.junit.Test

class GhostDeckEngineTest {

    private fun mockStudent(id: Int, name: String): StudentUiItem {
        return StudentUiItem(
            id = id,
            fullName = mutableStateOf(name),
            nickname = mutableStateOf(null),
            xPosition = mutableStateOf(0f),
            yPosition = mutableStateOf(0f)
        )
    }

    @Test
    fun `test createDeck sorts by affinity`() {
        val student1 = mockStudent(1, "Alice")
        val student2 = mockStudent(2, "Bob")
        val students = listOf(student1, student2)

        val behaviorLogs = listOf(
            BehaviorEvent(studentId = 1, type = "Negative (Disruption)", timestamp = 1000L),
            BehaviorEvent(studentId = 2, type = "Positive (Helping)", timestamp = 2000L)
        )

        val deck = GhostDeckEngine.createDeck(students, behaviorLogs, emptyList(), emptyList())

        assertEquals(2, deck.size)
        assertEquals(1, deck[0].student.id) // Alice should be first (affinity 1.0)
        assertEquals(1.0f, deck[0].affinity)
        assertEquals(2, deck[1].student.id) // Bob should be second (affinity 0.0)
        assertEquals(0.0f, deck[1].affinity)
    }

    @Test
    fun `test engagement score calculation`() {
        val student1 = mockStudent(1, "Alice")
        val students = listOf(student1)

        val quizLogs = listOf(
            com.example.myapplication.data.QuizLog(studentId = 1, quizName = "Q1", markValue = 10.0, maxMarkValue = 10.0, timestamp = 1000L),
            com.example.myapplication.data.QuizLog(studentId = 1, quizName = "Q2", markValue = 10.0, maxMarkValue = 10.0, timestamp = 2000L)
        )

        val deck = GhostDeckEngine.createDeck(students, emptyList(), quizLogs, emptyList())

        assertEquals(0.25f, deck[0].engagementScore) // 2 / 8 = 0.25
    }
}
