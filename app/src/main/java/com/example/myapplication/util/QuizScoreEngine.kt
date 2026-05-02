package com.example.myapplication.util

import android.util.LruCache
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.QuizMarkType
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * QuizScoreEngine: Centralized logic for calculating quiz percentages.
 * Ported from the Python blueprint for strict logical parity.
 *
 * BOLT: Optimized with LruCache to avoid redundant JSON parsing of marksData.
 */
object QuizScoreEngine {

    val json = Json { ignoreUnknownKeys = true }
    private val decodedMarksCache = LruCache<String, Map<String, Int>>(1000)

    /**
     * Decodes the marksData JSON from a QuizLog into a Map of mark type IDs/Names to counts.
     * BOLT: Uses LruCache to avoid redundant parsing.
     */
    fun getMarksData(log: QuizLog): Map<String, Int> {
        if (log.marksData.isBlank() || log.marksData == "{}") return emptyMap()
        return try {
            decodedMarksCache.get(log.marksData) ?: json.decodeFromString<Map<String, Int>>(log.marksData).also {
                decodedMarksCache.put(log.marksData, it)
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Calculates the score percentage for a given quiz log entry.
     * Logic matches Python's _calculate_quiz_score_percentage exactly.
     *
     * @param log The quiz log entry to evaluate.
     * @param quizMarkTypes The list of available mark types to determine point values.
     * @return The calculated percentage (0-100+) or null if calculation is not possible.
     */
    fun calculatePercentage(log: QuizLog, quizMarkTypes: List<QuizMarkType>): Double? {
        // Handle live quiz session scores or logs with specific score details
        // In the Android app, granular marks are stored in marksData JSON.

        val marksDataMap = getMarksData(log)

        // Python logic: if num_questions <= 0, we can't calculate based on marksData.
        if (log.numQuestions <= 0) {
            // Fallback for legacy logs or simplified logs
            if (log.markValue != null && log.maxMarkValue != null && log.maxMarkValue > 0) {
                return (log.markValue / log.maxMarkValue) * 100.0
            }
            return null
        }

        // Find the "Correct" mark type to determine the base points per question.
        // We match by name "Correct" or ID if it matches the default "mark_correct" string.
        val correctMarkType = quizMarkTypes.find {
            it.name.equals("Correct", ignoreCase = true) && it.contributesToTotal
        } ?: quizMarkTypes.find { it.contributesToTotal }

        val defaultPointsPerMainQuestion = correctMarkType?.defaultPoints ?: 1.0
        val totalPossiblePointsMain = log.numQuestions * defaultPointsPerMainQuestion

        if (marksDataMap.isEmpty()) {
             // If no granular marks but we have markValue, use it.
             if (log.markValue != null) {
                 return if (totalPossiblePointsMain > 0) (log.markValue / totalPossiblePointsMain) * 100.0 else 0.0
             }
             return null
        }

        var totalEarnedPoints = 0.0
        val markTypeMapById = quizMarkTypes.associateBy { it.id.toString() }
        val markTypeMapByName = quizMarkTypes.associateBy { it.name }

        marksDataMap.forEach { (key, count) ->
            // Try to match mark type by ID first, then by Name.
            val markConfig = markTypeMapById[key] ?: markTypeMapByName[key]
            if (markConfig != null) {
                totalEarnedPoints += count * markConfig.defaultPoints
            }
        }

        return if (totalPossiblePointsMain > 0) {
            (totalEarnedPoints / totalPossiblePointsMain) * 100.0
        } else if (totalEarnedPoints > 0) {
            100.0 // Extra credit only scenario
        } else {
            0.0
        }
    }
}
