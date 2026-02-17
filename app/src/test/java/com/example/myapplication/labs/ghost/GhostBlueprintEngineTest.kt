package com.example.myapplication.labs.ghost

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.model.FurnitureUiItem
import com.example.myapplication.ui.model.StudentUiItem
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostBlueprintEngineTest {

    @Test
    fun testGenerateBlueprint() {
        val students = listOf(
            createMockStudent(1, "Alice Wonderland", "AW", 400f, 400f),
            createMockStudent(2, "Bob Builder", "BB", 1200f, 400f)
        )
        val furniture = listOf(
            FurnitureUiItem(
                id = 1,
                stringId = "f1",
                name = mutableStateOf("Desk"),
                type = mutableStateOf("desk"),
                xPosition = mutableStateOf(800f),
                yPosition = mutableStateOf(1200f),
                displayWidth = mutableStateOf(200.dp),
                displayHeight = mutableStateOf(100.dp),
                displayBackgroundColor = mutableStateOf(Color.Gray),
                displayOutlineColor = mutableStateOf(Color.Black),
                displayTextColor = mutableStateOf(Color.White),
                displayOutlineThickness = mutableStateOf(2.dp)
            )
        )

        val svg = GhostBlueprintEngine.generateBlueprint(students, furniture)

        // Verify SVG basic structure
        assertTrue(svg.contains("<svg width=\"1200\" height=\"800\""))
        assertTrue(svg.contains("viewBox=\"0 0 1200 800\""))
        assertTrue(svg.contains("<linearGradient id=\"grad1\""))
        assertTrue(svg.contains("<filter id=\"glow\""))

        // Verify student 1 scaling: (400 / 4) + 200 = 300, (400 / 4) + 100 = 200
        assertTrue(svg.contains("translate(300.0,200.0)"))
        // Verify student 2 scaling: (1200 / 4) + 200 = 500, (400 / 4) + 100 = 200
        assertTrue(svg.contains("translate(500.0,200.0)"))

        // Verify student content
        assertTrue(svg.contains(">AW</text>"))
        assertTrue(svg.contains(">Alice Wonderland</text>"))
        assertTrue(svg.contains(">BB</text>"))
        assertTrue(svg.contains(">Bob Builder</text>"))

        // Verify furniture scaling: (800 / 4) + 200 = 400, (1200 / 4) + 100 = 400
        assertTrue(svg.contains("translate(400.0,400.0)"))
        assertTrue(svg.contains("stroke-dasharray=\"5,5\""))
        assertTrue(svg.contains(">Desk</text>"))

        // Verify grid lines
        assertTrue(svg.contains("<!-- Grid Lines"))
    }

    private fun createMockStudent(id: Int, name: String, initials: String, x: Float, y: Float): StudentUiItem {
        return StudentUiItem(
            id = id,
            fullName = mutableStateOf(name),
            nickname = mutableStateOf(null),
            initials = mutableStateOf(initials),
            xPosition = mutableStateOf(x),
            yPosition = mutableStateOf(y),
            displayWidth = mutableStateOf(130.dp),
            displayHeight = mutableStateOf(80.dp),
            displayBackgroundColor = mutableStateOf(listOf(Color.Blue)),
            displayOutlineColor = mutableStateOf(listOf(Color.Black)),
            displayTextColor = mutableStateOf(Color.White),
            displayOutlineThickness = mutableStateOf(2.dp),
            displayCornerRadius = mutableStateOf(8.dp),
            displayPadding = mutableStateOf(4.dp),
            fontFamily = mutableStateOf("monospace"),
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
