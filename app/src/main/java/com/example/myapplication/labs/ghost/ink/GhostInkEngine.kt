package com.example.myapplication.labs.ghost.ink

import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * GhostInkEngine: Manages persistent spatial annotations for the seating chart.
 *
 * Annotations are stored as [Stroke] objects in a 4000x4000 logical coordinate space,
 * ensuring they remain correctly positioned and scaled regardless of the device's
 * screen resolution or the current zoom/pan state of the seating chart.
 */
class GhostInkEngine {

    /**
     * Represents a single continuous drawing gesture.
     *
     * @property points List of [Offset] coordinates in logical 4000x4000 space.
     * @property color The color of the stroke (future-proofing for multiple ink colors).
     * @property timestamp When the stroke was created.
     */
    data class Stroke(
        val points: List<Offset>,
        val color: Long = 0xFF00E5FF, // Default Neon Cyan
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _strokes = MutableStateFlow<List<Stroke>>(emptyList())
    val strokes: StateFlow<List<Stroke>> = _strokes.asStateFlow()

    private var currentStrokePoints = mutableListOf<Offset>()

    /**
     * Starts a new stroke at the given logical coordinate.
     */
    fun startStroke(point: Offset) {
        currentStrokePoints = mutableListOf(point)
    }

    /**
     * Appends a new point to the current active stroke.
     */
    fun continueStroke(point: Offset) {
        // BOLT: Basic distance-based thinning to prevent point explosion
        if (currentStrokePoints.isEmpty()) {
            currentStrokePoints.add(point)
            return
        }

        val lastPoint = currentStrokePoints.last()
        val distanceSq = (point.x - lastPoint.x) * (point.x - lastPoint.x) +
                         (point.y - lastPoint.y) * (point.y - lastPoint.y)

        if (distanceSq > 25f) { // 5-pixel logical distance threshold
            currentStrokePoints.add(point)
        }
    }

    /**
     * Finalizes the current stroke and adds it to the persistent list.
     */
    fun finishStroke() {
        if (currentStrokePoints.size > 1) {
            val newStroke = Stroke(points = currentStrokePoints.toList())
            _strokes.value = _strokes.value + newStroke
        }
        currentStrokePoints = mutableListOf()
    }

    /**
     * Clears all annotations from the canvas.
     */
    fun clearAll() {
        _strokes.value = emptyList()
        currentStrokePoints = mutableListOf()
    }

    /**
     * Removes the most recent stroke.
     */
    fun undo() {
        if (_strokes.value.isNotEmpty()) {
            _strokes.value = _strokes.value.dropLast(1)
        }
    }
}
