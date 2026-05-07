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
 * BOLT: Optimized with LruCache and identity-based memoization to avoid redundant
 * JSON parsing and O(N) list operations.
 */
object QuizScoreEngine {

    val json = Json { ignoreUnknownKeys = true }
    private val decodedMarksCache = LruCache<String, Map<String, Int>>(1000)

    /**
     * BOLT: Holds pre-calculated mappings and configurations for a specific set of quiz mark types.
     * This avoids redundant O(N) searches and HashMap allocations during bulk calculations.
     */
    internal class QuizScoringContext(val markTypesRef: List<QuizMarkType>) {
        val correctMarkType = markTypesRef.find {
            it.name.equals("Correct", ignoreCase = true) && it.contributesToTotal
        } ?: markTypesRef.find { it.contributesToTotal }

        val defaultPointsPerMainQuestion = correctMarkType?.defaultPoints ?: 1.0
        val markTypeMapById = markTypesRef.associateBy { it.id.toString() }
        val markTypeMapByName = markTypesRef.associateBy { it.name }
        val sumDefaultPointsContributing = markTypesRef.filter { it.contributesToTotal }.sumOf { it.defaultPoints }
    }

    @Volatile
    private var lastScoringContext: QuizScoringContext? = null

    /**
     * Internal helper to retrieve or create a scoring context for the given mark types.
     * Uses identity-based memoization for high-performance reuse.
     */
    internal fun getScoringContext(quizMarkTypes: List<QuizMarkType>): QuizScoringContext {
        val cached = lastScoringContext
        if (cached?.markTypesRef === quizMarkTypes) {
            return cached
        }
        val newContext = QuizScoringContext(quizMarkTypes)
        lastScoringContext = newContext
        return newContext
    }

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
        return calculatePercentage(log, getScoringContext(quizMarkTypes))
    }

    /**
     * Optimized overload that accepts a pre-calculated [QuizScoringContext].
     */
    internal fun calculatePercentage(log: QuizLog, context: QuizScoringContext): Double? {
        val marksDataMap = getMarksData(log)

        // Python logic: if num_questions <= 0, we can't calculate based on marksData.
        if (log.numQuestions <= 0) {
            // Fallback for legacy logs or simplified logs
            if (log.markValue != null && log.maxMarkValue != null && log.maxMarkValue > 0) {
                return (log.markValue / log.maxMarkValue) * 100.0
            }
            return null
        }

        val totalPossiblePointsMain = log.numQuestions * context.defaultPointsPerMainQuestion

        if (marksDataMap.isEmpty()) {
             // If no granular marks but we have markValue, use it.
             if (log.markValue != null) {
                 return if (totalPossiblePointsMain > 0) (log.markValue / totalPossiblePointsMain) * 100.0 else 0.0
             }
             return null
        }

        var totalEarnedPoints = 0.0
        val markTypeMapById = context.markTypeMapById
        val markTypeMapByName = context.markTypeMapByName

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
