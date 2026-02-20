package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostPulseEngineTest {

    @Test
    fun testCalculateResonance_EmptyEvents() {
        val students = listOf(mockStudent(1))
        val pulses = GhostPulseEngine.calculateResonance(students, emptyList(), System.currentTimeMillis())
        assertTrue(pulses.isEmpty())
    }

    @Test
    fun testCalculateResonance_SingleRecentEvent() {
        val students = listOf(mockStudent(1))
        val currentTime = System.currentTimeMillis()
        val events = listOf(
            BehaviorEvent(id = 1, studentId = 1, type = "Positive", timestamp = currentTime - 1000, comment = null)
        )

        val pulses = GhostPulseEngine.calculateResonance(students, events, currentTime)

        assertEquals(1, pulses.size)
        assertEquals(1L, pulses[0].studentId)
        assertTrue(pulses[0].intensity > 0.7f)
        assertEquals(0.2f, pulses[0].color.first, 0.01f) // Green
    }

    @Test
    fun testCalculateResonance_MultipleEvents() {
        val students = listOf(mockStudent(1), mockStudent(2))
        val currentTime = System.currentTimeMillis()
        val events = listOf(
            BehaviorEvent(id = 1, studentId = 1, type = "Positive", timestamp = currentTime - 500, comment = null),
            BehaviorEvent(id = 2, studentId = 2, type = "Negative", timestamp = currentTime - 2000, comment = null)
        )

        val pulses = GhostPulseEngine.calculateResonance(students, events, currentTime)

        assertEquals(2, pulses.size)
        val pulse1 = pulses.find { it.studentId == 1L }!!
        val pulse2 = pulses.find { it.studentId == 2L }!!

        assertTrue(pulse1.intensity > pulse2.intensity)
        assertEquals(1.0f, pulse2.color.first, 0.01f) // Red
    }

    private fun mockStudent(id: Int): StudentUiItem {
        return StudentUiItem(
            id = id,
            fullName = mutableStateOf("Test Student"),
            nickname = mutableStateOf(""),
            initials = mutableStateOf("TS"),
            xPosition = mutableStateOf(100f),
            yPosition = mutableStateOf(100f),
            displayWidth = mutableStateOf(100.dp),
            displayHeight = mutableStateOf(100.dp),
            displayBackgroundColor = mutableStateOf(listOf(Color.White)),
            displayOutlineColor = mutableStateOf(listOf(Color.Black)),
            displayTextColor = mutableStateOf(Color.Black),
            displayOutlineThickness = mutableStateOf(2.dp),
            displayCornerRadius = mutableStateOf(8.dp),
            displayPadding = mutableStateOf(4.dp),
            fontFamily = mutableStateOf("SansSerif"),
            fontSize = mutableStateOf(12),
            fontColor = mutableStateOf(Color.Black),
            recentBehaviorDescription = mutableStateOf(emptyList()),
            recentHomeworkDescription = mutableStateOf(emptyList()),
            recentQuizDescription = mutableStateOf(emptyList()),
            groupColor = mutableStateOf(null),
            groupId = mutableStateOf(null),
            sessionLogText = mutableStateOf(emptyList()),
            temporaryTask = mutableStateOf(null)
        )
    }
}
