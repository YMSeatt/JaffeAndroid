package com.example.myapplication.labs.ghost.shell

import com.example.myapplication.data.BehaviorEvent
import kotlin.math.max
import kotlin.math.min

/**
 * GhostShellEngine: Calculates real-time classroom "Health" and "Pulse" metrics.
 *
 * This engine analyzes the sliding window of behavioral logs to drive the visual
 * behavior of the [GhostShellLayer] and its AGSL [GhostShellShader].
 */
object GhostShellEngine {
    /**
     * The sliding window duration (5 minutes) used to focus on current classroom energy.
     */
    private const val WINDOW_MS = 300_000L

    /**
     * ShellMetrics: A data container for synthesized classroom dynamics.
     *
     * @property healthIndex A normalized value [0.0..1.0] where 1.0 represents a purely positive
     * atmosphere and 0.0 represents high behavioral friction.
     * @property pulseFrequency A frequency value in Hz used to drive the visual "heartbeat" of the dock.
     * @property totalActivity The raw count of behavioral events within the current sliding window.
     * @property positiveRatio The percentage of positive logs relative to total activity.
     */
    data class ShellMetrics(
        val healthIndex: Float,
        val pulseFrequency: Float,
        val totalActivity: Int,
        val positiveRatio: Float
    )

    /**
     * BOLT ⚡ Optimization: Single-pass calculation to derive health and pulse metrics.
     *
     * This method implements a high-performance analysis of the behavioral log stream.
     * By using a manual index-based loop and avoiding functional operators, it achieves
     * zero object allocations and sub-millisecond execution times.
     *
     * ### Algorithm:
     * 1. **Temporal Filtering**: Only events within the [WINDOW_MS] window are considered.
     * 2. **Balance Calculation**: Health is derived from the net difference between
     *    positive and negative events, normalized around a 0.5 center.
     * 3. **Frequency Scaling**: Pulse frequency scales linearly with activity volume,
     *    capped at 4.0 Hz to prevent visual overwhelming.
     *
     * @param behaviorLogs The raw list of behavioral events from the database.
     * @return A [ShellMetrics] object containing the synthesized classroom state.
     */
    fun calculateMetrics(behaviorLogs: List<BehaviorEvent>): ShellMetrics {
        val now = System.currentTimeMillis()
        var positiveCount = 0
        var negativeCount = 0
        var totalInWindow = 0

        for (i in behaviorLogs.indices) {
            val log = behaviorLogs[i]
            if (now - log.timestamp < WINDOW_MS) {
                totalInWindow++
                if (log.type.contains("Positive", ignoreCase = true)) {
                    positiveCount++
                } else if (log.type.contains("Negative", ignoreCase = true)) {
                    negativeCount++
                }
            }
        }

        // Health Index: Derived from the balance of positive vs negative logs.
        // If no logs, defaults to 0.7 (Stable).
        val healthIndex = if (totalInWindow == 0) 0.7f else {
            val balance = (positiveCount - negativeCount).toFloat()
            val normalized = 0.5f + (balance / max(totalInWindow, 1)) * 0.5f
            normalized.coerceIn(0.0f, 1.0f)
        }

        // Pulse Frequency: Scales with total activity frequency.
        val freq = 0.5f + (totalInWindow / 20f).coerceAtMost(3.5f)

        return ShellMetrics(
            healthIndex = healthIndex,
            pulseFrequency = freq,
            totalActivity = totalInWindow,
            positiveRatio = if (totalInWindow == 0) 0.5f else positiveCount.toFloat() / totalInWindow
        )
    }
}
