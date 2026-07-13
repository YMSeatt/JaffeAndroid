package com.example.myapplication.labs.ghost.ion

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp

class GhostIonEngineTest {

    private fun createMockStudent(id: Int, name: String): StudentUiItem {
        return StudentUiItem(
            id = id,
            fullName = mutableStateOf(name),
            nickname = mutableStateOf(null),
            initials = mutableStateOf(""),
            xPosition = mutableStateOf(0f),
            yPosition = mutableStateOf(0f),
            displayWidth = mutableStateOf(100.dp),
            displayHeight = mutableStateOf(100.dp),
            displayBackgroundColor = mutableStateOf(emptyList()),
            displayOutlineColor = mutableStateOf(emptyList()),
            displayTextColor = mutableStateOf(androidx.compose.ui.graphics.Color.Black),
            displayOutlineThickness = mutableStateOf(1.dp),
            displayCornerRadius = mutableStateOf(4.dp),
            displayPadding = mutableStateOf(4.dp),
            fontFamily = mutableStateOf("SansSerif"),
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
            osmoticNode = mutableStateOf(null)
        )
    }

    @Test
    fun testCalculateIonization() {
        val students = listOf(
            createMockStudent(1, "Alice"),
            createMockStudent(2, "Bob")
        )

        val logs = listOf(
            BehaviorEvent(1, 1, "Positive Participation", 1000L, ""),
            BehaviorEvent(2, 2, "Negative Behavior", 1000L, "")
        )

        val ionPoints = GhostIonEngine.calculateIonization(students, logs, batteryTemp = 40f)

        assertEquals(2, ionPoints.size)

        // Alice should have positive charge
        assertTrue(ionPoints[0].charge > 0f)
        // Bob should have negative charge
        assertTrue(ionPoints[1].charge < 0f)

        // High density due to high battery temp (40C)
        assertTrue(ionPoints[0].density > 0.2f)
    }

    @Test
    fun testGlobalBalance() {
        val points = listOf(
            GhostIonEngine.IonPoint(0f, 0f, 0.5f, 0.5f),
            GhostIonEngine.IonPoint(1f, 1f, -0.5f, 0.5f)
        )

        val balance = GhostIonEngine.calculateGlobalBalance(points)
        assertEquals(0f, balance, 0.001f)
    }
}
