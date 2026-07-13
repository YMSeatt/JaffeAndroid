package com.example.myapplication.labs.ghost.glyph

import androidx.compose.ui.geometry.Offset
import kotlin.math.atan2
import kotlin.math.PI

/**
 * GhostGlyphEngine: Neural Gesture Recognition Engine.
 *
 * This engine classifies a sequence of touch points into pedagogical "Glyphs"
 * using a directional-sequence analysis algorithm.
 */
object GhostGlyphEngine {

    enum class GlyphType {
        POSITIVE,   // "✔" (Checkmark/V)
        NEGATIVE,   // "✖" (Single-stroke Alpha or cross-like gesture)
        ACADEMIC,   // "▲" (Triangle)
        UNKNOWN
    }

    /**
     * Recognizes a glyph from a sequence of touch points.
     *
     * @param points The sequence of [Offset] points captured during the gesture.
     * @return The recognized [GlyphType].
     */
    fun recognizeGlyph(points: List<Offset>): GlyphType {
        if (points.size < 10) return GlyphType.UNKNOWN

        // 1. Simplify points (Resample)
        val simplified = resample(points, 20)

        // 2. Convert to directions (8-point compass)
        val directions = getDirectionSequence(simplified)

        // 3. Match against templates
        return when {
            isCheckmark(directions) -> GlyphType.POSITIVE
            isTriangle(directions) -> GlyphType.ACADEMIC
            isAlphaLoop(directions) -> GlyphType.NEGATIVE
            else -> GlyphType.UNKNOWN
        }
    }

    private fun resample(points: List<Offset>, n: Int): List<Offset> {
        val pathLength = points.zipWithNext { a, b -> (b - a).getDistance() }.sum()
        val interval = pathLength / (n - 1)
        val resampled = mutableListOf<Offset>()
        resampled.add(points.first())

        var D = 0.0f
        var i = 1
        while (i < points.size) {
            val d = (points[i] - points[i - 1]).getDistance()
            if (D + d >= interval) {
                val q = points[i - 1] + (points[i] - points[i - 1]) * ((interval - D) / d)
                resampled.add(q)
                D = 0.0f
            } else {
                D += d
                i++
            }
        }
        if (resampled.size < n) resampled.add(points.last())
        return resampled
    }

    private fun getDirectionSequence(points: List<Offset>): List<Int> {
        return points.zipWithNext { a, b ->
            val angle = atan2(b.y - a.y, b.x - a.x) * 180 / PI
            // Normalize to 0-360
            val normalized = (angle + 360) % 360
            // Map to 8 directions (0: E, 1: SE, 2: S, 3: SW, 4: W, 5: NW, 6: N, 7: NE)
            ((normalized + 22.5) / 45).toInt() % 8
        }.distinctConsecutive()
    }

    private fun isCheckmark(directions: List<Int>): Boolean {
        // Expected: SE-ish then NE-ish
        // e.g., [1, 7] or [2, 0]
        if (directions.size != 2) return false
        val d1 = directions[0]
        val d2 = directions[1]
        // d1 should be downward/right (1, 2)
        // d2 should be upward/right (7, 0, 1)
        return (d1 in 0..2) && (d2 in 6..7 || d2 == 0) && (d2 != d1)
    }

    private fun isTriangle(directions: List<Int>): Boolean {
        // Expected: 3 distinct directions forming a loop
        // e.g., SE, W, NE (1, 4, 7)
        if (directions.size < 3) return false
        val unique = directions.distinct()
        return unique.size >= 3 && isClosed(directions)
    }

    private fun isAlphaLoop(directions: List<Int>): Boolean {
        // Expected: A loop that crosses itself.
        // For PoC, let's detect a high-curvature sequence (0-1-2-3-4-5-6-7)
        return directions.size >= 5 && isClosed(directions)
    }

    private fun isClosed(directions: List<Int>): Boolean {
        // A simple heuristic for closure: sum of angle changes is roughly 360
        // Or start/end proximity (though directions don't tell us that)
        // Let's just check if we have 4+ directions forming a rotation
        var turnSum = 0
        for (i in 0 until directions.size - 1) {
            var diff = directions[i + 1] - directions[i]
            if (diff > 4) diff -= 8
            if (diff < -4) diff += 8
            turnSum += diff
        }
        return Math.abs(turnSum) >= 6 // ~270 to 360 degrees
    }

    private fun <T> List<T>.distinctConsecutive(): List<T> {
        if (isEmpty()) return emptyList()
        val result = mutableListOf<T>()
        result.add(this[0])
        for (i in 1 until size) {
            if (this[i] != this[i - 1]) {
                result.add(this[i])
            }
        }
        return result
    }
}
