package com.example.myapplication.labs.ghost.stream

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import com.example.myapplication.ui.model.StudentUiItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostStreamEngineTest {

    private fun mockStudent(id: Int, name: String): StudentUiItem {
        return StudentUiItem(
            id = id,
            fullName = mutableStateOf(name),
            nickname = mutableStateOf(null),
            initials = mutableStateOf(""),
            xPosition = mutableStateOf(0f),
            yPosition = mutableStateOf(0f),
            displayWidth = mutableStateOf(0.dp),
            displayHeight = mutableStateOf(0.dp),
            displayBackgroundColor = mutableStateOf(emptyList()),
            displayOutlineColor = mutableStateOf(emptyList()),
            displayTextColor = mutableStateOf(Color.Black),
            displayOutlineThickness = mutableStateOf(0.dp),
            displayCornerRadius = mutableStateOf(0.dp),
            displayPadding = mutableStateOf(0.dp),
            fontFamily = mutableStateOf(""),
            fontSize = mutableStateOf(0),
            fontColor = mutableStateOf(Color.Black),
            recentBehaviorDescription = mutableStateOf(emptyList()),
            recentHomeworkDescription = mutableStateOf(emptyList()),
            recentQuizDescription = mutableStateOf(emptyList()),
            groupColor = mutableStateOf(null),
            groupId = mutableStateOf(null),
            sessionLogText = mutableStateOf(emptyList()),
            temporaryTask = mutableStateOf(null),
            irisParams = mutableStateOf(null),
            osmoticNode = mutableStateOf(null),
            altitude = mutableStateOf(0f),
            behaviorEntropy = mutableStateOf(0f),
            tectonicStress = mutableStateOf(0f),
            quasarEnergy = mutableStateOf(0f),
            quasarPolarity = mutableStateOf(0f),
            ionCharge = mutableStateOf(0f),
            ionDensity = mutableStateOf(0f),
            magneticStrength = mutableStateOf(0f),
            magneticRadius = mutableStateOf(0f),
            isPinned = mutableStateOf(false)
        )
    }

    @Test
    fun `test synthesizeStream combines and sorts diverse logs`() {
        val student = mockStudent(1, "John Doe")
        val students = listOf(student)

        val behaviorLogs = listOf(
            BehaviorEvent(id = 1, studentId = 1, type = "Participating", timestamp = 3000L)
        )
        val quizLogs = listOf(
            QuizLog(id = 1, studentId = 1, quizName = "Math", markValue = 10.0, maxMarkValue = 10.0, loggedAt = 1000L, marksData = "{}", numQuestions = 10)
        )
        val homeworkLogs = listOf(
            HomeworkLog(id = 1, studentId = 1, assignmentName = "Reading", status = "Complete", loggedAt = 2000L, marksData = "{}")
        )

        val stream = GhostStreamEngine.synthesizeStream(students, behaviorLogs, quizLogs, homeworkLogs, maxEntries = 5)

        assertEquals(3, stream.size)
        assertEquals("Participating", stream[0].content) // Newest first
        assertEquals(3000L, stream[0].timestamp)
        assertEquals("Homework: Reading -> Complete", stream[1].content)
        assertEquals(2000L, stream[1].timestamp)
        assertEquals("Quiz: Math (10.0/10.0)", stream[2].content)
        assertEquals(1000L, stream[2].timestamp)
    }

    @Test
    fun `test synthesizeStream respects maxEntries limit`() {
        val student = mockStudent(1, "John Doe")
        val students = listOf(student)

        val behaviorLogs = (1..50).map { i ->
            BehaviorEvent(id = i.toLong(), studentId = 1, type = "Behavior $i", timestamp = i.toLong())
        }.reversed() // Simulating pre-sorted input

        val stream = GhostStreamEngine.synthesizeStream(students, behaviorLogs, emptyList(), emptyList(), maxEntries = 10)

        assertEquals(10, stream.size)
        assertEquals("Behavior 50", stream[0].content)
        assertEquals(50L, stream[0].timestamp)
        assertEquals(41L, stream[9].timestamp)
    }

    @Test
    fun `test synthesizeStream handles unknown students gracefully`() {
        val students = emptyList<StudentUiItem>()
        val behaviorLogs = listOf(
            BehaviorEvent(id = 1, studentId = 999, type = "Unknown", timestamp = 1000L)
        )

        val stream = GhostStreamEngine.synthesizeStream(students, behaviorLogs, emptyList(), emptyList())

        assertTrue(stream.isEmpty())
    }

    @Test
    fun `test synthesizeStream identifies negative behavior`() {
        val student = mockStudent(1, "John Doe")
        val students = listOf(student)
        val behaviorLogs = listOf(
            BehaviorEvent(id = 1, studentId = 1, type = "Negative Disruption", timestamp = 1000L)
        )

        val stream = GhostStreamEngine.synthesizeStream(students, behaviorLogs, emptyList(), emptyList())

        assertEquals(GhostStreamEngine.EntryType.NEGATIVE, stream[0].type)
    }
}
