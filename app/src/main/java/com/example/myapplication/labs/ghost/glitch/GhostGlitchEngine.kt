package com.example.myapplication.labs.ghost.glitch

import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.sqrt

/**
 * GhostGlitchEngine: Calculates "Spatial Tension" and "Neural Glitch" intensity.
 *
 * This engine detects conflicts in the seating chart, such as student icons
 * overlapping or being placed too close to each other.
 */
object GhostGlitchEngine {

    private const val CRITICAL_DISTANCE = 160f // Distance where glitching starts
    private const val MAX_TENSION_DISTANCE = 40f // Distance for max glitching

    /**
     * Calculates the glitch intensity for the current classroom state.
     *
     * BOLT ⚡ Optimization: Uses squared distance for preliminary checks and
     * manual loops to avoid iterator overhead.
     */
    fun calculateGlitchIntensity(students: List<StudentUiItem>): Float {
        if (students.size < 2) return 0f

        var maxTension = 0f
        val critDistSq = CRITICAL_DISTANCE * CRITICAL_DISTANCE

        // O(N^2) conflict detection - acceptable for typical classroom sizes (N < 40)
        for (i in 0 until students.size) {
            val s1 = students[i]
            val x1 = s1.xPosition.value
            val y1 = s1.yPosition.value

            for (j in i + 1 until students.size) {
                val s2 = students[j]
                val dx = x1 - s2.xPosition.value
                val dy = y1 - s2.yPosition.value
                val distSq = dx * dx + dy * dy

                if (distSq < critDistSq) {
                    val dist = sqrt(distSq)
                    val tension = 1f - ((dist - MAX_TENSION_DISTANCE) / (CRITICAL_DISTANCE - MAX_TENSION_DISTANCE))
                    if (tension > maxTension) {
                        maxTension = tension
                    }
                }
            }
        }

        return maxTension.coerceIn(0f, 1f)
    }
}
