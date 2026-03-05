package com.example.myapplication.labs.ghost.emergence

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import io.mockk.every
import io.mockk.mockk
import androidx.compose.runtime.mutableStateOf

class GhostEmergenceEngineTest {

    private val engine = GhostEmergenceEngine()

    @Test
    fun `test initial grid is empty`() {
        val grid = engine.getGrid()
        assertEquals(100, grid.size)
        grid.forEach { assertEquals(0f, it) }
    }

    @Test
    fun `test positive impulse increases vitality`() {
        val student = mockk<StudentUiItem>()
        every { student.id } returns 1
        every { student.xPosition.value } returns 2000f
        every { student.yPosition.value } returns 2000f

        val event = BehaviorEvent(studentId = 1, type = "Positive Participation", timestamp = System.currentTimeMillis())

        val grid = engine.update(listOf(student), listOf(event))

        val idx = 5 * 10 + 5
        assertTrue("Vitality at (5,5) should be positive", grid[idx] > 0)
    }

    @Test
    fun `test negative impulse decreases vitality`() {
        val student = mockk<StudentUiItem>()
        every { student.id } returns 1
        every { student.xPosition.value } returns 2000f
        every { student.yPosition.value } returns 2000f

        val event = BehaviorEvent(studentId = 1, type = "Negative Outburst", timestamp = System.currentTimeMillis())

        val grid = engine.update(listOf(student), listOf(event))

        val idx = 5 * 10 + 5
        assertTrue("Vitality at (5,5) should be negative", grid[idx] < 0)
    }

    @Test
    fun `test diffusion spreads vitality to neighbors`() {
        val student = mockk<StudentUiItem>()
        every { student.id } returns 1
        every { student.xPosition.value } returns 2000f
        every { student.yPosition.value } returns 2000f

        val event = BehaviorEvent(studentId = 1, type = "Positive", timestamp = System.currentTimeMillis())

        // Iteration 1: Impulse at (5, 5)
        engine.update(listOf(student), listOf(event))

        // Iteration 2: Diffusion should spread to (5, 6)
        val grid = engine.update(listOf(student), emptyList())

        val neighborIdx = 6 * 10 + 5
        assertTrue("Vitality should diffuse to neighbor", grid[neighborIdx] > 0)
    }
}
