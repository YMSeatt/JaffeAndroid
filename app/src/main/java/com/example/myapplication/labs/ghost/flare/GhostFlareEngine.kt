package com.example.myapplication.labs.ghost.flare

import androidx.compose.runtime.mutableStateListOf
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostFlareEngine: Manages the lifecycle and triggering of "Neural Flares" — high-intensity
 * behavioral celebratory events.
 *
 * A "Flare" is triggered when a student reaches a behavioral milestone, such as a burst
 * of positive participation or high academic performance. This engine handles the detection
 * logic, de-duplication, and temporal decay of these visual states.
 */
class GhostFlareEngine {

    /**
     * Represents the internal state of an active visual flare.
     *
     * @property studentId The unique identifier of the student associated with the flare.
     * @property x The horizontal coordinate in the 4000x4000 logical canvas space.
     * @property y The vertical coordinate in the 4000x4000 logical canvas space.
     * @property intensity The visual brightness multiplier (0.0 to 1.0).
     * @property life The lifecycle progress, where 1.0 is freshly spawned and 0.0 is fully decayed.
     * @property type Categorizes the flare: 0 for Positive (Gold/Cyan) and 1 for Academic (Purple).
     */
    data class FlareState(
        val studentId: Long,
        var x: Float,
        var y: Float,
        var intensity: Float,
        var life: Float,
        val type: Int
    )

    /**
     * An observable list of currently active flares, used by the Compose UI layer for rendering.
     */
    val flares = mutableStateListOf<FlareState>()

    /**
     * Scans the current list of students to identify new behavioral milestones.
     *
     * This method implements a high-performance heuristic check:
     * 1. **Quantity**: A student must have at least 3 logs in the current session.
     * 2. **Valence**: The `behaviorBalance` must be highly positive (> 0.8f).
     *
     * @param students The current list of UI-transformed student items.
     * @see BOLT Performance Optimization: Uses manual index loops to avoid iterator overhead.
     */
    fun checkMilestones(students: List<StudentUiItem>) {
        val count = students.size
        for (i in 0 until count) {
            val student = students[i]
            val logs = student.behaviorLogs

            // Heuristic: Trigger flare if 3+ logs exist and the balance is highly positive (>0.8)
            // or if they have many logs in total.
            if (logs.size >= 3 && student.behaviorBalance.value > 0.8f) {
                triggerFlare(student)
            }
        }
    }

    /**
     * Instantiates and registers a new flare for a student.
     *
     * This method includes a de-duplication check to prevent visual stacking (clutter).
     * A student cannot trigger a new flare if they already have one that is less than
     * 50% decayed (`life > 0.5f`).
     *
     * @param student The student item that reached the milestone.
     */
    private fun triggerFlare(student: StudentUiItem) {
        // Prevent duplicate flares for the same student if one is already active and "young"
        val existing = flares.find { it.studentId == student.id.toLong() && it.life > 0.5f }
        if (existing != null) return

        flares.add(
            FlareState(
                studentId = student.id.toLong(),
                x = student.xPosition.value,
                y = student.yPosition.value,
                intensity = 1.0f,
                life = 1.0f,
                type = if (student.behaviorBalance.value > 0.9f) 0 else 1
            )
        )
    }

    /**
     * Progresses the temporal decay of all active flares.
     *
     * This should be called on every animation frame (e.g., from a `LaunchedEffect`
     * synchronized with the Compose animation clock). Flares are automatically
     * removed from the list once their `life` reaches 0.
     *
     * @param dt The delta time since the last update in seconds.
     */
    fun update(dt: Float) {
        val iterator = flares.iterator()
        while (iterator.hasNext()) {
            val flare = iterator.next()
            flare.life -= dt * 0.8f // Fade out over ~1.25 seconds
            if (flare.life <= 0f) {
                iterator.remove()
            }
        }
    }
}
