package com.example.myapplication.util

import android.graphics.Typeface as AndroidTypeface // Alias for android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.text.font.toFontFamily

/**
 * Retrieves a curated list of common font families available on the Android system.
 *
 * This list is used to populate font selection menus in the application's settings.
 * While a comprehensive list of all system fonts is not easily accessible across all
 * Android versions, this collection covers the most widely used and compatible faces.
 *
 * @return A distinct, sorted list of font family names.
 */
fun getAvailableFontFamilies(): List<String> {
    return listOf(
        "Default",
        "Sans-serif",
        "Serif",
        "Monospace",
        "Cursive",
        "Default",
        "sans-serif-light",
        "sans-serif-condensed",
        "sans-serif-medium",
        "sans-serif-black",
        "casual",
        "serif-monospace",
        "monospace",
        "cursive",
        "fantasy"
    ).distinct().sorted()
}

/**
 * Maps a font family name to its corresponding Jetpack Compose [FontFamily] object.
 *
 * @param fontName The name of the font family (e.g., "Monospace", "Serif").
 * @return The associated [FontFamily], defaulting to [FontFamily.Default] if the name is not recognized.
 */
fun getFontFamily(fontName: String): FontFamily {
    return when (fontName) {
        "Sans-serif" -> FontFamily.SansSerif
        "Serif" -> FontFamily.Serif
        "Monospace" -> FontFamily.Monospace
        "Cursive" -> FontFamily.Cursive
        else -> FontFamily.Default
    }
}
