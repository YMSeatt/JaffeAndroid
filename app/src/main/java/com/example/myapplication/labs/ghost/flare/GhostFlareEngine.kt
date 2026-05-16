package com.example.myapplication.labs.ghost.flare

import androidx.compose.runtime.mutableStateListOf
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostFlareEngine: Manages the lifecycle and triggering of behavioral flares.
 *
 * A "Flare" is a high-intensity visual event triggered when a student reaches
 * a behavioral milestone (e.g., 3+ positive events in a short window).
 */
class GhostFlareEngine {

    data class FlareState(
        val studentId: Long,
        var x: Float,
        var y: Float,
        var intensity: Float,
        var life: Float, // 1.0 (spawn) to 0.0 (death)
        val type: Int // 0: Positive (Cyan/Gold), 1: Academic (Purple)
    )

    val flares = mutableStateListOf<FlareState>()

    /**
     * Scans students for behavioral milestones and triggers new flares.
     *
     * BOLT: Uses manual index loops to avoid iterator overhead during high-frequency checks.
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
     * Updates flare lifecycles. Should be called on every frame.
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
