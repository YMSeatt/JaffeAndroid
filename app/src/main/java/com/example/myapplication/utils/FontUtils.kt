package com.example.myapplication.utils

import android.graphics.Typeface as AndroidTypeface // Alias for android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.text.font.toFontFamily

fun getAvailableFontFamilies(): List<String> {
    // This is a curated list of common font families.
    // A comprehensive list of all system fonts is not easily accessible across all Android versions.
    return listOf(
        "Default",
        "Sans-serif", // Changed from FontFamily.SansSerif.name
        "Serif",      // Changed from FontFamily.Serif.name
        "Monospace",  // Changed from FontFamily.Monospace.name
        "Cursive",    // Changed from FontFamily.Cursive.name
        "Default",    // Changed from FontFamily.Default.name (which is SansSerif)
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

fun getFontFamily(fontName: String): FontFamily {
    return when (fontName) {
        "Sans-serif" -> FontFamily.SansSerif
        "Serif" -> FontFamily.Serif
        "Monospace" -> FontFamily.Monospace
        "Cursive" -> FontFamily.Cursive
        else -> FontFamily.Default
    }
}