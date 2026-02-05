package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Student
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostCognitiveEngineTest {

    @Test
    fun `optimizeLayout moves students from initial positions`() {
        val students = listOf(
            createStudent(1, 100f, 100f),
            createStudent(2, 110f, 110f) // Very close, should repel
        )
        val behaviorLogs = emptyList<BehaviorEvent>()

        val result = GhostCognitiveEngine.optimizeLayout(students, behaviorLogs, 4000, 4000)

        assertEqualsWithTolerance(2, result.size)
        val p1 = result[1L]!!
        val p2 = result[2L]!!

        // They should have moved away from each other
        assertNotEquals(100f, p1.x)
        assertNotEquals(100f, p1.y)
        assertNotEquals(110f, p2.x)
        assertNotEquals(110f, p2.y)

        val initialDistSq = 10f * 10f + 10f * 10f
        val finalDistSq = (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)

        assertTrue("Students should have repelled each other", finalDistSq > initialDistSq)
    }

    @Test
    fun `optimizeLayout handles groups by pulling members together`() {
        // Two students far apart but in the same group
        val students = listOf(
            createStudent(1, 100f, 100f, groupId = 1),
            createStudent(2, 500f, 500f, groupId = 1)
        )
        val behaviorLogs = emptyList<BehaviorEvent>()

        val result = GhostCognitiveEngine.optimizeLayout(students, behaviorLogs, 4000, 4000)

        val p1 = result[1L]!!
        val p2 = result[2L]!!

        val initialDistSq = 400f * 400f + 400f * 400f
        val finalDistSq = (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)

        assertTrue("Group members should have been pulled together", finalDistSq < initialDistSq)
    }

    @Test
    fun `optimizeLayout increases repulsion for negative behavior`() {
        val s1 = createStudent(1, 100f, 100f)
        val s2 = createStudent(2, 200f, 200f)
        val s3 = createStudent(3, 100f, 100f)
        val s4 = createStudent(4, 200f, 200f)

        // Case A: No negative behavior
        val resultA = GhostCognitiveEngine.optimizeLayout(listOf(s1, s2), emptyList(), 4000, 4000)
        val distA = dist(resultA[1L]!!, resultA[2L]!!)

        // Case B: Negative behavior for student 3
        val behaviorLogs = listOf(
            BehaviorEvent(studentId = 3, type = "Negative Participation", timestamp = System.currentTimeMillis(), comment = "Test")
        )
        val resultB = GhostCognitiveEngine.optimizeLayout(listOf(s3, s4), behaviorLogs, 4000, 4000)
        val distB = dist(resultB[3L]!!, resultB[4L]!!)

        assertTrue("Negative behavior should increase repulsion", distB > distA)
    }

    private fun createStudent(id: Long, x: Float, y: Float, groupId: Long? = null): Student {
        return Student(
            id = id,
            firstName = "Student",
            lastName = id.toString(),
            xPosition = x,
            yPosition = y,
            groupId = groupId
        )
    }

    private fun dist(p1: Point, p2: Point): Float {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    private fun assertEqualsWithTolerance(expected: Int, actual: Int) {
        assertTrue("Expected $expected, but got $actual", expected == actual)
    }
}
