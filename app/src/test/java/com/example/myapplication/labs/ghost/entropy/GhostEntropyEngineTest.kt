package com.example.myapplication.labs.ghost.entropy

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.QuizLog
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDateTime

class GhostEntropyEngineTest {

    @Test
    fun `calculateBehaviorEntropy returns zero for empty logs`() {
        val entropy = GhostEntropyEngine.calculateBehaviorEntropy(emptyList())
        assertThat(entropy).isEqualTo(0f)
    }

    @Test
    fun `calculateBehaviorEntropy returns zero for single type of logs`() {
        val logs = listOf(
            BehaviorEvent(studentId = 1, type = "Positive", timestamp = System.currentTimeMillis(), comment = null),
            BehaviorEvent(studentId = 1, type = "Positive", timestamp = System.currentTimeMillis(), comment = null)
        )
        val entropy = GhostEntropyEngine.calculateBehaviorEntropy(logs)
        assertThat(entropy).isEqualTo(0f)
    }

    @Test
    fun `calculateBehaviorEntropy returns higher value for diverse logs`() {
        val logs = listOf(
            BehaviorEvent(studentId = 1, type = "Type A", timestamp = System.currentTimeMillis(), comment = null),
            BehaviorEvent(studentId = 1, type = "Type B", timestamp = System.currentTimeMillis(), comment = null),
            BehaviorEvent(studentId = 1, type = "Type C", timestamp = System.currentTimeMillis(), comment = null)
        )
        val entropy = GhostEntropyEngine.calculateBehaviorEntropy(logs)
        assertThat(entropy).isGreaterThan(0f)
        assertThat(entropy).isAtMost(1.0f)
    }

    @Test
    fun `calculateAcademicVariance returns zero for single quiz`() {
        val quizLogs = listOf(
            QuizLog(studentId = 1, quizName = "Quiz 1", markValue = 10.0, markType = "Points", maxMarkValue = 10.0, loggedAt = System.currentTimeMillis(), comment = null, marksData = "{}", numQuestions = 10)
        )
        val variance = GhostEntropyEngine.calculateAcademicVariance(quizLogs)
        assertThat(variance).isEqualTo(0f)
    }

    @Test
    fun `calculateAcademicVariance returns higher value for inconsistent scores`() {
        val quizLogs = listOf(
            QuizLog(studentId = 1, quizName = "Quiz 1", markValue = 10.0, markType = "Points", maxMarkValue = 10.0, loggedAt = System.currentTimeMillis(), comment = null, marksData = "{}", numQuestions = 10),
            QuizLog(studentId = 1, quizName = "Quiz 2", markValue = 2.0, markType = "Points", maxMarkValue = 10.0, loggedAt = System.currentTimeMillis(), comment = null, marksData = "{}", numQuestions = 10)
        )
        val variance = GhostEntropyEngine.calculateAcademicVariance(quizLogs)
        assertThat(variance).isGreaterThan(0f)
        assertThat(variance).isAtMost(1.0f)
    }

    @Test
    fun `generateEntropyReport contains key metrics`() {
        val analysis = GhostEntropyEngine.EntropyAnalysis(0.85f, 2, listOf(1L, 2L))
        val report = GhostEntropyEngine.generateEntropyReport(analysis, "2024-01-01 12:00:00")

        assertThat(report).contains("85.0%")
        assertThat(report).contains("High-Entropy Nodes: 2")
        assertThat(report).contains("CRITICAL")
    }
}
