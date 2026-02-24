package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostSpectraEngineTest {

    @Test
    fun testCalculateSpectralDensity_EmptyLogs() {
        val density = GhostSpectraEngine.calculateSpectralDensity(emptyList())
        assertEquals(0.2f, density, 0.01f)
    }

    @Test
    fun testCalculateSpectralDensity_HighDiversity() {
        val logs = listOf(
            BehaviorEvent(studentId = 1L, type = "Type 1", timestamp = 1000L, comment = null),
            BehaviorEvent(studentId = 1L, type = "Type 2", timestamp = 1000L, comment = null),
            BehaviorEvent(studentId = 1L, type = "Type 3", timestamp = 1000L, comment = null),
            BehaviorEvent(studentId = 1L, type = "Type 4", timestamp = 1000L, comment = null),
            BehaviorEvent(studentId = 1L, type = "Type 5", timestamp = 1000L, comment = null)
        )
        val density = GhostSpectraEngine.calculateSpectralDensity(logs)
        // diversityRatio = 5/10 = 0.5. volumeFactor = 5/100 = 0.05
        // expected = 0.5 * 0.7 + 0.05 * 0.3 = 0.35 + 0.015 = 0.365
        assertEquals(0.365f, density, 0.01f)
    }

    @Test
    fun testCalculateAgitation_RecentNegative() {
        val now = System.currentTimeMillis()
        val logs = listOf(
            BehaviorEvent(studentId = 1L, type = "Negative Behavior", timestamp = now - 1000, comment = null),
            BehaviorEvent(studentId = 1L, type = "Negative Behavior", timestamp = now - 2000, comment = null)
        )
        val agitation = GhostSpectraEngine.calculateAgitation(logs)
        // count = 2. expected = 2/10 = 0.2
        assertEquals(0.2f, agitation, 0.01f)
    }

    @Test
    fun testCalculateAgitation_OldNegative() {
        val now = System.currentTimeMillis()
        val oldTime = now - (48 * 60 * 60 * 1000L) // 48 hours ago
        val logs = listOf(
            BehaviorEvent(studentId = 1L, type = "Negative Behavior", timestamp = oldTime, comment = null)
        )
        val agitation = GhostSpectraEngine.calculateAgitation(logs)
        // Should be ignored as it's outside 24h window
        assertEquals(0.1f, agitation, 0.01f)
    }

    @Test
    fun testAnalyzeStudentSpectra_Infrared() {
        val logs = listOf(
            BehaviorEvent(studentId = 1L, type = "Negative", timestamp = 1000L, comment = null),
            BehaviorEvent(studentId = 1L, type = "Negative", timestamp = 1000L, comment = null),
            BehaviorEvent(studentId = 1L, type = "Positive", timestamp = 1000L, comment = null)
        )
        val spectra = GhostSpectraEngine.analyzeStudentSpectra(1L, logs)
        // shift = 2/3 = 0.66 > 0.5 -> INFRARED
        assertEquals(GhostSpectraEngine.SpectralState.INFRARED, spectra.state)
        assertEquals(3f / 20f, spectra.intensity, 0.01f)
        assertEquals(2f / 3f, spectra.shift, 0.01f)
    }

    @Test
    fun testAnalyzeStudentSpectra_Ultraviolet() {
        val logs = mutableListOf<BehaviorEvent>()
        repeat(18) {
            logs.add(BehaviorEvent(studentId = 1L, type = "Positive", timestamp = 1000L, comment = null))
        }
        val spectra = GhostSpectraEngine.analyzeStudentSpectra(1L, logs)
        // intensity = 18/20 = 0.9 > 0.8. shift = 0 -> ULTRAVIOLET
        assertEquals(GhostSpectraEngine.SpectralState.ULTRAVIOLET, spectra.state)
        assertEquals(0.9f, spectra.intensity, 0.01f)
        assertEquals(0f, spectra.shift, 0.01f)
    }

    @Test
    fun testAnalyzeStudentSpectra_Stable() {
        val logs = listOf(
            BehaviorEvent(studentId = 1L, type = "Positive", timestamp = 1000L, comment = null),
            BehaviorEvent(studentId = 1L, type = "Negative", timestamp = 1000L, comment = null)
        )
        val spectra = GhostSpectraEngine.analyzeStudentSpectra(1L, logs)
        // shift = 1/2 = 0.5 (not > 0.5). intensity = 2/20 = 0.1 -> STABLE
        assertEquals(GhostSpectraEngine.SpectralState.STABLE, spectra.state)
    }

    @Test
    fun testGenerateSpectraReport() {
        val spectra = listOf(
            GhostSpectraEngine.StudentSpectra(1L, 0.9f, 0f, GhostSpectraEngine.SpectralState.ULTRAVIOLET),
            GhostSpectraEngine.StudentSpectra(2L, 0.2f, 0.8f, GhostSpectraEngine.SpectralState.INFRARED)
        )
        val names = mapOf(1L to "Alice", 2L to "Bob")
        val report = GhostSpectraEngine.generateSpectraReport(spectra, names, 0.5f, 0.2f)

        assertTrue(report.contains("# 👻 GHOST SPECTRA: NEURAL ANALYSIS REPORT"))
        assertTrue(report.contains("Dispersion Index: 0.50"))
        assertTrue(report.contains("Agitation Level:  0.20"))
        assertTrue(report.contains("Alice: ULTRAVIOLET (High Engagement)"))
        assertTrue(report.contains("Bob: INFRARED (At Risk)"))
        assertTrue(report.contains("(I:0.90, S:0.00)"))
        assertTrue(report.contains("(I:0.20, S:0.80)"))
    }
}
