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
    private const val WINDOW_MS = 300_000L // 5 minute sliding window

    data class ShellMetrics(
        val healthIndex: Float,    // 0.0 (Critical) to 1.0 (Optimal)
        val pulseFrequency: Float, // 0.5 (Slow) to 4.0 (Hyper-active)
        val totalActivity: Int,
        val positiveRatio: Float
    )

    /**
     * BOLT ⚡ Optimization: Single-pass calculation to derive health and pulse.
     * Uses manual loops to avoid list churn.
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
