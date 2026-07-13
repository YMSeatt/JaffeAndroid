package com.example.myapplication.labs.ghost

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.model.StudentUiItem
import org.junit.Assert.assertEquals
import org.junit.Test

class GhostLensEngineTest {

    @Test
    fun `getPropheciesForStudentsUnderLens returns prophecies for students within radius`() {
        val engine = GhostLensEngine()
        val lensPos = Offset(100f, 100f)
        val lensRadius = 250f

        val studentUnder = createMockStudent(1, 100f, 100f)
        val studentAway = createMockStudent(2, 500f, 500f)

        val prophecies = listOf(
            GhostOracle.Prophecy(1, GhostOracle.ProphecyType.ACADEMIC_SYNERGY, "Synergy predicted", 0.9f),
            GhostOracle.Prophecy(2, GhostOracle.ProphecyType.SOCIAL_FRICTION, "Friction predicted", 0.8f)
        )
        val propheciesByStudent = prophecies.groupBy { it.studentId }

        val result = engine.getPropheciesForStudentsUnderLens(
            students = listOf(studentUnder, studentAway),
            propheciesByStudent = propheciesByStudent,
            lensPos = lensPos,
            lensRadius = lensRadius,
            canvasScale = 1.0f,
            canvasOffset = Offset.Zero
        )

        assertEquals(1, result.size)
        assertEquals("Synergy predicted", result[0].description)
    }

    private fun createMockStudent(id: Long, x: Float, y: Float): StudentUiItem {
        return StudentUiItem(
            id = id.toInt(),
            fullName = mutableStateOf("Student $id"),
            nickname = mutableStateOf(null),
            initials = mutableStateOf("S$id"),
            xPosition = mutableStateOf(x),
            yPosition = mutableStateOf(y),
            displayWidth = mutableStateOf(100.dp),
            displayHeight = mutableStateOf(100.dp),
            displayBackgroundColor = mutableStateOf(listOf(Color.Gray)),
            displayOutlineColor = mutableStateOf(listOf(Color.Black)),
            displayTextColor = mutableStateOf(Color.White),
            displayOutlineThickness = mutableStateOf(2.dp),
            displayCornerRadius = mutableStateOf(8.dp),
            displayPadding = mutableStateOf(4.dp),
            fontFamily = mutableStateOf("sans-serif"),
            fontSize = mutableStateOf(12),
            fontColor = mutableStateOf(Color.Black),
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
            magneticRadius = mutableStateOf(0f)
        )
    }
}
