package com.example.myapplication.labs.ghost

import android.util.LongSparseArray
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostFocusEngine: Logic for calculating classroom concentration metrics.
 *
 * It analyzes behavioral events within a specific "Focus Window" to determine
 * the individual and collective concentration state of the classroom.
 */
object GhostFocusEngine {

    /**
     * Calculates concentration scores (0.0 to 1.0) for all students.
     *
     * @param students List of current students.
     * @param behaviorLogsByStudent Map of student ID to their behavior logs.
     * @param focusStartTime The timestamp when the current focus session started.
     * @return A LongSparseArray mapping student IDs to their concentration score.
     */
    fun calculateConcentrationScores(
        students: List<StudentUiItem>,
        behaviorLogsByStudent: Map<Long, List<BehaviorEvent>>,
        focusStartTime: Long
    ): LongSparseArray<Float> {
        val scores = LongSparseArray<Float>(students.size)
        val now = System.currentTimeMillis()

        for (i in students.indices) {
            val student = students[i]
            val logs = behaviorLogsByStudent[student.id.toLong()] ?: emptyList()

            // Base focus: Students start at a high baseline of 0.8
            var concentration = 0.8f

            // Analyze logs occurring AFTER focusStartTime
            // BOLT: Use manual loop for performance in high-frequency synthesis
            for (j in logs.indices) {
                val log = logs[j]
                if (log.timestamp >= focusStartTime) {
                    if (log.type.contains("Negative", ignoreCase = true) ||
                        log.type.contains("Distraction", ignoreCase = true)) {
                        concentration -= 0.3f
                    } else if (log.type.contains("Positive", ignoreCase = true) ||
                               log.type.contains("Focused", ignoreCase = true)) {
                        concentration += 0.15f
                    }
                }
            }

            // Passive recovery: Focus slightly recovers if no recent negative logs
            // (Simulated logic: if the focus session has been active for a while and no logs)
            // But for simplicity in PoC, we just clamp.

            scores.put(student.id.toLong(), concentration.coerceIn(0.0f, 1.0f))
        }

        return scores
    }

    /**
     * Calculates the macroscopic classroom focus level.
     */
    fun calculateGlobalFocus(scores: LongSparseArray<Float>): Float {
        if (scores.size() == 0) return 1.0f
        var sum = 0f
        for (i in 0 until scores.size()) {
            sum += scores.valueAt(i)
        }
        return sum / scores.size()
    }
}
