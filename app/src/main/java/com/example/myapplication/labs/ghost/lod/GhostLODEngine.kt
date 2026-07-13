package com.example.myapplication.labs.ghost.lod

/**
 * GhostLODEngine: Adaptive Detail Engine for high-performance seating chart rendering.
 *
 * This engine manages the "Level of Detail" (LOD) for UI components based on the
 * current viewport scale (zoom level). It allows the system to shed expensive
 * rendering tasks (like drawing multiple text layers or complex icons) when
 * items are too small to be clearly legible, preserving GPU cycles and frame budget.
 */
object GhostLODEngine {

    enum class DetailLevel {
        /** API 33+ High-fidelity rendering with all experimental layers and shaders. */
        CRITICAL,

        /** Standard detail level: all text and basic behavior logs visible. */
        FULL,

        /** Compact view: hide behavior logs, keep names and status icons. */
        COMPACT,

        /** Minimal view: show only student initials or colored indicators. */
        MINIMAL
    }

    /**
     * Calculates the appropriate [DetailLevel] based on the current canvas scale.
     *
     * Heuristics:
     * - > 1.5x: Critical (Zoomed in for inspection)
     * - 0.8x to 1.5x: Full (Normal interaction)
     * - 0.4x to 0.8x: Compact (Macroscopic overview)
     * - < 0.4x: Minimal (Bird's eye view)
     *
     * @param scale The current zoom scale of the seating chart.
     * @return The calculated [DetailLevel].
     */
    fun calculateLOD(scale: Float): DetailLevel {
        return when {
            scale > 1.5f -> DetailLevel.CRITICAL
            scale >= 0.8f -> DetailLevel.FULL
            scale >= 0.4f -> DetailLevel.COMPACT
            else -> DetailLevel.MINIMAL
        }
    }
}
