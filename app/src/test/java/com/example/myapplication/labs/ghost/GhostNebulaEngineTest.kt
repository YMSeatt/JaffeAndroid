package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostNebulaEngineTest {

    @Test
    fun `calculateNebula identifies clusters from logs`() {
        val students = listOf(
            createMockStudent(1, 100f, 100f),
            createMockStudent(2, 500f, 500f)
        )

        val now = System.currentTimeMillis()
        val logs = listOf(
            BehaviorEvent(1, 1L, "Positive Interaction", now, null),
            BehaviorEvent(2, 2L, "Negative Disruption", now, null),
            BehaviorEvent(3, 2L, "Negative Disruption", now, null)
        )

        val (intensity, clusters) = GhostNebulaEngine.calculateNebula(students, logs)

        assertEquals(2, clusters.size)

        // Cluster 1 (Student 2) should be first (more logs) or sorted somehow?
        // Current implementation groups and sorts by log count.
        val clusterStudent2 = clusters.find { it.x == 500f }
        val clusterStudent1 = clusters.find { it.x == 100f }

        assertTrue(clusterStudent2 != null)
        assertTrue(clusterStudent1 != null)

        assertEquals(1f, clusterStudent2!!.colorIndex) // Negative
        assertEquals(0f, clusterStudent1!!.colorIndex) // Positive

        assertTrue(intensity > 0.3f)
    }

    @Test
    fun `calculateNebula respects time window`() {
        val students = listOf(createMockStudent(1, 100f, 100f))
        val now = System.currentTimeMillis()
        val oldLogs = listOf(
            BehaviorEvent(1, 1L, "Positive", now - 20 * 60 * 1000L, null) // 20 mins ago
        )

        val (intensity, clusters) = GhostNebulaEngine.calculateNebula(students, oldLogs, timeWindowMs = 15 * 60 * 1000L)

        assertEquals(0, clusters.size)
        assertEquals(0.3f, intensity)
    }

    private fun createMockStudent(id: Int, x: Float, y: Float): StudentUiItem {
        @Suppress("DEPRECATION")
        return StudentUiItem(
            id = id,
            fullName = mutableStateOf("Student $id"),
            nickname = mutableStateOf(null),
            initials = mutableStateOf("S$id"),
            xPosition = mutableStateOf(x),
            yPosition = mutableStateOf(y),
            displayWidth = mutableStateOf(100.dp),
            displayHeight = mutableStateOf(100.dp),
            displayBackgroundColor = mutableStateOf(listOf()),
            displayOutlineColor = mutableStateOf(listOf()),
            displayTextColor = mutableStateOf(androidx.compose.ui.graphics.Color.Black),
            displayOutlineThickness = mutableStateOf(1.dp),
            displayCornerRadius = mutableStateOf(8.dp),
            displayPadding = mutableStateOf(4.dp),
            fontFamily = mutableStateOf("sans-serif"),
            fontSize = mutableStateOf(12),
            fontColor = mutableStateOf(androidx.compose.ui.graphics.Color.Black),
            recentBehaviorDescription = mutableStateOf(listOf()),
            recentHomeworkDescription = mutableStateOf(listOf()),
            recentQuizDescription = mutableStateOf(listOf()),
            groupColor = mutableStateOf(null),
            groupId = mutableStateOf(null),
            sessionLogText = mutableStateOf(listOf()),
            temporaryTask = mutableStateOf(null)
        )
    }
}
