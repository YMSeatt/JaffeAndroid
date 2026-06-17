package com.example.myapplication.labs.ghost.sonar

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import androidx.compose.runtime.mutableStateOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostSonarEngineTest {

    @Test
    fun testIdentifyQuietStudents() {
        val now = System.currentTimeMillis()

        val student1 = createMockStudent(1)
        val student2 = createMockStudent(2)
        val student3 = createMockStudent(3)

        val students = listOf(student1, student2, student3)

        // Student 1 has a recent log
        val behaviorLogs = listOf(
            BehaviorEvent(studentId = 1, type = "Positive", timestamp = now - 1000L)
        )

        val quietIds = GhostSonarEngine.identifyQuietStudents(
            students = students,
            behaviorLogs = behaviorLogs,
            quizLogs = emptyList(),
            homeworkLogs = emptyList(),
            timeWindowMs = 5000L
        )

        assertEquals(2, quietIds.size)
        assertTrue(quietIds.contains(2L))
        assertTrue(quietIds.contains(3L))
        assertTrue(!quietIds.contains(1L))
    }

    @Test
    fun testEarlyExitWithSortedLogs() {
        val now = System.currentTimeMillis()

        val student1 = createMockStudent(1)
        val students = listOf(student1)

        // Logs are DESC sorted. First one is old, so we should break immediately and find student1 as quiet.
        val behaviorLogs = listOf(
            BehaviorEvent(studentId = 1, type = "Positive", timestamp = now - 10000L),
            BehaviorEvent(studentId = 1, type = "Positive", timestamp = now - 100L) // This should be first if sorted correctly
        )

        val quietIds = GhostSonarEngine.identifyQuietStudents(
            students = students,
            behaviorLogs = behaviorLogs,
            quizLogs = emptyList(),
            homeworkLogs = emptyList(),
            timeWindowMs = 5000L
        )

        // Because of the break in the loop when hitting the first old log, it won't see the second recent log.
        // This is expected behavior for BOLT's O(Recent) optimization which assumes sorted input.
        assertTrue(quietIds.contains(1L))
    }

    private fun createMockStudent(id: Int): StudentUiItem {
        return StudentUiItem(
            id = id,
            fullName = mutableStateOf("Student $id"),
            nickname = mutableStateOf(null),
            initials = mutableStateOf("S$id"),
            xPosition = mutableStateOf(0f),
            yPosition = mutableStateOf(0f),
            displayWidth = mutableStateOf(androidx.compose.ui.unit.dp(100)),
            displayHeight = mutableStateOf(androidx.compose.ui.unit.dp(100)),
            displayBackgroundColor = mutableStateOf(emptyList()),
            displayOutlineColor = mutableStateOf(emptyList()),
            displayTextColor = mutableStateOf(androidx.compose.ui.graphics.Color.Black),
            displayOutlineThickness = mutableStateOf(androidx.compose.ui.unit.dp(1)),
            displayCornerRadius = mutableStateOf(androidx.compose.ui.unit.dp(1)),
            displayPadding = mutableStateOf(androidx.compose.ui.unit.dp(1)),
            fontFamily = mutableStateOf("Default"),
            fontSize = mutableStateOf(12),
            fontColor = mutableStateOf(androidx.compose.ui.graphics.Color.Black),
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
            isPinned = mutableStateOf(false),
            insightStatus = mutableStateOf(com.example.myapplication.labs.ghost.InsightStatus.STABLE),
            moodState = mutableStateOf(com.example.myapplication.labs.ghost.mood.GhostMoodEngine.MoodState.CALM),
            moodIntensity = mutableStateOf(0f),
            moodValence = mutableStateOf(0f)
        )
    }
}
