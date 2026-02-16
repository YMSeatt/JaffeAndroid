package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import kotlin.math.sqrt

/**
 * GhostSpectraEngine: The "Prism Logic" for Ghost Spectra.
 *
 * This engine calculates the "Spectral Density" and "Classroom Agitation"
 * based on student behavioral history. These metrics drive the visual
 * intensity and dispersion of the spectral shader.
 */
object GhostSpectraEngine {

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
}
