package com.example.myapplication.util

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

fun safeParseColor(colorString: String): Color {
    if (colorString.isBlank()) {
        return Color.White
    }
    return try {
        Color(colorString.toColorInt())
    } catch (e: IllegalArgumentException) {
        Color.White
    }
}
