package com.example.myapplication.labs.ghost.kinetic

import androidx.compose.animation.core.exponentialDecay
import androidx.compose.ui.geometry.Offset

/**
 * GhostKineticEngine: Physics-based momentum engine for classroom interactions.
 *
 * This engine provides constants and specifications for "Kinetic Flick" gestures,
 * allowing student icons to glide with friction across the seating chart.
 *
 * BOLT ⚡ Optimization:
 * 1. Uses [exponentialDecay] for smooth, natural deceleration without manual math.
 * 2. Employs a high friction coefficient to ensure students don't glide off-canvas.
 */
object GhostKineticEngine {

    /** The friction coefficient for kinetic movement. Higher values mean faster stopping. */
    const val KINETIC_FRICTION = 1.1f

    /** The minimum velocity (pixels/sec) required to trigger a kinetic glide. */
    const val VELOCITY_THRESHOLD = 200f

    /**
     * Provides a decay animation specification based on the [KINETIC_FRICTION].
     */
    fun <T> decaySpec() = exponentialDecay<T>(
        frictionMultiplier = KINETIC_FRICTION,
        absVelocityThreshold = 0.1f
    )

    /**
     * Calculates the projected destination of a flick gesture.
     * Useful for pre-calculating grid snapping or boundary constraints.
     */
    fun calculateTargetOffset(initialVelocity: Offset, initialPosition: Offset): Offset {
        // exponentialDecay formula: target = start + velocity / friction
        // Note: This is a simplified approximation.
        val targetX = initialPosition.x + (initialVelocity.x / KINETIC_FRICTION)
        val targetY = initialPosition.y + (initialVelocity.y / KINETIC_FRICTION)
        return Offset(targetX, targetY)
    }
}
