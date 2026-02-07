package com.example.myapplication.util

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import java.util.concurrent.ConcurrentHashMap

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
