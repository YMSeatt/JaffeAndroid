package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostOracleTest {

    @Test
    fun `test Oracle detects social friction`() {
        val studentA = createMockStudent(1, "Alice", 0f, 0f)
        val studentB = createMockStudent(2, "Bob", 100f, 0f) // Close to Alice

        val logs = listOf(
            BehaviorEvent(studentId = 1, type = "Negative - Disruption", timestamp = System.currentTimeMillis(), comment = null),
            BehaviorEvent(studentId = 1, type = "Negative - Talking", timestamp = System.currentTimeMillis(), comment = null),
            BehaviorEvent(studentId = 2, type = "Negative - Disruption", timestamp = System.currentTimeMillis(), comment = null),
            BehaviorEvent(studentId = 2, type = "Negative - Talking", timestamp = System.currentTimeMillis(), comment = null)
        )

        val prophecies = GhostOracle.consult(listOf(studentA, studentB), logs)

        assertTrue(prophecies.any { it.type == GhostOracle.ProphecyType.SOCIAL_FRICTION })
    }

    @Test
    fun `test Oracle detects engagement drop`() {
        val student = createMockStudent(1, "Alice", 0f, 0f)

        // Positive log 10 days ago
        val tenDaysAgo = System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000)
        val logs = listOf(
            BehaviorEvent(studentId = 1, type = "Positive Participation", timestamp = tenDaysAgo, comment = null)
        )

        val prophecies = GhostOracle.consult(listOf(student), logs)

        assertTrue(prophecies.any { it.type == GhostOracle.ProphecyType.ENGAGEMENT_DROP })
    }

    private fun createMockStudent(id: Int, name: String, x: Float, y: Float): StudentUiItem {
        return StudentUiItem(
            id = id,
            fullName = name,
            nickname = null,
            initials = name.take(2),
            xPosition = mutableStateOf(x),
            yPosition = mutableStateOf(y),
            displayWidth = mutableStateOf(100.dp),
            displayHeight = mutableStateOf(100.dp),
            displayBackgroundColor = mutableStateOf(listOf(Color.White)),
            displayOutlineColor = mutableStateOf(listOf(Color.Black)),
            displayTextColor = mutableStateOf(Color.Black),
            displayOutlineThickness = mutableStateOf(1.dp),
            displayCornerRadius = mutableStateOf(4.dp),
            displayPadding = mutableStateOf(4.dp),
            fontFamily = mutableStateOf("sans-serif"),
            fontSize = mutableStateOf(14),
            fontColor = mutableStateOf(Color.Black),
            recentBehaviorDescription = emptyList(),
            recentHomeworkDescription = emptyList(),
            recentQuizDescription = emptyList(),
            groupColor = null,
            groupId = null,
            temporaryTask = null
        )
    }
}
