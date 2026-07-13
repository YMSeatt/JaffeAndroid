package com.example.myapplication.labs.ghost.origami

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * GhostOrigamiEngine: Manages the folding state for the Neural Origami experiment.
 *
 * This engine tracks the "folding progress" (0.0 to 1.0), where:
 * - 0.0: Flat (Normal seating chart)
 * - 1.0: Fully folded (revealing the "Backstage" data layer)
 *
 * It provides logic for toggling the fold and calculating the reactive rotation
 * and translation offsets required for the origami transition.
 */
class GhostOrigamiEngine {
    private val _foldProgress = MutableStateFlow(0f)

    /**
     * Normalized folding progress (0.0 to 1.0).
     */
    val foldProgress = _foldProgress.asStateFlow()

    /**
     * Toggles the origami fold state.
     */
    fun toggleFold() {
        _foldProgress.value = if (_foldProgress.value > 0.5f) 0f else 1f
    }

    /**
     * Sets the fold progress directly (useful for gesture-driven folding).
     */
    fun setFoldProgress(progress: Float) {
        _foldProgress.value = progress.coerceIn(0f, 1f)
    }

    /**
     * Resets the fold to flat state.
     */
    fun reset() {
        _foldProgress.value = 0f
    }
}
