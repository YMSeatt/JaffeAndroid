package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.abs

/**
 * GhostPulseEngine: Logic for calculating "Neural Resonance" between students.
 *
 * This engine analyzes behavioral event history to find "Temporal Clusters" —
 * students who receive logs at similar times. These clusters are visualized
 * as synchronized pulses in the [GhostPulseLayer].
 */
object GhostPulseEngine {

    data class ResonancePulse(
        val studentId: Long,
        val intensity: Float,
        val color: Triple<Float, Float, Float>, // RGB
        val startTime: Long
    )

    // BOLT: Static color constants to eliminate Triple allocations in the analysis loop.
    private val COLOR_POSITIVE = Triple(0.2f, 1.0f, 0.4f) // Green
    private val COLOR_NEGATIVE = Triple(1.0f, 0.2f, 0.2f) // Red
    private val COLOR_NEUTRAL = Triple(0.2f, 0.6f, 1.0f)  // Blue

    /**
     * Identifies students who should pulse based on recent activity and calculates
     * their "Resonance" (sync level).
     *
     * BOLT: Removed redundant 'students' list and its O(N) association overhead.
     * Verification of student existence is now deferred to the high-performance
     * Layer drawing loop which maintains its own mapped cache.
     *
     * @param events All behavior events.
     * @param currentTime The current system time.
     * @param windowMillis The time window (ms) to consider for "simultaneous" events.
     */
    fun calculateResonance(
        events: List<BehaviorEvent>,
        currentTime: Long,
        windowMillis: Long = 5000L
    ): List<ResonancePulse> {
        if (events.isEmpty()) return emptyList()

        // BOLT: Events are sorted by timestamp DESC. Use early exit to avoid O(N) scan.
        val recentEvents = mutableListOf<BehaviorEvent>()
        for (event in events) {
            if (currentTime - event.timestamp < windowMillis) {
                recentEvents.add(event)
            } else if (event.timestamp < currentTime - windowMillis) {
                break
            }
        }

        if (recentEvents.isEmpty()) return emptyList()

        val pulses = mutableListOf<ResonancePulse>()

        recentEvents.forEach { event ->
            // Determine color based on behavior type
            val color = when {
                event.type.contains("Positive", ignoreCase = true) -> COLOR_POSITIVE
                event.type.contains("Negative", ignoreCase = true) -> COLOR_NEGATIVE
                else -> COLOR_NEUTRAL
            }

            // Intensity based on how recent the event is
            val age = currentTime - event.timestamp
            val intensity = (1.0f - (age.toFloat() / windowMillis)).coerceIn(0f, 1f)

            pulses.add(
                ResonancePulse(
                    studentId = event.studentId,
                    intensity = intensity,
                    color = color,
                    startTime = event.timestamp
                )
            )
        }

        return pulses
    }
}
