package com.example.myapplication.labs.ghost

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.model.StudentUiItem
import org.junit.Assert.*
import org.junit.Test

class GhostSparkEngineTest {

    @Test
    fun testEmitSparks() {
        val engine = GhostSparkEngine()
        assertEquals(0, engine.sparks.size)

        engine.emit(2000f, 2000f, "Positive Behavior")
        assertEquals(15, engine.sparks.size)

        engine.sparks.forEach { spark ->
            assertEquals(2000f, spark.x, 0.01f)
            assertEquals(2000f, spark.y, 0.01f)
            assertEquals(0, spark.colorType) // Positive
            assertEquals(1.0f, spark.life, 0.01f)
        }
    }

    @Test
    fun testUpdatePhysicsAndDecay() {
        val engine = GhostSparkEngine()
        engine.emit(2000f, 2000f, "Negative Behavior")

        val initialX = engine.sparks[0].x
        val initialLife = engine.sparks[0].life

        // No students nearby to affect gravity initially
        engine.update(emptyList(), deltaTime = 1.0f)

        assertNotEquals(initialX, engine.sparks[0].x)
        assertTrue(engine.sparks[0].life < initialLife)
    }

    @Test
    fun testSocialGravity() {
        val engine = GhostSparkEngine()
        // Emit spark at 1000, 1000
        engine.emit(1000f, 1000f, "Academic Progress")

        val spark = engine.sparks[0]
        spark.vx = 0f
        spark.vy = 0f

        // Place a student at 1100, 1000 (100 units away)
        val students = listOf(
            mockStudentUiItem(id = 1, x = 1100f, y = 1000f)
        )

        engine.update(students, deltaTime = 1.0f)

        // Spark should have been pulled towards the student (positive VX)
        assertTrue("Spark should accelerate towards student", spark.vx > 0f)
    }

    @Test
    fun testSparkLifecycleRemoval() {
        val engine = GhostSparkEngine()
        engine.emit(2000f, 2000f, "Positive")

        // Rapidly age sparks
        repeat(150) {
            engine.update(emptyList(), deltaTime = 1.0f)
        }

        assertTrue("Sparks should eventually be removed", engine.sparks.isEmpty())
    }

    @Test
    fun testReset() {
        val engine = GhostSparkEngine()
        engine.emit(2000f, 2000f, "Positive")
        assertFalse(engine.sparks.isEmpty())

        engine.reset()
        assertTrue(engine.sparks.isEmpty())
    }

    private fun mockStudentUiItem(id: Int, x: Float, y: Float): StudentUiItem {
        return StudentUiItem(
            id = id,
            fullName = mutableStateOf("Test Student"),
            nickname = mutableStateOf(null),
            initials = mutableStateOf("TS"),
            xPosition = mutableStateOf(x),
            yPosition = mutableStateOf(y),
            displayWidth = mutableStateOf(60.dp),
            displayHeight = mutableStateOf(60.dp),
            displayBackgroundColor = mutableStateOf(listOf(Color.Gray)),
            displayOutlineColor = mutableStateOf(listOf(Color.Black)),
            displayTextColor = mutableStateOf(Color.White),
            displayOutlineThickness = mutableStateOf(2.dp),
            displayCornerRadius = mutableStateOf(8.dp),
            displayPadding = mutableStateOf(4.dp),
            fontFamily = mutableStateOf("sans-serif"),
            fontSize = mutableStateOf(14),
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
