package com.example.myapplication.labs.ghost.flora

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.QuizLog
import kotlin.math.max
import kotlin.math.min

/**
 * GhostFloraEngine: Logic engine for Neural Botanical Visualization.
 *
 * It transforms classroom data into biological growth parameters.
 * - Positive behaviors act as "Sunlight/Water" (Growth).
 * - Negative behaviors cause "Wilting" (Vitality).
 * - Academic performance influences "Bloom Complexity".
 *
 * @see GhostFloraLayer for the visual representation.
 */
object GhostFloraEngine {

    /**
     * Represents the calculated botanical state of a student node.
     */
    data class FloraState(
        val growth: Float,      // 0.0 (Seed) to 1.0 (Full Bloom)
        val vitality: Float,    // -1.0 (Wilted) to 1.0 (Thriving)
        val petalCount: Int,    // 3 to 12 petals
        val complexity: Float,  // 0.0 to 1.0 (Fractal depth)
        val colorShift: Float   // Hue adjustment based on behavior type balance
    )

    /**
     * Calculates the Flora state for a student based on their historical logs.
     *
     * @param studentId The student's unique ID (used for deterministic randomization).
     * @param behaviorLogs Historical behavior events for the student.
     * @param quizLogs Historical quiz logs for the student.
     * @return A [FloraState] object containing the growth parameters.
     */
    fun calculateFloraState(
        studentId: Long,
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>
    ): FloraState {
        // BOLT: Manual loops to avoid functional operator overhead
        var positiveCount = 0
        var negativeCount = 0
        for (event in behaviorLogs) {
            if (event.type.contains("Negative", ignoreCase = true)) {
                negativeCount++
            } else {
                positiveCount++
            }
        }

        var totalQuizRatio = 0.0f
        var quizCount = 0
        for (log in quizLogs) {
            val v = log.markValue
            val m = log.maxMarkValue
            if (v != null && m != null && m > 0) {
                totalQuizRatio += (v.toFloat() / m.toFloat())
                quizCount++
            }
        }

        val avgQuiz = if (quizCount > 0) totalQuizRatio / quizCount else 0.7f

        // 1. Growth: Driven by positive participation volume
        // A student needs ~10 positive logs for a full bloom
        val growth = (positiveCount.toFloat() / 10f).coerceIn(0.1f, 1.0f)

        // 2. Vitality: Driven by behavior balance
        val totalBehavior = behaviorLogs.size.coerceAtLeast(1)
        val vitality = ((positiveCount - negativeCount).toFloat() / totalBehavior).coerceIn(-1.0f, 1.0f)

        // 3. Petal Count: Deterministic based on ID (3 to 8 petals)
        val petalCount = (3 + (studentId % 6).toInt())

        // 4. Complexity: Driven by academic performance
        val complexity = avgQuiz.coerceIn(0.1f, 1.0f)

        // 5. Color Shift: Shifts towards Magenta for negative, Cyan for positive
        val colorShift = (vitality * 0.5f + 0.5f).coerceIn(0.0f, 1.0f)

        return FloraState(
            growth = growth,
            vitality = vitality,
            petalCount = petalCount,
            complexity = complexity,
            colorShift = colorShift
        )
    }
}
