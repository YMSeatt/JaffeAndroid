package com.example.myapplication.labs.ghost.palette

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.max
import kotlin.math.min

/**
 * GhostPaletteEngine: The neural color harmony calculator for Ghost Lab.
 *
 * This engine provides logic for generating harmonic color schemes (Complementary, Triadic)
 * from a base HSV input. It is used by the Ghost Palette UI to provide interactive
 * color feedback during gesture-driven selection.
 */
object GhostPaletteEngine {

    /**
     * Calculates a harmonic color palette based on the selected harmony type.
     *
     * @param h Hue (0.0 to 360.0)
     * @param s Saturation (0.0 to 1.0)
     * @param v Value (0.0 to 1.0)
     * @param harmony The harmony type ("COMPLEMENTARY", "TRIADIC").
     * @return A Pair containing the Primary and Secondary colors.
     */
    fun calculateHarmony(h: Float, s: Float, v: Float, harmony: String): Pair<Color, Color> {
        val primary = Color.hsv(h, s, v)
        val secondaryHue = when (harmony) {
            "COMPLEMENTARY" -> (h + 180f) % 360f
            "TRIADIC" -> (h + 120f) % 360f
            else -> h
        }
        val secondary = Color.hsv(secondaryHue, s, v)
        return Pair(primary, secondary)
    }

    /**
     * Converts RGB to HSV.
     */
    fun rgbToHsv(color: Color): FloatArray {
        val r = color.red
        val g = color.green
        val b = color.blue

        val max = max(r, max(g, b))
        val min = min(r, min(g, b))
        val delta = max - min

        var h = 0f
        if (delta != 0f) {
            h = when (max) {
                r -> (g - b) / delta
                g -> 2f + (b - r) / delta
                else -> 4f + (r - g) / delta
            }
            h *= 60f
            if (h < 0) h += 360f
        }

        val s = if (max == 0f) 0f else delta / max
        val v = max

        return floatArrayOf(h, s, v)
    }
}
