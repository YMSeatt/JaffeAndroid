package com.example.myapplication.labs.ghost.flora

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import kotlin.math.max

/**
 * GhostFloraEngine: Implements "Neural Botanical Growth" logic for the classroom.
 * It translates academic performance and behavioral patterns into organic growth parameters.
 *
 * Parity with Python/ghost_flora_analysis.py.
 */
object GhostFloraEngine {

    data class FloraState(
        val growth: Float,      // Petal length/scale (Academic)
        val vitality: Float,    // Color vibrancy (Behavioral)
        val complexity: Float,  // Number of petals/noise (Activity frequency)
        val seed: Float         // Unique student hash
    )

    /**
     * Calculates the "Flora State" for a student based on their logs.
     *
     * @param studentId Unique ID for seeding the procedural generation.
     * @param behaviorLogs Student's behavior events.
     * @param quizLogs Student's academic quiz logs.
     * @param homeworkLogs Student's academic homework logs.
     * @return A FloraState object with normalized (0.0 - 1.0) parameters.
     */
    fun calculateFloraState(
        studentId: Long,
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>,
        homeworkLogs: List<HomeworkLog>
    ): FloraState {
        // 1. Growth: Driven by academic average (Quiz + Homework)
        val quizAvg = if (quizLogs.isNotEmpty()) {
            quizLogs.mapNotNull { it.markValue?.let { v -> it.maxMarkValue?.let { m -> v / m } } }.average().toFloat()
        } else 0.7f

        val homeworkRate = if (homeworkLogs.isNotEmpty()) {
            homeworkLogs.count { it.status.contains("Done", ignoreCase = true) }.toFloat() / homeworkLogs.size
        } else 0.8f

        val growth = (quizAvg + homeworkRate) / 2f

        // 2. Vitality: Driven by behavioral balance (Positive vs Negative)
        val totalBehaviors = behaviorLogs.size
        val positiveCount = behaviorLogs.count { !it.type.contains("Negative", ignoreCase = true) }
        val vitality = if (totalBehaviors > 0) {
            positiveCount.toFloat() / totalBehaviors
        } else 0.9f

        // 3. Complexity: Driven by total activity frequency (Logs per session/time)
        val totalLogs = behaviorLogs.size + quizLogs.size + homeworkLogs.size
        // Normalize to a range (e.g., 5 logs = high complexity for a PoC)
        val complexity = (totalLogs.toFloat() / 10f).coerceIn(0.1f, 1.0f)

        // 4. Seed: Stable hash for visual consistency
        val seed = (studentId % 1000).toFloat() / 1000f

        return FloraState(
            growth = growth.coerceIn(0.2f, 1.2f),
            vitality = vitality.coerceIn(0.0f, 1.0f),
            complexity = complexity,
            seed = seed
        )
    }
}
