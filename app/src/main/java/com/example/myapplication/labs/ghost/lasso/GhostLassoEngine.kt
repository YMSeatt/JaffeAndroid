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
        val size = polygon.size
        if (size < 3) return false

        var isInside = false
        var j = size - 1
        for (i in 0 until size) {
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
     *
     * BOLT: Optimized to avoid iterator churn and redundant list allocations.
     * Corrected resampling logic to handle segments longer than interval.
     */
    fun resample(points: List<Offset>, n: Int): List<Offset> {
        val size = points.size
        if (size == 0) return emptyList()
        if (size == 1) return List(n) { points[0] }

        var pathLength = 0f
        for (i in 0 until size - 1) {
            pathLength += (points[i + 1] - points[i]).getDistance()
        }

        if (pathLength == 0f) return List(n) { points[0] }

        val interval = pathLength / (n - 1)
        val resampled = ArrayList<Offset>(n)
        resampled.add(points[0])

        var targetDist = interval
        var currentDist = 0f
        for (i in 0 until size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            val d = (p2 - p1).getDistance()

            while (currentDist + d >= targetDist && resampled.size < n) {
                val ratio = (targetDist - currentDist) / d
                resampled.add(p1 + (p2 - p1) * ratio)
                targetDist += interval
            }
            currentDist += d
        }

        while (resampled.size < n) {
            resampled.add(points[size - 1])
        }
        return resampled
    }

    /**
     * Resamples the input points directly into a pre-allocated buffer.
     *
     * BOLT: Zero-allocation high-frequency resampling for 60fps rendering.
     * Corrected resampling logic to handle segments longer than interval.
     */
    fun resampleToBuffer(points: List<Offset>, n: Int, buffer: FloatArray) {
        val size = points.size
        if (size == 0 || buffer.size < n * 2) return

        if (size == 1) {
            val px = points[0].x
            val py = points[0].y
            for (j in 0 until n) {
                buffer[j * 2] = px
                buffer[j * 2 + 1] = py
            }
            return
        }

        var pathLength = 0f
        for (i in 0 until size - 1) {
            pathLength += (points[i + 1] - points[i]).getDistance()
        }

        if (pathLength == 0f) {
            val px = points[0].x
            val py = points[0].y
            for (j in 0 until n) {
                buffer[j * 2] = px
                buffer[j * 2 + 1] = py
            }
            return
        }

        val interval = pathLength / (n - 1)
        buffer[0] = points[0].x
        buffer[1] = points[0].y
        var count = 1

        var targetDist = interval
        var currentDist = 0f
        for (i in 0 until size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            val dx = p2.x - p1.x
            val dy = p2.y - p1.y
            val d = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

            while (currentDist + d >= targetDist && count < n) {
                val ratio = (targetDist - currentDist) / d
                buffer[count * 2] = p1.x + dx * ratio
                buffer[count * 2 + 1] = p1.y + dy * ratio
                count++
                targetDist += interval
            }
            currentDist += d
        }

        val lastX = points[size - 1].x
        val lastY = points[size - 1].y
        while (count < n) {
            buffer[count * 2] = lastX
            buffer[count * 2 + 1] = lastY
            count++
        }
    }
}
