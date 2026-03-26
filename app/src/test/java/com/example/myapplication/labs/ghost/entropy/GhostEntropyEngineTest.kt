package com.example.myapplication.labs.ghost.entropy

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.QuizLog
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDateTime

class GhostEntropyEngineTest {

    @Test
    fun `calculateBehaviorEntropy returns zero for empty logs`() {
        val entropy = GhostEntropyEngine.calculateBehaviorEntropy(emptyMap(), 0)
        assertThat(entropy).isEqualTo(0f)
    }

    @Test
    fun `calculateBehaviorEntropy returns zero for single type of logs`() {
        val typeCounts = mapOf("Positive" to 2)
        val entropy = GhostEntropyEngine.calculateBehaviorEntropy(typeCounts, 2)
        assertThat(entropy).isEqualTo(0f)
    }

    @Test
    fun `calculateBehaviorEntropy returns higher value for diverse logs`() {
        val typeCounts = mapOf("Type A" to 1, "Type B" to 1, "Type C" to 1)
        val entropy = GhostEntropyEngine.calculateBehaviorEntropy(typeCounts, 3)
        assertThat(entropy).isGreaterThan(0f)
        assertThat(entropy).isAtMost(1.0f)
    }

    @Test
    fun `calculateAcademicVariance returns zero for single quiz`() {
        val variance = GhostEntropyEngine.calculateAcademicVariance(1.0, 1.0, 1)
        assertThat(variance).isEqualTo(0f)
    }

    @Test
    fun `calculateAcademicVariance returns higher value for inconsistent scores`() {
        val sum = 1.0 + 0.2 // 10/10 and 2/10
        val sumSq = (1.0 * 1.0) + (0.2 * 0.2)
        val variance = GhostEntropyEngine.calculateAcademicVariance(sum, sumSq, 2)
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
