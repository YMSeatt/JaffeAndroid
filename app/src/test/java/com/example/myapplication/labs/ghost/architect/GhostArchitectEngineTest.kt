package com.example.myapplication.labs.ghost.architect

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.labs.ghost.lattice.GhostLatticeEngine
import com.example.myapplication.ui.model.StudentUiItem
import org.junit.Assert.assertEquals
import org.junit.Test

class GhostArchitectEngineTest {

    private fun createMockStudent(id: Int, x: Float, y: Float): StudentUiItem {
        return StudentUiItem(
            id = id,
            fullName = mutableStateOf("Student $id"),
            nickname = mutableStateOf(null),
            initials = mutableStateOf("S$id"),
            xPosition = mutableStateOf(x),
            yPosition = mutableStateOf(y),
            displayWidth = mutableStateOf(100.dp),
            displayHeight = mutableStateOf(100.dp),
            displayBackgroundColor = mutableStateOf(listOf(Color.White)),
            displayOutlineColor = mutableStateOf(listOf(Color.Black)),
            displayTextColor = mutableStateOf(Color.Black),
            displayOutlineThickness = mutableStateOf(1.dp),
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
            quasarPolarity = mutableStateOf(0f)
        )
    }

    @Test
    fun testCalculateSynergy_Collaboration_Perfect() {
        // In Python: score = 1.0 - (avg_dist / 1000.0)
        // In Android (Proposed): score = 1.0 - (avg_dist / 2000.0)
        // If dist = 0, score should be 1.0
        val s1 = createMockStudent(1, 500f, 500f)
        val s2 = createMockStudent(2, 500f, 500f)
        val edges = listOf(
            GhostLatticeEngine.Edge(1L, 2L, 1.0f, GhostLatticeEngine.ConnectionType.COLLABORATION, Color.Green)
        )

        val synergy = GhostArchitectEngine.calculateSynergy(listOf(s1, s2), edges, GhostArchitectEngine.StrategicGoal.COLLABORATION)
        assertEquals(1.0f, synergy, 0.001f)
    }

    @Test
    fun testCalculateSynergy_Collaboration_Half() {
        // avg_dist = 1000, max_dist = 2000. score = 1.0 - 1000/2000 = 0.5
        val s1 = createMockStudent(1, 0f, 0f)
        val s2 = createMockStudent(2, 1000f, 0f)
        val edges = listOf(
            GhostLatticeEngine.Edge(1L, 2L, 0.5f, GhostLatticeEngine.ConnectionType.COLLABORATION, Color.Green)
        )

        val synergy = GhostArchitectEngine.calculateSynergy(listOf(s1, s2), edges, GhostArchitectEngine.StrategicGoal.COLLABORATION)
        assertEquals(0.5f, synergy, 0.001f)
    }

    @Test
    fun testCalculateSynergy_Focus_Perfect() {
        // In Python: score = (avg_dist / 1500.0)
        // In Android (Proposed): score = (avg_dist / 3000.0)
        // If dist = 3000, score should be 1.0
        val s1 = createMockStudent(1, 0f, 0f)
        val s2 = createMockStudent(2, 3000f, 0f)
        val edges = listOf(
            GhostLatticeEngine.Edge(1L, 2L, 0.1f, GhostLatticeEngine.ConnectionType.FRICTION, Color.Red)
        )

        val synergy = GhostArchitectEngine.calculateSynergy(listOf(s1, s2), edges, GhostArchitectEngine.StrategicGoal.FOCUS)
        assertEquals(1.0f, synergy, 0.001f)
    }

    @Test
    fun testCalculateSynergy_NoEdges() {
        // Should return 1.0 as per Python blueprint
        val s1 = createMockStudent(1, 500f, 500f)
        val synergy = GhostArchitectEngine.calculateSynergy(listOf(s1), emptyList(), GhostArchitectEngine.StrategicGoal.COLLABORATION)
        assertEquals(1.0f, synergy, 0.001f)
    }
}
