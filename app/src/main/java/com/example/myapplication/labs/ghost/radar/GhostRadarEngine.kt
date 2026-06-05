package com.example.myapplication.labs.ghost.radar

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.sqrt

/**
 * GhostRadarEngine: Localized spatiotemporal behavioral resonance analysis.
 *
 * This engine calculates behavioral "pings" for a specific spatial area on the
 * seating chart. It's used by the Ghost Radar UI to visualize the density
 * and intensity of recent behaviors around a student or focal point.
 */
object GhostRadarEngine {
    private const val RADAR_RADIUS = 500f

    /**
     * Calculates the local resonance at a given coordinate.
     * Resonance is driven by the volume and valence of recent behavioral events
     * within the [RADAR_RADIUS].
     *
     * @param targetX The X coordinate in logical canvas space (4000x4000).
     * @param targetY The Y coordinate in logical canvas space.
     * @param students The current list of student UI items.
     * @param behaviorLogsByStudent Map of student IDs to their behavioral history.
     * @return A resonance value from 0.0 (quiet) to 1.0 (highly active/turbulent).
     */
    fun calculateLocalResonance(
        targetX: Float,
        targetY: Float,
        students: List<StudentUiItem>,
        behaviorLogsByStudent: Map<Long, List<BehaviorEvent>>,
        timeWindowMs: Long = 24 * 60 * 60 * 1000L // Default 24 hours
    ): Float {
        val now = System.currentTimeMillis()
        val cutoff = now - timeWindowMs
        var totalIntensity = 0f
        var nodeCount = 0

        for (student in students) {
            val dx = student.xPosition.value - targetX
            val dy = student.yPosition.value - targetY
            val distance = sqrt(dx * dx + dy * dy)

            if (distance <= RADAR_RADIUS) {
                nodeCount++
                val logs = behaviorLogsByStudent[student.id.toLong()] ?: continue

                // Distance weight (inverse square)
                val distanceFactor = 1.0f - (distance / RADAR_RADIUS)

                for (event in logs) {
                    if (event.timestamp < cutoff) break // BOLT: DESC sorted logs

                    val eventWeight = when {
                        event.type.contains("Negative", ignoreCase = true) -> 0.4f
                        event.type.contains("Positive", ignoreCase = true) -> 0.2f
                        else -> 0.1f
                    }

                    // Time decay
                    val timeFactor = (event.timestamp - cutoff).toFloat() / timeWindowMs.toFloat()
                    totalIntensity += eventWeight * distanceFactor * timeFactor
                }
            }
        }

        if (nodeCount == 0) return 0f

        // Normalize intensity (clamped at 1.0)
        return (totalIntensity / 2.0f).coerceIn(0f, 1f)
    }
}
