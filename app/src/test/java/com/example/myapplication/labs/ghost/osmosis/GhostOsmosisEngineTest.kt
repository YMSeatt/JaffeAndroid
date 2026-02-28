package com.example.myapplication.labs.ghost.osmosis

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostOsmosisEngineTest {

    @Test
    fun testCalculateStudentPotentials_EmptyLogs() {
        val (k, b) = GhostOsmosisEngine.calculateStudentPotentials(emptyList(), emptyList(), emptyList())
        assertEquals(0.5f, k, 0.01f)
        assertEquals(0.0f, b, 0.01f)
    }

    @Test
    fun testCalculateStudentPotentials_Positive() {
        val behavior = listOf(
            BehaviorEvent(studentId = 1L, type = "Positive Participation", timestamp = System.currentTimeMillis(), comment = null),
            BehaviorEvent(studentId = 1L, type = "Good Work", timestamp = System.currentTimeMillis(), comment = null)
        )
        val quizzes = listOf(
            QuizLog(studentId = 1L, quizName = "Test", markValue = 10.0, markType = "Test", maxMarkValue = 10.0, loggedAt = System.currentTimeMillis(), comment = null, marksData = "{}", numQuestions = 10)
        )
        val (k, b) = GhostOsmosisEngine.calculateStudentPotentials(behavior, quizzes, emptyList())

        // k = (1.0 (quiz) + 0.5 (hwork fallback)) / 2 = 0.75
        assertEquals(0.75f, k, 0.01f)
        // b = (2 - 0) / 2 = 1.0
        assertEquals(1.0f, b, 0.01f)
    }

    @Test
    fun testCalculateOsmosis_GradientGeneration() {
        val students = listOf(
            GhostOsmosisEngine.OsmoticNode(1, 100f, 100f, 0.9f, 0.8f),
            GhostOsmosisEngine.OsmoticNode(2, 200f, 200f, 0.3f, -0.5f)
        )

        val gradients = GhostOsmosisEngine.calculateOsmosis(students, gridSize = 10)

        // Ensure some gradients are generated
        assertTrue(gradients.isNotEmpty())

        // First gradient point (close to student 1)
        val first = gradients.first()
        assertTrue(first.potential > 0f)
    }

    @Test
    fun testAnalyzeOsmoticBalance_ParityWithPython() {
        // Mock data from Python ghost_osmosis_analyzer.py __main__
        val students = listOf(
            GhostOsmosisEngine.OsmoticNode(1, 500f, 500f, 0.9f, 0.8f),  // High performing mentor
            GhostOsmosisEngine.OsmoticNode(2, 600f, 600f, 0.4f, 0.1f),  // Nearby student (Diffusion zone)
            GhostOsmosisEngine.OsmoticNode(3, 3000f, 3000f, 0.7f, -0.5f) // Isolated student with friction
        )

        val analysis = GhostOsmosisEngine.analyzeOsmoticBalance(students)

        // Python results for this mock data:
        // total_interactions: 1 (only student 1 and 2 are within 1000 units)
        // dist: sqrt(100^2 + 100^2) = 141.42
        // k_diff: abs(0.9 - 0.4) = 0.5
        // b_diff: abs(0.8 - 0.1) = 0.7
        // weight: exp(-(141.42^2) / (2 * 400^2)) = exp(-20000 / 320000) = exp(-0.0625) ≈ 0.9394
        // total_diffusion: (0.5 + 0.7) * 0.9394 = 1.12728
        // avg_diffusion: 1.12728 / 1 = 1.12728
        // balance_score: max(0.0, 1.0 - (1.12728 * 2.0)) = 0.0
        // status: HIGH_GRADIENT (since balanceScore < 0.4)

        assertEquals(1, analysis.totalInteractions)
        assertEquals(0f, analysis.balanceScore, 0.01f)
        assertEquals(GhostOsmosisEngine.OsmosisStatus.HIGH_GRADIENT, analysis.status)
        assertEquals(1.127f, analysis.avgDiffusionDelta, 0.01f)
    }

    @Test
    fun testGenerateOsmosisReport() {
        val analysis = GhostOsmosisEngine.OsmosisAnalysis(
            status = GhostOsmosisEngine.OsmosisStatus.EQUILIBRIUM,
            balanceScore = 0.95f,
            totalInteractions = 12,
            avgDiffusionDelta = 0.025f
        )

        val report = GhostOsmosisEngine.generateOsmosisReport(analysis, timestamp = "2027-10-10 10:10:10")

        assertTrue(report.contains("# 👻 GHOST OSMOSIS: NEURAL DIFFUSION ANALYSIS"))
        assertTrue(report.contains("Classroom Status:** EQUILIBRIUM"))
        assertTrue(report.contains("Osmotic Balance Score:** 95.0%"))
        assertTrue(report.contains("Timestamp:** 2027-10-10 10:10:10"))
        assertTrue(report.contains("Active Diffusion Zones: 12"))
        assertTrue(report.contains("Avg Diffusion Delta:   0.025"))
        assertTrue(report.contains("The classroom has reached a state of neural equilibrium."))
    }
}
