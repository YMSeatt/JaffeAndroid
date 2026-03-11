package com.example.myapplication.labs.ghost.pulsar

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.PI
import kotlin.math.sin

/**
 * GhostPulsarEngine: Logic for calculating "Harmonic Synchronicity" in the classroom.
 *
 * This engine analyzes behavioral event frequency for each student to determine
 * their individual "Classroom Rhythm". Students with similar rhythms exhibit
 * interference patterns in the [GhostPulsarLayer].
 */
object GhostPulsarEngine {

    data class HarmonicState(
        val studentId: Long,
        val phase: Float,      // 0.0 to 1.0 (Current position in the cycle)
        val frequency: Float,  // Oscillations per minute
        val amplitude: Float   // Strength of the harmonic (based on log density)
    )

    /**
     * Calculates the harmonic state for each student based on their behavior history.
     *
     * @param students List of students to analyze.
     * @param events Historical behavior logs.
     * @param currentTime Current system time.
     */
    fun calculateHarmonics(
        students: List<StudentUiItem>,
        events: List<BehaviorEvent>,
        currentTime: Long
    ): List<HarmonicState> {
        if (students.isEmpty()) return emptyList()

        // BOLT: Group events by studentId for O(N + L) efficiency.
        val eventsByStudent = events.groupBy { it.studentId }
        val windowMillis = 600_000L // 10 minute window for frequency analysis

        return students.map { student ->
            val studentLogs = eventsByStudent[student.id.toLong()] ?: emptyList()

            // Filter logs within the sliding window
            val recentLogs = studentLogs.filter { currentTime - it.timestamp < windowMillis }

            // frequency = logs per minute
            val frequency = (recentLogs.size.toFloat() / (windowMillis / 60_000f)).coerceIn(0.1f, 10f)

            // amplitude = density of logs (scaled)
            val amplitude = (recentLogs.size.toFloat() / 5f).coerceIn(0f, 1.5f)

            // phase = (currentTime * frequency) normalized to 0..1
            // frequency is logs/min, so logs/(60*1000) ms
            val phase = ((currentTime % 60_000L).toFloat() / 60_000f * frequency) % 1.0f

            HarmonicState(
                studentId = student.id.toLong(),
                phase = phase,
                frequency = frequency,
                amplitude = amplitude
            )
        }
    }
}
