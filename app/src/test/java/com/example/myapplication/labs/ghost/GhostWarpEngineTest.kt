package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.labs.ghost.warp.GhostWarpEngine
import com.example.myapplication.ui.model.StudentUiItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostWarpEngineTest {

    @Test
    fun `calculateGravityPoints returns top activity students`() {
        val students = listOf(
            createMockStudent(1, 100f, 100f),
            createMockStudent(2, 500f, 500f),
            createMockStudent(3, 1000f, 1000f)
        )

        val now = System.currentTimeMillis()
        val logs = listOf(
            BehaviorEvent(1, 1L, "Positive", now, null),
            BehaviorEvent(2, 2L, "Negative", now, null),
            BehaviorEvent(3, 2L, "Negative", now, null),
            BehaviorEvent(4, 2L, "Positive", now, null)
        )

        val points = GhostWarpEngine.calculateGravityPoints(students, logs)

        // Student 2 has 3 logs (2 negative), should have highest mass
        assertEquals(2, points.size)
        assertEquals(2, students.find { it.xPosition.value == points[0].x }?.id)
        assertTrue(points[0].mass > points[1].mass)
    }

    @Test
    fun `calculateGravityPoints respects maxPoints`() {
        val students = (1..20).map { createMockStudent(it, it * 10f, it * 10f) }
        val logs = (1..20).map { BehaviorEvent(id = it, studentId = it.toLong(), type = "Positive", timestamp = System.currentTimeMillis(), comment = null) }

        val points = GhostWarpEngine.calculateGravityPoints(students, logs, maxPoints = 5)

        assertEquals(5, points.size)
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
