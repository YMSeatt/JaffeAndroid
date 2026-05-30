package com.example.myapplication.labs.ghost.mood

import com.example.myapplication.data.BehaviorEvent
import org.junit.Assert.*
import org.junit.Test

class GhostMoodEngineTest {

    @Test
    fun testCalculateStudentMoods_Calm() {
        val students = listOf(1L)
        val behaviorLogs = mapOf(1L to emptyList<BehaviorEvent>())
        val moods = GhostMoodEngine.calculateStudentMoods(students, behaviorLogs, emptyMap(), emptyMap())

        assertEquals(1, moods.size)
        assertEquals(GhostMoodEngine.MoodState.CALM, moods[0].state)
        assertEquals(0.1f, moods[0].intensity, 0.01f)
        assertEquals(0f, moods[0].valence, 0.01f)
    }

    @Test
    fun testCalculateStudentMoods_Turbulent() {
        val students = listOf(1L)
        val now = System.currentTimeMillis()
        val behaviorLogs = mapOf(
            1L to listOf(
                BehaviorEvent(1, 1L, "Negative (Talking)", now - 1000, null)
            )
        )
        val moods = GhostMoodEngine.calculateStudentMoods(students, behaviorLogs, emptyMap(), emptyMap())

        assertEquals(GhostMoodEngine.MoodState.TURBULENT, moods[0].state)
        assertTrue(moods[0].valence < 0f)
    }

    @Test
    fun testCalculateStudentMoods_Energetic() {
        val students = listOf(1L)
        val now = System.currentTimeMillis()
        val behaviorLogs = mapOf(
            1L to listOf(
                BehaviorEvent(1, 1L, "Positive (Participation)", now - 1000, null),
                BehaviorEvent(2, 1L, "Positive (Helpful)", now - 2000, null),
                BehaviorEvent(3, 1L, "Positive (Focused)", now - 3000, null)
            )
        )
        val moods = GhostMoodEngine.calculateStudentMoods(students, behaviorLogs, emptyMap(), emptyMap())

        assertEquals(GhostMoodEngine.MoodState.ENERGETIC, moods[0].state)
        assertTrue(moods[0].valence > 0f)
    }

    @Test
    fun testCalculateClassroomMood_Aggregate() {
        val studentMoods = listOf(
            GhostMoodEngine.StudentMood(1L, GhostMoodEngine.MoodState.TURBULENT, 0.8f, -0.8f),
            GhostMoodEngine.StudentMood(2L, GhostMoodEngine.MoodState.TURBULENT, 0.9f, -0.9f),
            GhostMoodEngine.StudentMood(3L, GhostMoodEngine.MoodState.CALM, 0.2f, 0f)
        )

        val classroomMood = GhostMoodEngine.calculateClassroomMood(studentMoods)

        assertEquals(GhostMoodEngine.MoodState.TURBULENT, classroomMood.aggregateState)
        assertTrue(classroomMood.stability < 1.0f)
    }
}
