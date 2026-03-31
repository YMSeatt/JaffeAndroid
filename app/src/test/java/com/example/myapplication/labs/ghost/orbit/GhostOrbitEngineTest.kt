package com.example.myapplication.labs.ghost.orbit

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class GhostOrbitEngineTest {

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
            displayBackgroundColor = mutableStateOf(emptyList()),
            displayOutlineColor = mutableStateOf(emptyList()),
            displayTextColor = mutableStateOf(androidx.compose.ui.graphics.Color.Black),
            displayOutlineThickness = mutableStateOf(2.dp),
            displayCornerRadius = mutableStateOf(8.dp),
            displayPadding = mutableStateOf(4.dp),
            fontFamily = mutableStateOf("sans-serif"),
            fontSize = mutableStateOf(12),
            fontColor = mutableStateOf(androidx.compose.ui.graphics.Color.Black),
            recentBehaviorDescription = mutableStateOf(emptyList()),
            recentHomeworkDescription = mutableStateOf(emptyList()),
            recentQuizDescription = mutableStateOf(emptyList()),
            groupColor = mutableStateOf(null),
            groupId = mutableStateOf(null),
            sessionLogText = mutableStateOf(emptyList()),
            temporaryTask = mutableStateOf(null),
            irisParams = mutableStateOf(com.example.myapplication.labs.ghost.GhostIrisEngine.IrisParameters(0f, androidx.compose.ui.graphics.Color.White, androidx.compose.ui.graphics.Color.Blue, 0.5f)),
            osmoticNode = mutableStateOf(com.example.myapplication.labs.ghost.osmosis.GhostOsmosisEngine.OsmoticNode(id, x, y, 0.5f, 0.5f)),
            altitude = mutableStateOf(0.5f),
            behaviorEntropy = mutableStateOf(0f),
            tectonicStress = mutableStateOf(0f),
            quasarEnergy = mutableStateOf(0f),
            quasarPolarity = mutableStateOf(0f)
        )
    }

    @Test
    fun testCalculateOrbits_Empty() {
        val states = GhostOrbitEngine.calculateOrbits(emptyList(), emptyList(), 0f)
        assertTrue(states.isEmpty())
    }

    @Test
    fun testCalculateOrbits_Basic() {
        val students = listOf(createMockStudent(1L, 1000f, 1000f))
        val states = GhostOrbitEngine.calculateOrbits(students, emptyList(), 0f)

        assertEquals(1, states.size)
        val state = states[0]
        assertEquals(1L, state.studentId)
        assertEquals(2000f, state.centerX) // Defaults to classroom center
        assertEquals(2000f, state.centerY)
        assertEquals(0.8f, state.stability, 0.01f) // Default stability
    }

    @Test
    fun testCalculateOrbits_EngagementSpeed() {
        val now = System.currentTimeMillis()
        val student = createMockStudent(1L, 1000f, 1000f)
        val logs = listOf(
            BehaviorEvent(studentId = 1L, type = "Positive", timestamp = now - 1000, comment = null),
            BehaviorEvent(studentId = 1L, type = "Positive", timestamp = now - 2000, comment = null)
        )

        val state0 = GhostOrbitEngine.calculateOrbits(listOf(student), emptyList(), 0f)[0]
        val stateWithLogs = GhostOrbitEngine.calculateOrbits(listOf(student), logs, 0f)[0]

        // Engagement = 2/5 = 0.4. Speed = 0.5 * 0.4 = 0.2
        // Base Speed with 0 logs is 0.5 * 0.1 = 0.05 (coerced)
        assertTrue(stateWithLogs.speed > state0.speed)
    }

    @Test
    fun testCalculateOrbits_SocialSuns() {
        val now = System.currentTimeMillis()
        // Student 1 is a Sun (6 positive logs)
        val sun = createMockStudent(1L, 500f, 500f)
        val logs = mutableListOf<BehaviorEvent>()
        repeat(6) {
            logs.add(BehaviorEvent(studentId = 1L, type = "Positive", timestamp = now, comment = null))
        }

        // Student 2 is a "Planet"
        val planet = createMockStudent(2L, 1000f, 1000f)

        val states = GhostOrbitEngine.calculateOrbits(listOf(sun, planet), logs, 0f)
        val planetState = states.find { it.studentId == 2L }!!

        assertEquals(500f, planetState.centerX)
        assertEquals(500f, planetState.centerY)
    }
}
