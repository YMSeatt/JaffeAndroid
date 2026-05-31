package com.example.myapplication.labs.ghost.link

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GhostLinkEngineTest {

    @Test
    fun testDossierContent() {
        val studentId = 123L
        val studentName = "John Doe"
        val dossier = GhostLinkEngine.generateNeuralDossier(studentId, studentName)

        // PRIVACY HARDENING: Should NOT contain internal ID and should mask full name
        assertThat(dossier).doesNotContain("123")
        assertThat(dossier).contains("J. DOE")
        assertThat(dossier).doesNotContain("JOHN DOE")
        assertThat(dossier).contains("Status")
        assertThat(dossier).contains("Neural Metrics")
    }

    @Test
    fun `generateNeuralDossier metrics are within expected ranges`() {
        val dossier = GhostLinkEngine.generateNeuralDossier(1L, "Test Student")

        // Extract metrics using regex.
        val resonanceMatch = Regex("""Cognitive Resonance \| (\d+)%""").find(dossier)
        val fluxMatch = Regex("""Collaborative Flux \| ([\d.]+)""").find(dossier)
        val entropyMatch = Regex("""Academic Entropy \| ([\d.]+)""").find(dossier)
        val gravityMatch = Regex("""Social Gravity \| (\d+) mG""").find(dossier)

        assertThat(resonanceMatch).isNotNull()
        assertThat(fluxMatch).isNotNull()
        assertThat(entropyMatch).isNotNull()
        assertThat(gravityMatch).isNotNull()

        val resonance = resonanceMatch?.groupValues?.get(1)?.toInt() ?: 0
        val flux = fluxMatch?.groupValues?.get(1)?.toDouble() ?: 0.0
        val entropy = entropyMatch?.groupValues?.get(1)?.toDouble() ?: 0.0
        val gravity = gravityMatch?.groupValues?.get(1)?.toInt() ?: 0

        assertThat(resonance).isAtLeast(70)
        assertThat(resonance).isAtMost(95)

        assertThat(flux).isAtLeast(0.5)
        assertThat(flux).isAtMost(0.9)

        assertThat(entropy).isAtLeast(0.1)
        assertThat(entropy).isAtMost(0.4)

        assertThat(gravity).isAtLeast(10)
        assertThat(gravity).isAtMost(50)
    }

    @Test
    fun `generateNeuralDossier is deterministic for the same student ID`() {
        val studentId = 456L
        val dossier1 = GhostLinkEngine.generateNeuralDossier(studentId, "Jane Smith")
        val dossier2 = GhostLinkEngine.generateNeuralDossier(studentId, "Jane Smith")

        // We skip the timestamp for comparison
        val content1 = dossier1.lines().filterNot { it.contains("Analysis Timestamp") }.joinToString("\n")
        val content2 = dossier2.lines().filterNot { it.contains("Analysis Timestamp") }.joinToString("\n")

        assertThat(content1).isEqualTo(content2)
    }

    @Test
    fun `calculateLinks identifies proximity-based pairs`() {
        val nodes = listOf(
            GhostLinkEngine.StudentNode(1L, 1000f, 1000f, 0.9f),
            GhostLinkEngine.StudentNode(2L, 1100f, 1100f, 0.85f), // Close and similar
            GhostLinkEngine.StudentNode(3L, 3000f, 3000f, 0.1f),   // Far
            GhostLinkEngine.StudentNode(4L, 1050f, 1050f, 0.2f)    // Close but disparate balance
        )

        val links = GhostLinkEngine.calculateLinks(nodes)

        assertThat(links).isNotEmpty()
        assertThat(links.any { it.studentA == 1L && it.studentB == 2L }).isTrue()

        // Strongest should be 1-2
        assertThat(links[0].studentA).isEqualTo(1L)
        assertThat(links[0].studentB).isEqualTo(2L)
    }
}
