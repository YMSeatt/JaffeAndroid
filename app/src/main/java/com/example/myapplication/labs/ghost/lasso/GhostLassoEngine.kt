package com.example.myapplication.labs.ghost.lasso

import androidx.compose.ui.geometry.Offset

/**
 * GhostLassoEngine: High-performance spatial selection engine.
 *
 * It provides geometric utilities to determine if a point (student coordinate)
 * falls within a closed or semi-closed gesture path (Lasso).
 */
object GhostLassoEngine {

    /**
     * Determines if a point is inside a polygon defined by a set of points.
     * Implements the Ray Casting algorithm (Jordan Curve Theorem).
     *
     * BOLT: Optimized for 60fps interaction by using a single-pass traversal.
     */
    fun contains(polygon: List<Offset>, point: Offset): Boolean {
        if (polygon.size < 3) return false

        var isInside = false
        var j = polygon.size - 1
        for (i in polygon.indices) {
            val pi = polygon[i]
            val pj = polygon[j]

            if (((pi.y > point.y) != (pj.y > point.y)) &&
                (point.x < (pj.x - pi.x) * (point.y - pi.y) / (pj.y - pi.y) + pi.x)
            ) {
                isInside = !isInside
            }
            j = i
        }
        return isInside
    }

    /**
     * Resamples the input points to a fixed count [n] to ensure consistent
     * performance and logic processing.
     */
    fun resample(points: List<Offset>, n: Int): List<Offset> {
        if (points.isEmpty()) return emptyList()
        if (points.size == 1) return List(n) { points[0] }

        val pathLength = points.zipWithNext { a, b -> (b - a).getDistance() }.sum()
        if (pathLength == 0f) return List(n) { points[0] }

        val interval = pathLength / (n - 1)
        val resampled = mutableListOf<Offset>()
        resampled.add(points.first())

        var accumulatedDist = 0.0f
        var i = 1
        while (i < points.size && resampled.size < n) {
            val d = (points[i] - points[i - 1]).getDistance()
            if (accumulatedDist + d >= interval) {
                val q = points[i - 1] + (points[i] - points[i - 1]) * ((interval - accumulatedDist) / d)
                resampled.add(q)
                accumulatedDist = 0.0f
            } else {
                accumulatedDist += d
                i++
            }
        }

        while (resampled.size < n) {
            resampled.add(points.last())
        }
        return resampled.take(n)
    }
}
