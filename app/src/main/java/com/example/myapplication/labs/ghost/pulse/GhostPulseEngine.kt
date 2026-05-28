package com.example.myapplication.labs.ghost.pulse

import com.example.myapplication.data.BehaviorEvent

/**
 * GhostPulseEngine: Logic for calculating "Temporal Resonance" between students.
 *
 * This engine analyzes behavioral event history to find "Temporal Clusters" —
 * students who receive logs at similar times. These clusters are visualized
 * as synchronized pulses in the [GhostPulseLayer].
 *
 * ### Temporal Resonance Metaphor:
 * Much like ripples in a pond, behavioral events create "waves" of data. When
 * multiple students receive logs simultaneously, their waves overlap, creating
 * resonance patterns that highlight classroom-wide behavioral trends.
 *
 * BOLT ⚡ Optimizations:
 * 1. **O(Recent) Analysis**: Uses DESC-sorted logs with early-exit loops to identify
 *    resonant pulses in sub-millisecond time.
 * 2. **Manual Indexing**: Replaces functional iterators (forEach, filter) with manual
 *    for-loops to avoid Iterator object churn.
 */
object GhostPulseEngine {

    /**
     * Represents a detected behavioral resonance for a specific student.
     */
    data class ResonancePulse(
        val studentId: Long,
        val intensity: Float,
        val r: Float,
        val g: Float,
        val b: Float,
        val startTime: Long
    )

    /**
     * Identifies students who should pulse based on recent activity and calculates
     * their resonance intensity.
     *
     * @param events All behavior events (expected to be sorted DESC by timestamp).
     * @param currentTime The current system time.
     * @param windowMillis The time window (ms) to consider for "simultaneous" events.
     * @return A list of active pulses.
     */
    fun calculateResonance(
        events: List<BehaviorEvent>,
        currentTime: Long,
        windowMillis: Long = 5000L
    ): List<ResonancePulse> {
        if (events.isEmpty()) return emptyList()

        val pulses = ArrayList<ResonancePulse>()
        val eventSize = events.size

        // BOLT: Manual index-based loop with early exit for O(Recent) performance.
        for (i in 0 until eventSize) {
            val event = events[i]
            val age = currentTime - event.timestamp

            if (age in 0 until windowMillis) {
                if (pulses.size < 40) {
                    // Determine color based on behavior type
                    val (r, g, b) = when {
                        event.type.contains("Positive", ignoreCase = true) -> {
                            Triple(0.2f, 1.0f, 0.4f)
                        }
                        event.type.contains("Negative", ignoreCase = true) -> {
                            Triple(1.0f, 0.2f, 0.2f)
                        }
                        else -> {
                            Triple(0.2f, 0.6f, 1.0f)
                        }
                    }

                    // Intensity based on how recent the event is
                    val intensity = (1.0f - (age.toFloat() / windowMillis)).coerceIn(0f, 1f)

                    pulses.add(ResonancePulse(
                        studentId = event.studentId,
                        intensity = intensity,
                        r = r,
                        g = g,
                        b = b,
                        startTime = event.timestamp
                    ))
                }
            } else if (event.timestamp < currentTime - windowMillis) {
                // Logs are sorted DESC, so we can stop here.
                break
            }
        }

        return pulses
    }
}
