package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog

/**
 * GhostAuroraEngine: Analyzes classroom data to determine the visual parameters
 * of the Ghost Aurora (Classroom Climate).
 */
object GhostAuroraEngine {

    data class AuroraParams(
        val intensity: Float,
        val colorPrimary: Triple<Float, Float, Float>,
        val colorSecondary: Triple<Float, Float, Float>,
        val speed: Float
    )

    private val COLOR_POSITIVE = Triple(0.0f, 0.8f, 1.0f) // Cyan
    private val COLOR_NEGATIVE = Triple(1.0f, 0.2f, 0.1f) // Red-ish
    private val COLOR_ACADEMIC = Triple(0.6f, 0.0f, 1.0f) // Purple
    private val COLOR_NEUTRAL = Triple(0.0f, 0.5f, 0.8f)  // Deep Blue

    /**
     * Calculates aurora parameters based on recent events.
     *
     * @param behaviorLogs List of behavior events.
     * @param quizLogs List of quiz logs.
     * @param homeworkLogs List of homework logs.
     * @param timeWindow The window (in ms) to consider for "recent" activity. Default 10 mins.
     */
    fun calculateAuroraParams(
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>,
        homeworkLogs: List<HomeworkLog>,
        timeWindow: Long = 10 * 60 * 1000L
    ): AuroraParams {
        val now = System.currentTimeMillis()
        val recentBehaviors = behaviorLogs.filter { now - it.timestamp < timeWindow }

        // Intensity scales with log frequency (up to 20 logs for max intensity)
        val eventCount = recentBehaviors.size + quizLogs.size + homeworkLogs.size
        val intensity = (eventCount.toFloat() / 20f).coerceIn(0.2f, 1.0f)

        // Speed matches intensity
        val speed = 0.5f + (intensity * 1.5f)

        // Color logic: Balance of positive vs negative
        val negativeCount = recentBehaviors.count { it.type.contains("Negative", ignoreCase = true) }
        val positiveCount = recentBehaviors.count { !it.type.contains("Negative", ignoreCase = true) }
        val academicCount = quizLogs.size + homeworkLogs.size

        val eventSum = negativeCount + positiveCount + academicCount
        val total = eventSum.coerceAtLeast(1)

        val negRatio = negativeCount.toFloat() / total
        val posRatio = positiveCount.toFloat() / total
        val acadRatio = academicCount.toFloat() / total

        // Blend primary color based on ratios
        val r = (COLOR_NEGATIVE.first * negRatio) + (COLOR_POSITIVE.first * posRatio) + (COLOR_ACADEMIC.first * acadRatio)
        val g = (COLOR_NEGATIVE.second * negRatio) + (COLOR_POSITIVE.second * posRatio) + (COLOR_ACADEMIC.second * acadRatio)
        val b = (COLOR_NEGATIVE.third * negRatio) + (COLOR_POSITIVE.third * posRatio) + (COLOR_ACADEMIC.third * acadRatio)

        val primary = if (eventSum == 0) COLOR_NEUTRAL else Triple(r, g, b)

        // Secondary color is a slightly shifted version or a complementary blue
        val secondary = Triple(
            (primary.first * 0.5f).coerceIn(0f, 1f),
            (primary.second * 0.8f + 0.2f).coerceIn(0f, 1f),
            (primary.third * 1.2f).coerceIn(0f, 1f)
        )

        return AuroraParams(intensity, primary, secondary, speed)
    }
}
