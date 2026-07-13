package com.example.myapplication.labs.ghost.flora

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import kotlin.math.max

/**
 * GhostFloraEngine: Implements "Neural Botanical Growth" logic for the classroom.
 * It translates academic performance and behavioral patterns into organic growth parameters.
 *
 * This engine serves as the "Biological Logic" for the Ghost Flora visualization,
 * mapping discrete classroom events to continuous procedural parameters.
 *
 * Parity with Python/ghost_flora_analysis.py.
 */
object GhostFloraEngine {

    /**
     * Represents the calculated state of a student's "Neural Flower".
     *
     * @property growth Normalized petal length (0.2 - 1.2), driven by academic scores.
     * @property vitality Normalized color balance (0.0 - 1.0), where 1.0 is Cyan (Positive)
     * and 0.0 is Magenta (Negative).
     * @property complexity Normalized petal density (0.1 - 1.0), driven by log frequency.
     * @property seed A stable 0.0-1.0 float used to seed procedural noise and rotation.
     */
    data class FloraState(
        val growth: Float,
        val vitality: Float,
        val complexity: Float,
        val seed: Float
    )

    /**
     * Calculates the [FloraState] for a student by aggregating behavioral and academic logs.
     *
     * The logic follows a three-pillar mapping:
     * 1. **Academic -> Growth**: A simple average of normalized quiz scores and homework
     *    completion rates. Fallback values (0.7, 0.8) represent a "standard healthy sprout".
     * 2. **Behavioral -> Vitality**: A ratio of non-negative behaviors to total behaviors.
     *    Fallback (0.9) assumes a positive baseline for new or quiet students.
     * 3. **Frequency -> Complexity**: Total log count normalized against a "saturation point"
     *    of 10 logs.
     *
     * @param studentId Unique ID used to derive the [FloraState.seed].
     * @param behaviorLogs Historical list of behavior events for the student.
     * @param quizLogs Historical list of quiz logs for the student.
     * @param homeworkLogs Historical list of homework logs for the student.
     * @return A [FloraState] encapsulating the growth parameters for the AGSL shader.
     */
    fun calculateFloraState(
        studentId: Long,
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>,
        homeworkLogs: List<HomeworkLog>
    ): FloraState {
        // Pillar 1: Growth (Academic)
        // We use the markValue / maxMarkValue ratio.
        // Fallback: 0.75f average if no academic data exists.
        val quizAvg = if (quizLogs.isNotEmpty()) {
            quizLogs.mapNotNull { it.markValue?.let { v -> it.maxMarkValue?.let { m -> if (m > 0) v / m else 0.0 } } }.average().toFloat()
        } else 0.7f

        val homeworkRate = if (homeworkLogs.isNotEmpty()) {
            homeworkLogs.count { it.status.contains("Done", ignoreCase = true) }.toFloat() / homeworkLogs.size
        } else 0.8f

        val growth = (quizAvg + homeworkRate) / 2f

        // Pillar 2: Vitality (Behavioral balance)
        // Maps the "climate" of the student's behavior.
        val totalBehaviors = behaviorLogs.size
        val positiveCount = behaviorLogs.count { !it.type.contains("Negative", ignoreCase = true) }
        val vitality = if (totalBehaviors > 0) {
            positiveCount.toFloat() / totalBehaviors
        } else 0.9f

        // Pillar 3: Complexity (Activity frequency)
        // 10 logs is the current "complexity saturation point" for the visual system.
        val totalLogs = behaviorLogs.size + quizLogs.size + homeworkLogs.size
        val complexity = (totalLogs.toFloat() / 10f).coerceIn(0.1f, 1.0f)

        // Stable Seed: derived from studentId to ensure the flower doesn't "jump" patterns
        // between recompositions, while remaining unique per student.
        val seed = (studentId % 1000).toFloat() / 1000f

        return FloraState(
            growth = growth.coerceIn(0.2f, 1.2f),
            vitality = vitality.coerceIn(0.0f, 1.0f),
            complexity = complexity,
            seed = seed
        )
    }
}
