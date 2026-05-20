package com.example.myapplication.labs.ghost.util

/**
 * GhostActivityMetrics: A centralized data structure for recent student behavior.
 *
 * This class encapsulates metrics calculated over a specific time window (e.g., 10 minutes)
 * to avoid redundant O(L) scanning across multiple Ghost Lab engines.
 *
 * @property studentId The unique student ID.
 * @property posCount Number of positive behaviors in the window.
 * @property negCount Number of negative behaviors in the window.
 * @property recentCount Total number of behaviors in the window.
 * @property intensity Normalized intensity (0.0 to 1.0) based on log frequency.
 * @property polarity Net behavioral polarity (1.0 for positive, -1.0 for negative).
 */
data class GhostActivityMetrics(
    val studentId: Long,
    val posCount: Int,
    val negCount: Int,
    val recentCount: Int,
    val intensity: Float,
    val polarity: Float
)
