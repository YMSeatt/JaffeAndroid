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
 *
 * ### Performance Context (BOLT):
 * To maintain 60fps performance during radar sweeps, this engine utilizes a
 * background-safe analysis pipeline that leverages chronologically DESC-sorted
 * behavior logs to enable early-exit termination.
 */
object GhostRadarEngine {
    /**
     * The maximum spatial influence distance for a behavior log.
     * Calibrated for the 4000x4000 logical canvas.
     */
    private const val RADAR_RADIUS = 500f
    private const val RADAR_RADIUS_SQ = RADAR_RADIUS * RADAR_RADIUS
    private const val INV_RADAR_RADIUS = 1f / RADAR_RADIUS

    /**
     * Calculates the local resonance at a given coordinate.
     * Resonance is driven by the volume and valence of recent behavioral events
     * within the [RADAR_RADIUS].
     *
     * ### Algorithm Details:
     * - **Spatial Decay**: Linear influence falloff from 1.0 at distance 0 to 0.0 at [RADAR_RADIUS].
     * - **Temporal Decay**: Linear influence falloff over the provided [timeWindowMs].
     * - **Valence Weighting**: Negative behaviors (0.4f) contribute more to resonance than
     *   Positive (0.2f) or Neutral (0.1f) behaviors.
     *
     * @param targetX The X coordinate in logical canvas space (4000x4000).
     * @param targetY The Y coordinate in logical canvas space.
     * @param students The current list of student UI items to check for proximity.
     * @param behaviorLogsByStudent Map of student IDs to their behavioral history (must be DESC sorted).
     * @param timeWindowMs The sliding window for analysis (default 24h).
     * @return A normalized resonance value from 0.0 (quiet) to 1.0 (clamped).
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
        val invTimeWindow = 1f / timeWindowMs.toFloat()
        var totalIntensity = 0f
        var nodeCount = 0

        // BOLT: Allocation-free manual index loop
        for (i in 0 until students.size) {
            val student = students[i]
            // BOLT: Hoist MutableState reads
            val sx = student.xPosition.value
            val sy = student.yPosition.value

            val dx = sx - targetX
            val dy = sy - targetY
            val distSq = dx * dx + dy * dy

            // BOLT: Fast proximity pruning using squared distance
            if (distSq <= RADAR_RADIUS_SQ) {
                nodeCount++
                val logs = behaviorLogsByStudent[student.id.toLong()] ?: continue

                // Distance weight (linear decay)
                val distance = sqrt(distSq)
                val distanceFactor = 1.0f - (distance * INV_RADAR_RADIUS)

                for (j in 0 until logs.size) {
                    val event = logs[j]
                    if (event.timestamp < cutoff) break // BOLT: DESC sorted logs

                    val eventWeight = when {
                        event.type.contains("Negative", ignoreCase = true) -> 0.4f
                        event.type.contains("Positive", ignoreCase = true) -> 0.2f
                        else -> 0.1f
                    }

                    // Time decay
                    val timeFactor = (event.timestamp - cutoff).toFloat() * invTimeWindow
                    totalIntensity += eventWeight * distanceFactor * timeFactor
                }
            }
        }

        if (nodeCount == 0) return 0f

        // Normalize intensity (clamped at 1.0)
        return (totalIntensity * 0.5f).coerceIn(0f, 1f)
    }
}
