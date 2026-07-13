package com.example.myapplication.labs.ghost.quasar

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import androidx.compose.runtime.mutableStateOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostQuasarEngineTest {

    @Test
    fun testIdentifyQuasars() {
        val currentTime = System.currentTimeMillis()

        // Mock Students
        val student1 = StudentUiItem(1, "Alice", "Smith", mutableStateOf(100f), mutableStateOf(200f))
        val student2 = StudentUiItem(2, "Bob", "Jones", mutableStateOf(300f), mutableStateOf(400f))

        // Mock Logs for student1 (Quasar threshold reached)
        val logs = listOf(
            BehaviorEvent(1, 1, "Positive", currentTime - 1000, null),
            BehaviorEvent(2, 1, "Positive", currentTime - 2000, null),
            BehaviorEvent(3, 1, "Negative", currentTime - 3000, null)
        )

        val quasars = GhostQuasarEngine.identifyQuasars(listOf(student1, student2), logs, currentTime)

        assertEquals(1, quasars.size)
        assertEquals(1L, quasars[0].studentId)
        assertEquals(0.3f, quasars[0].energy, 0.01f)
        assertEquals(1/3f, quasars[0].behaviorPolarity, 0.01f)
    }

    @Test
    fun testIdentifyNoQuasarsUnderThreshold() {
        val currentTime = System.currentTimeMillis()
        val student1 = StudentUiItem(1, "Alice", "Smith", mutableStateOf(100f), mutableStateOf(200f))

        val logs = listOf(
            BehaviorEvent(1, 1, "Positive", currentTime - 1000, null),
            BehaviorEvent(2, 1, "Positive", currentTime - 2000, null)
        )

        val quasars = GhostQuasarEngine.identifyQuasars(listOf(student1), logs, currentTime)
        assertTrue(quasars.isEmpty())
    }
}
