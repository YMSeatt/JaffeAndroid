package com.example.myapplication.utils

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

fun safeParseColor(colorString: String?): Color? {
    return try {
        colorString?.let { Color(it.toColorInt()) }
    } catch (e: IllegalArgumentException) {
        Log.e("ColorUtils", "Failed to parse color: $colorString", e)
        null
    }
}