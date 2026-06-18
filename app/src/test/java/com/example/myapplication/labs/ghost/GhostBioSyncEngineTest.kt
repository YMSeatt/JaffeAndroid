package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Student
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostBioSyncEngineTest {

    @Test
    fun testCalculateBioStates() {
        val students = listOf(
            Student(id = 1, firstName = "John", lastName = "Doe")
        )
        val now = System.currentTimeMillis()
        val logs = mapOf(
            1L to listOf(
                BehaviorEvent(id = 1, studentId = 1, type = "Positive Participation", timestamp = now - 1000)
            )
        )

        val states = GhostBioSyncEngine.calculateBioStates(students, logs, now)
        val johnState = states[1L]!!

        // Base 0.5 + 0.1 (recent) = 0.6
        assertEquals(0.6f, johnState.vitality, 0.01f)
        assertEquals(0.0f, johnState.stress, 0.01f)
    }

    @Test
    fun testCalculateHarmony() {
        val states = mapOf(
            1L to GhostBioSyncEngine.BioStatus(1L, 0.8f, 0.1f, 0.5f),
            2L to GhostBioSyncEngine.BioStatus(2L, 0.4f, 0.5f, 0.5f)
        )

        val harmony = GhostBioSyncEngine.calculateHarmony(states)

        // avgVitality = 0.6, avgStress = 0.3
        // harmony = 0.6 * (1.0 - 0.3) = 0.6 * 0.7 = 0.42
        assertEquals(0.42f, harmony, 0.01f)
    }
}
