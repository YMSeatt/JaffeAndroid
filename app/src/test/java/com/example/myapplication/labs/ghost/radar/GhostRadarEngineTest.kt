package com.example.myapplication.labs.ghost.radar

import androidx.compose.runtime.mutableStateOf
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostRadarEngineTest {

    @Test
    fun `calculateLocalResonance returns 0 when no students in range`() {
        val resonance = GhostRadarEngine.calculateLocalResonance(
            targetX = 0f,
            targetY = 0f,
            students = emptyList(),
            behaviorLogsByStudent = emptyMap()
        )
        assertEquals(0f, resonance, 0.001f)
    }

    @Test
    fun `calculateLocalResonance detects nearby behavior`() {
        val studentId = 1L
        val students = listOf(
            StudentUiItem(
                id = studentId.toInt(),
                firstName = "Test",
                lastName = "Student",
                xPosition = mutableStateOf(100f),
                yPosition = mutableStateOf(100f)
            )
        )

        val now = System.currentTimeMillis()
        val logs = listOf(
            BehaviorEvent(studentId, "Negative Behavior", now - 1000)
        )
        val logsMap = mapOf(studentId to logs)

        // Near the student
        val resonanceNear = GhostRadarEngine.calculateLocalResonance(100f, 100f, students, logsMap)
        assertTrue("Resonance should be > 0 near student with logs", resonanceNear > 0f)

        // Far from the student (outside 500f radius)
        val resonanceFar = GhostRadarEngine.calculateLocalResonance(700f, 100f, students, logsMap)
        assertEquals(0f, resonanceFar, 0.001f)
    }

    @Test
    fun `calculateLocalResonance respects time decay`() {
        val studentId = 1L
        val students = listOf(
            StudentUiItem(
                id = studentId.toInt(),
                firstName = "Test",
                lastName = "Student",
                xPosition = mutableStateOf(100f),
                yPosition = mutableStateOf(100f)
            )
        )

        val now = System.currentTimeMillis()
        val timeWindow = 24 * 60 * 60 * 1000L

        val recentLogs = listOf(BehaviorEvent(studentId, "Negative", now - 1000))
        val oldLogs = listOf(BehaviorEvent(studentId, "Negative", now - (timeWindow - 1000)))

        val resRecent = GhostRadarEngine.calculateLocalResonance(100f, 100f, students, mapOf(studentId to recentLogs))
        val resOld = GhostRadarEngine.calculateLocalResonance(100f, 100f, students, mapOf(studentId to oldLogs))

        assertTrue("Recent logs should have higher resonance ($resRecent) than old logs ($resOld)", resRecent > resOld)
    }
}
