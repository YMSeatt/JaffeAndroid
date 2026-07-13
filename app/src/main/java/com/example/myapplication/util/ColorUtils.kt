package com.example.myapplication.util

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import java.util.concurrent.ConcurrentHashMap

/**
 * ColorUtils: High-performance color management utilities.
 *
 * This utility provides a bridge between the application's Hex-based storage model
 * and the Jetpack Compose [Color] system.
 *
 * ### BOLT (Performance-Obsessed) Strategy:
 * Because the seating chart frequently updates (e.g., during student movement or
 * live quiz sessions), color parsing can become a significant source of CPU overhead
 * and string allocation churn. This utility utilizes a [ConcurrentHashMap] cache
 * to ensure that identical color strings are only parsed once, maintaining 60fps
 * responsiveness across the UI.
 */

/**
 * Global cache for parsed colors to minimize string-to-int conversion overhead.
 */
private val colorCache = ConcurrentHashMap<String, Color>()

/**
 * Safely parses a Hex color string into a Compose [Color] object.
 *
 * This function is optimized with a [ConcurrentHashMap] cache to avoid repeated
 * expensive string parsing and [toColorInt] calls for the same color strings,
 * which is common during high-frequency UI updates.
 *
 * @param colorString The Hex color string (e.g., "#RRGGBB" or "#AARRGGBB").
 * @return The parsed [Color], or [Color.White] if parsing fails.
 */
fun safeParseColor(colorString: String): Color {
    if (colorString.isBlank()) {
        return Color.White
    }

    return colorCache.getOrPut(colorString) {
        try {
            Color(colorString.toColorInt())
        } catch (e: IllegalArgumentException) {
            Color.White
        }
    }
}
