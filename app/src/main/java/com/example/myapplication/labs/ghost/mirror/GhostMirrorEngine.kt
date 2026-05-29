package com.example.myapplication.labs.ghost.mirror

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.graphicsLayer

/**
 * GhostMirrorEngine: Manages spatial transformations for the seating chart.
 *
 * This engine allows teachers to "flip" their perspective of the classroom
 * (e.g., viewing from the back of the room vs. the front).
 *
 * ### Transformations:
 * - **0°**: Default view (Teacher's perspective).
 * - **180°**: Flipped view (Student's perspective).
 * - **Mirror**: Horizontal flip.
 */
class GhostMirrorEngine {

    enum class Perspective(val rotationZ: Float, val scaleX: Float) {
        NORMAL(0f, 1f),
        FLIPPED(180f, 1f),
        MIRROR(0f, -1f),
        FLIPPED_MIRROR(180f, -1f)
    }

    private val _perspective = mutableStateOf(Perspective.NORMAL)
    val perspective: State<Perspective> = _perspective

    /**
     * Toggles to the next perspective in the sequence.
     */
    fun togglePerspective() {
        _perspective.value = when (_perspective.value) {
            Perspective.NORMAL -> Perspective.FLIPPED
            Perspective.FLIPPED -> Perspective.MIRROR
            Perspective.MIRROR -> Perspective.FLIPPED_MIRROR
            Perspective.FLIPPED_MIRROR -> Perspective.NORMAL
        }
    }

    /**
     * Resets the perspective to default.
     */
    fun reset() {
        _perspective.value = Perspective.NORMAL
    }
}
