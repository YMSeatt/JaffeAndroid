package com.example.myapplication.labs.ghost.navigator

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize

/**
 * GhostNavigatorEngine: Performance-optimized coordinate mapping for the mini-map.
 *
 * This engine handles the translation between the high-fidelity 4000x4000 logical
 * seating chart canvas and the normalized mini-map space.
 *
 * BOLT ⚡ Optimization: Uses pure mathematical functions without side effects to
 * ensure sub-millisecond calculation time, even during high-frequency zoom gestures.
 */
object GhostNavigatorEngine {
    /** The logical size of the seating chart world space. */
    const val LOGICAL_CANVAS_SIZE = 4000f

    /**
     * Calculates the current viewport's boundaries in normalized (0..1) logical space.
     *
     * @param scale The current zoom level of the seating chart.
     * @param offset The current pan offset in pixels.
     * @param containerSize The size of the screen/viewport in pixels.
     * @return A [Rect] representing the visible area in 0..1 range.
     */
    fun calculateNormalizedViewport(
        scale: Float,
        offset: Offset,
        containerSize: IntSize
    ): Rect {
        if (scale <= 0f) return Rect(0f, 0f, 1f, 1f)

        // Inverse transform: Logical = (Screen - Offset) / Scale
        val logicalLeft = (-offset.x) / scale
        val logicalTop = (-offset.y) / scale
        val logicalRight = (containerSize.width - offset.x) / scale
        val logicalBottom = (containerSize.height - offset.y) / scale

        return Rect(
            left = (logicalLeft / LOGICAL_CANVAS_SIZE).coerceIn(0f, 1f),
            top = (logicalTop / LOGICAL_CANVAS_SIZE).coerceIn(0f, 1f),
            right = (logicalRight / LOGICAL_CANVAS_SIZE).coerceIn(0f, 1f),
            bottom = (logicalBottom / LOGICAL_CANVAS_SIZE).coerceIn(0f, 1f)
        )
    }

    /**
     * Translates a tap on the normalized mini-map into a pan offset that centers
     * the seating chart on that location.
     *
     * @param normalizedTap The coordinates of the tap on the mini-map (0..1).
     * @param currentScale The current zoom level.
     * @param containerSize The size of the screen/viewport in pixels.
     * @return The new pan [Offset] to be applied to the seating chart.
     */
    fun calculateOffsetForTap(
        normalizedTap: Offset,
        currentScale: Float,
        containerSize: IntSize
    ): Offset {
        val targetLogicalX = normalizedTap.x * LOGICAL_CANVAS_SIZE
        val targetLogicalY = normalizedTap.y * LOGICAL_CANVAS_SIZE

        // Formula for centering: offset = ScreenCenter - (LogicalTarget * Scale)
        val newOffsetX = (containerSize.width / 2f) - (targetLogicalX * currentScale)
        val newOffsetY = (containerSize.height / 2f) - (targetLogicalY * currentScale)

        return Offset(newOffsetX, newOffsetY)
    }
}
