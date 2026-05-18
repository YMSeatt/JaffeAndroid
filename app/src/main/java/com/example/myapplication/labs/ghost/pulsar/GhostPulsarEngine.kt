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
 *
 * ### Rhythmic Model:
 * - **Frequency**: Calculated as logs-per-minute within a 10-minute window.
 * - **Phase**: Derived from the system clock and frequency, ensuring that
 *   synchronized students (those with similar frequencies) pulse in unison.
 * - **Interference**: When two student nodes have a frequency delta < 0.2,
 *   a "Harmonic Bond" is detected, triggering visual constructive interference.
 */
object GhostPulsarEngine {

    data class HarmonicState(
        val studentId: Long,
        val frequency: Float,  // Oscillations per minute
        val amplitude: Float   // Strength of the harmonic (based on log density)
    )

    /**
     * Represents a detected synchronicity bond between two students.
     */
    data class HarmonicBond(
        val studentAName: String,
        val studentBName: String,
        val sync: Float
    )

    /**
     * Calculates the harmonic state for each student based on their behavior history.
     *
     * ### Theoretical Basis:
     * Activity is modeled as a periodic wave where the frequency is driven by recent
     * behavioral density. This allows the system to visualize "Classroom Momentum."
     *
     * @param students List of students to analyze.
     * @param events Historical behavior logs.
     * @param currentTime Current system time (used for windowing).
     */
    fun calculateHarmonics(
        students: List<StudentUiItem>,
        events: List<BehaviorEvent>,
        currentTime: Long
    ): List<HarmonicState> {
        if (students.isEmpty()) return emptyList()

        // BOLT: Group events by studentId for O(N + L) efficiency.
        val eventsByStudent = events.groupBy { it.studentId }
        return calculateHarmonics(students, eventsByStudent, currentTime)
    }

    /**
     * BOLT: High-performance overload that accepts pre-grouped logs.
     */
    fun calculateHarmonics(
        students: List<StudentUiItem>,
        eventsByStudent: Map<Long, List<BehaviorEvent>>,
        currentTime: Long
    ): List<HarmonicState> {
        if (students.isEmpty()) return emptyList()

        // The 10-minute window (600,000ms) was chosen to capture short-term behavioral
        // "bursts" without being overly influenced by long-term historical averages.
        val windowMillis = 600_000L
        val results = ArrayList<HarmonicState>(students.size)

        // BOLT: Use manual index-based loop to eliminate iterator allocations.
        for (i in students.indices) {
            val student = students[i]
            val studentLogs = eventsByStudent[student.id.toLong()] ?: emptyList()

            // BOLT: Optimized count loop with early break.
            // Behavior logs are typically sorted DESC by timestamp in the database layer.
            var recentCount = 0
            for (j in studentLogs.indices) {
                val log = studentLogs[j]
                if (currentTime - log.timestamp < windowMillis) {
                    recentCount++
                } else {
                    // Logs are sorted DESC, so once we hit a log outside the window,
                    // all subsequent logs are also outside the window.
                    break
                }
            }

            // Frequency is calculated as logs-per-minute (LPM).
            // A higher LPM indicates a more "active" student in the current window.
            val frequency = (recentCount.toFloat() / (windowMillis / 60_000f)).coerceIn(0.1f, 10f)

            // Amplitude represents the "loudness" of the student's rhythm.
            // Calibrated such that 5 logs in 10 minutes (0.5 LPM) results in 1.0 amplitude.
            val amplitude = (recentCount.toFloat() / 5f).coerceIn(0f, 1.5f)

            results.add(
                HarmonicState(
                    studentId = student.id.toLong(),
                    frequency = frequency,
                    amplitude = amplitude
                )
            )
        }

        return results
    }

    /**
     * Detects synchronicity bonds between students with similar classroom rhythms.
     *
     * ### Parity:
     * Ported from `Python/ghost_pulsar_analyzer.py`. The 0.2 frequency delta threshold
     * represents a +/- 12% variance for a medium-activity student (1.5 LPM).
     */
    fun detectBonds(
        harmonics: List<HarmonicState>,
        studentNames: Map<Long, String>
    ): List<HarmonicBond> {
        val bonds = mutableListOf<HarmonicBond>()
        for (i in harmonics.indices) {
            for (j in i + 1 until harmonics.size) {
                val h1 = harmonics[i]
                val h2 = harmonics[j]
                val diff = kotlin.math.abs(h1.frequency - h2.frequency)

                // Sync threshold parity-matched with Python blueprint (diff < 0.2)
                if (diff < 0.2f) {
                    bonds.add(
                        HarmonicBond(
                            studentAName = studentNames[h1.studentId] ?: "Student ${h1.studentId}",
                            studentBName = studentNames[h2.studentId] ?: "Student ${h2.studentId}",
                            sync = 1.0f - diff
                        )
                    )
                }
            }
        }
        return bonds
    }

    /**
     * Generates a Markdown-formatted Classroom Rhythm report.
     *
     * ### Parity:
     * Parity-matched with the output of `Python/ghost_pulsar_analyzer.py` to ensure
     * that researchers see consistent data across desktop and mobile platforms.
     */
    fun generatePulsarReport(
        harmonics: List<HarmonicState>,
        bonds: List<HarmonicBond>,
        studentNames: Map<Long, String>,
        studentCount: Int,
        logCount: Int
    ): String {
        val report = StringBuilder()
        report.append("# \uD83D\uDC7B GHOST PULSAR: CLASSROOM RHYTHM ANALYSIS\n")
        report.append("Analyzing $studentCount students and $logCount logs...\n\n")

        report.append("| Student Name | Frequency (LPM) | Harmonic Amplitude |\n")
        report.append("| :--- | :--- | :--- |\n")

        // Sort by frequency DESC to find rhythm clusters (Python parity)
        harmonics.sortedByDescending { it.frequency }.forEach { h ->
            val name = studentNames[h.studentId] ?: "Student ${h.studentId}"
            report.append("| $name | ${String.format(java.util.Locale.US, "%.2f", h.frequency)} | ${String.format(java.util.Locale.US, "%.2f", h.amplitude)} |\n")
        }

        report.append("\n## --- Detected Harmonic Bonds (Synchronized Pairs) ---\n")
        if (bonds.isEmpty()) {
            report.append("No significant harmonic bonds detected in the current data stream.\n")
        } else {
            bonds.forEach { bond ->
                report.append("\uD83D\uDD17 Bond: ${bond.studentAName} <-> ${bond.studentBName} (Sync: ${String.format(java.util.Locale.US, "%.2f", bond.sync)})\n")
            }
        }

        report.append("\n---\n*Generated by Ghost Pulsar Analysis Bridge v1.0 (Experimental)*")
        return report.toString()
    }
}
