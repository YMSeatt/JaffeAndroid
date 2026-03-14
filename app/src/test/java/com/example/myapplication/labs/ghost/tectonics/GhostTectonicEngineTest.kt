package com.example.myapplication.labs.ghost.tectonics

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostTectonicEngineTest {

    @Test
    fun `calculateTectonicState calculates stress based on logs and proximity`() {
        val students = listOf(
            createMockStudent(1, 100f, 100f),
            createMockStudent(2, 200f, 200f), // Close to Student 1
            createMockStudent(3, 2000f, 2000f) // Far away
        )

        val now = System.currentTimeMillis()
        val logs = listOf(
            BehaviorEvent(1, 1L, "Negative", now, null),
            BehaviorEvent(2, 1L, "Negative", now, null)
        )

        val nodes = GhostTectonicEngine.calculateTectonicState(students, logs)

        // Student 1 has 2 negative logs -> base stress 0.3
        // Student 2 is close to Student 1 -> should have some proximity stress
        // Student 3 is far -> should have 0 stress
        val node1 = nodes.find { it.id == 1L }!!
        val node2 = nodes.find { it.id == 2L }!!
        val node3 = nodes.find { it.id == 3L }!!

        assertEquals(0.3f, node1.stress, 0.05f)
        assertTrue(node2.stress > 0f)
        assertEquals(0f, node3.stress, 0.01f)
    }

    @Test
    fun `analyzeSeismicRisk identifies fault lines and risk levels`() {
        // High stress cluster (3 students close together with high stress)
        val students = listOf(
            createMockStudent(1, 100f, 100f),
            createMockStudent(2, 200f, 100f),
            createMockStudent(3, 150f, 200f)
        )
        val logs = mutableListOf<BehaviorEvent>()
        for (i in 1..4) {
            logs.add(BehaviorEvent(i, 1L, "Negative", 0L, null))
            logs.add(BehaviorEvent(i + 4, 2L, "Negative", 0L, null))
            logs.add(BehaviorEvent(i + 8, 3L, "Negative", 0L, null))
        }

        val nodes = GhostTectonicEngine.calculateTectonicState(students, logs)
        val analysis = GhostTectonicEngine.analyzeSeismicRisk(nodes)

        assertTrue(analysis.faultLineCount >= 3)
        assertEquals(GhostTectonicEngine.RiskLevel.CRITICAL, analysis.riskLevel)
    }

    @Test
    fun `generateSeismicReport contains expected Markdown sections`() {
        val analysis = GhostTectonicEngine.SeismicAnalysis(0.35f, 0.85f, 2, GhostTectonicEngine.RiskLevel.CRITICAL)
        val report = GhostTectonicEngine.generateSeismicReport(analysis)

        assertTrue(report.contains("# \uD83C\uDF0B GHOST TECTONICS: SEISMIC ACTIVITY REPORT"))
        assertTrue(report.contains("Classroom Risk Level: CRITICAL"))
        assertTrue(report.contains("Average Social Stress: 35.0%"))
        assertTrue(report.contains("Active Fault Lines: 2"))
        assertTrue(report.contains("URGENT: Major seismic event imminent"))
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
