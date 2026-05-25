package com.example.myapplication.labs.ghost.rain

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Test

class GhostRainEngineTest {

    @Test
    fun testDropletPooling() {
        val engine = GhostRainEngine()

        // Initial state
        for (i in 0 until GhostRainEngine.MAX_DROPLETS) {
            assertFalse(engine.dropActive[i])
        }

        // Update with high intensity
        val logs = listOf(
            BehaviorEvent(studentId = 1, type = "Positive", timestamp = System.currentTimeMillis())
        )

        // Run multiple updates to ensure droplets spawn
        repeat(100) {
            engine.update(emptyList(), logs)
        }

        var activeCount = 0
        for (i in 0 until GhostRainEngine.MAX_DROPLETS) {
            if (engine.dropActive[i]) activeCount++
        }

        assertTrue("Should have active droplets after activity", activeCount > 0)
    }

    @Test
    fun testSplashLogic() {
        val engine = GhostRainEngine()

        // Manually trigger a droplet
        engine.dropActive[0] = true
        engine.dropX[0] = 500f
        engine.dropY[0] = 500f
        engine.dropVel[0] = 10f
        engine.splashTime[0] = 0f

        // Student icon at the same position
        val mockStudent = StudentUiItem(
            id = 1,
            firstName = mutableStateOf("Test"),
            lastName = mutableStateOf("Student"),
            xPosition = mutableStateOf(450f),
            yPosition = mutableStateOf(450f),
            displayWidth = mutableStateOf(100.dp),
            displayHeight = mutableStateOf(100.dp)
        )

        engine.update(listOf(mockStudent), emptyList())

        assertTrue("Droplet should splash on student icon", engine.splashTime[0] > 0f)
    }
}
