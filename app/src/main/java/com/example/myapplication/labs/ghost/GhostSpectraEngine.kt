package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import java.util.Locale
import kotlin.math.sqrt

/**
 * GhostSpectraEngine: The "Prism Logic" for Ghost Spectra.
 *
 * This engine calculates the "Spectral Density" and "Classroom Agitation"
 * based on student behavioral history. These metrics drive the visual
 * intensity and dispersion of the spectral shader.
 *
 * It also provides student-level spectroscopy analysis and report generation,
 * maintaining logical parity with `Python/ghost_spectra_analyzer.py`.
 */
object GhostSpectraEngine {

    /**
     * Categorization of a student's spectral state.
     */
    enum class SpectralState {
        STABLE,
        INFRARED,    // At Risk (High Negative Shift)
        ULTRAVIOLET  // High Engagement (High Intensity)
    }

    /**
     * Encapsulates the spectral analysis for a single student.
     */
    data class StudentSpectra(
        val studentId: Long,
        val intensity: Float,
        val shift: Float,
        val state: SpectralState
    )

    /**
     * Calculates the Spectral Density (Dispersion) of the classroom.
     * Higher density occurs when there is a high variance in student performance
     * or behavior, representing a "diffraction" of the class into different groups.
     *
     * @param behaviorLogs Historical behavioral events.
     * @return A normalized value (0.0 to 1.0) for the iDensity uniform.
     */
    fun calculateSpectralDensity(behaviorLogs: List<BehaviorEvent>): Float {
        if (behaviorLogs.isEmpty()) return 0.2f

        // Count different types of behaviors to measure "spectral diversity"
        val behaviorTypes = behaviorLogs.map { it.type }.distinct().size
        val totalLogs = behaviorLogs.size

        // More behavior types = higher dispersion (more "colors" in the class)
        val diversityRatio = (behaviorTypes.toFloat() / 10f).coerceAtMost(1.0f)

        // Log volume also contributes to "mass" and thus refractive density
        val volumeFactor = (totalLogs.toFloat() / 100f).coerceAtMost(1.0f)

        return (diversityRatio * 0.7f + volumeFactor * 0.3f).coerceIn(0.1f, 1.0f)
    }

    /**
     * Calculates the Classroom Agitation level.
     * Higher agitation occurs with recent negative behavioral events.
     *
     * @param behaviorLogs Historical behavioral events.
     * @return A normalized value (0.0 to 1.0) for the iAgitation uniform.
     */
    fun calculateAgitation(behaviorLogs: List<BehaviorEvent>): Float {
        if (behaviorLogs.isEmpty()) return 0.0f

        val now = System.currentTimeMillis()
        val recentWindow = 24 * 60 * 60 * 1000L // Last 24 hours

        val recentNegativeLogs = behaviorLogs.filter {
            it.timestamp > (now - recentWindow) && it.type.contains("Negative", ignoreCase = true)
        }

        if (recentNegativeLogs.isEmpty()) return 0.1f

        // Agitation scales with the frequency of negative events in the recent window
        val count = recentNegativeLogs.size
        return (count.toFloat() / 10f).coerceIn(0.1f, 1.0f)
    }

    /**
     * Performs spectroscopy analysis on a single student's behavioral history.
     * Parity-matched with the logic in `Python/ghost_spectra_analyzer.py`.
     *
     * @param studentId The ID of the student.
     * @param studentLogs The list of logs associated with this student.
     * @return A [StudentSpectra] object containing the analysis results.
     */
    fun analyzeStudentSpectra(studentId: Long, studentLogs: List<BehaviorEvent>): StudentSpectra {
        if (studentLogs.isEmpty()) {
            return StudentSpectra(studentId, 0f, 0f, SpectralState.STABLE)
        }

        val negativeLogs = studentLogs.filter { it.type.contains("Negative", ignoreCase = true) }

        // Normalized intensity based on log volume (Threshold: 20 logs = 1.0)
        val intensity = (studentLogs.size.toFloat() / 20f).coerceAtMost(1.5f)

        // Spectral shift (ratio of negative logs)
        val shift = negativeLogs.size.toFloat() / studentLogs.size.toFloat()

        val state = when {
            shift > 0.5f -> SpectralState.INFRARED
            intensity > 0.8f -> SpectralState.ULTRAVIOLET
            else -> SpectralState.STABLE
        }

        return StudentSpectra(studentId, intensity, shift, state)
    }

    /**
     * Generates a Markdown-formatted Neural Spectroscopy report.
     * Parity-matched with the output of `Python/ghost_spectra_analyzer.py`.
     *
     * @param studentSpectra A list of analyzed student spectra.
     * @param studentNames A map of student IDs to their display names.
     * @param globalDensity The classroom's spectral density metric.
     * @param globalAgitation The classroom's agitation metric.
     * @return A formatted Markdown string.
     */
    fun generateSpectraReport(
        studentSpectra: List<StudentSpectra>,
        studentNames: Map<Long, String>,
        globalDensity: Float,
        globalAgitation: Float
    ): String {
        val timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        )

        val report = StringBuilder()
        report.append("# 👻 GHOST SPECTRA: NEURAL ANALYSIS REPORT\n")
        report.append("**Timestamp:** $timestamp\n")
        report.append("**Classroom Size:** ${studentSpectra.size} nodes\n\n")

        report.append("## [SPECTRAL METRICS]\n")
        report.append("Dispersion Index: ${String.format(Locale.US, "%.2f", globalDensity)} (Refractive Variance)\n")
        report.append("Agitation Level:  ${String.format(Locale.US, "%.2f", globalAgitation)} (Neural Turbulence)\n\n")

        report.append("## [NODE SPECTROSCOPY]\n")
        studentSpectra.forEach { spectra ->
            val name = studentNames[spectra.studentId] ?: "Student ${spectra.studentId}"
            val status = when (spectra.state) {
                SpectralState.INFRARED -> "INFRARED (At Risk)"
                SpectralState.ULTRAVIOLET -> "ULTRAVIOLET (High Engagement)"
                SpectralState.STABLE -> "STABLE"
            }
            report.append(" - $name: $status (I:${String.format(Locale.US, "%.2f", spectra.intensity)}, S:${String.format(Locale.US, "%.2f", spectra.shift)})\n")
        }

        report.append("\n---\n*Generated by Ghost Spectra Analysis Bridge v1.0 (Experimental)*")
        return report.toString()
    }
}
