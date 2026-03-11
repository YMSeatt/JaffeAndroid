package com.example.myapplication.labs.ghost.pulsar

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostPulsarEngineTest {

    @Test
    fun testCalculateHarmonics_NoLogs() {
        val students = listOf(mockStudent(1))
        val currentTime = System.currentTimeMillis()
        val harmonics = GhostPulsarEngine.calculateHarmonics(students, emptyList(), currentTime)

        assertEquals(1, harmonics.size)
        assertEquals(0.1f, harmonics[0].frequency, 0.01f) // Base frequency
        assertEquals(0f, harmonics[0].amplitude, 0.01f)   // Zero amplitude
    }

    @Test
    fun testCalculateHarmonics_WithLogs() {
        val students = listOf(mockStudent(1))
        val currentTime = System.currentTimeMillis()
        val events = listOf(
            BehaviorEvent(id = 1, studentId = 1, type = "Positive", timestamp = currentTime - 1000, comment = null),
            BehaviorEvent(id = 2, studentId = 1, type = "Positive", timestamp = currentTime - 5000, comment = null)
        )

        val harmonics = GhostPulsarEngine.calculateHarmonics(students, events, currentTime)

        assertEquals(1, harmonics.size)
        // 2 logs in 10 mins = 0.2 logs/min. Engine clamps freq to 0.1..10.
        // Wait, 2 logs / 10 mins = 0.2. So freq should be 0.2.
        assertEquals(0.2f, harmonics[0].frequency, 0.01f)
        assertEquals(0.4f, harmonics[0].amplitude, 0.01f) // 2 / 5 = 0.4
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
