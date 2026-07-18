package com.example.myapplication.util

import android.util.LruCache
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.QuizMarkType
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Centralized engine for calculating quiz percentages from granular mark data.
 *
 * This engine implements the core scoring algorithm ported from the Python reference
 * implementation (`_calculate_quiz_score_percentage`). It transforms JSON-based mark counts
 * into numeric percentages by applying point values defined in [QuizMarkType].
 *
 * ### BOLT (Performance-Obsessed) Architecture:
 * - **LruCache**: Utilizes [decodedMarksCache] to avoid redundant JSON deserialization of
 *   `marksData` strings across multiple rule evaluations.
 * - **Identity-Based Memoization**: Uses [getScoringContext] to cache and reuse pre-calculated
 *   lookups ([QuizScoringContext]) when the input list of [QuizMarkType] remains unchanged.
 * - **Complexity**: Achieves $O(M + N)$ complexity (where M is marks in a log, N is total mark types)
 *   by using HashMaps for mark type lookups instead of repeated list searches.
 */
object QuizScoreEngine {

    /** Shared JSON configuration for decoding mark data. */
    val json = Json { ignoreUnknownKeys = true }

    /**
     * Cache for decoded mark JSON strings. Keyed by the raw JSON string from `QuizLog.marksData`.
     */
    private val decodedMarksCache = LruCache<String, Map<String, Int>>(1000)

    /**
     * BOLT: Cache for calculated percentage results.
     * Keyed by a composite long of log identity and scoring context identity.
     */
    private val scoreResultCache = LruCache<Long, Double>(1000)

    /**
     * A performance-optimized snapshot of mark type metadata.
     *
     * This internal class pre-calculates the necessary mappings and heuristics required
     * for bulk scoring operations. By resolving these values once per UI update cycle,
     * the engine eliminates redundant $O(N)$ searches and allocations inside the per-student loop.
     *
     * @property markTypesRef The source list of mark types used to build this context.
     */
    internal class QuizScoringContext(val markTypesRef: List<QuizMarkType>) {
        /**
         * The heuristic used to find the "standard" mark type for the denominator.
         * Priority:
         * 1. First mark type named "Correct" that contributes to the total.
         * 2. First mark type found that contributes to the total.
         */
        val correctMarkType = markTypesRef.find {
            it.name.equals("Correct", ignoreCase = true) && it.contributesToTotal
        } ?: markTypesRef.find { it.contributesToTotal }

        /** The base point value assigned to a single question. Defaults to 1.0. */
        val defaultPointsPerMainQuestion = correctMarkType?.defaultPoints ?: 1.0

        /** Fast lookup by Database ID (as String). */
        val markTypeMapById = markTypesRef.associateBy { it.id.toString() }

        /** Fast lookup by Name. */
        val markTypeMapByName = markTypesRef.associateBy { it.name }

        /** Pre-calculated sum of all contributing point values (unused in current denominator logic). */
        val sumDefaultPointsContributing = markTypesRef.filter { it.contributesToTotal }.sumOf { it.defaultPoints }
    }

    @Volatile
    private var lastScoringContext: QuizScoringContext? = null

    /**
     * Resolves a [QuizScoringContext] for the given list of mark types.
     *
     * This method utilizes identity-based memoization (`===`). If the [quizMarkTypes] list
     * is the same object instance as the last call, the cached context is returned immediately.
     * This is critical for maintaining 60fps during seating chart updates where this
     * method is called for every student icon.
     *
     * @param quizMarkTypes The list of mark types to analyze.
     * @return An optimized context for scoring lookups.
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
     * Decodes the granular marks from a [QuizLog] into a usable Map.
     *
     * To maximize performance, this method uses [decodedMarksCache] to store the results
     * of expensive JSON deserialization. Subsequent calls with the same [QuizLog.marksData]
     * string will return the cached Map in $O(1)$ time.
     *
     * @param log The quiz log containing the JSON marks string.
     * @return A map of mark type IDs or names to their respective counts.
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
     * Calculates the score percentage for a quiz entry.
     *
     * This is a convenience overload that resolves a [QuizScoringContext] internally.
     * For high-frequency callers (e.g., inside loops), use the [QuizScoringContext] overload instead.
     *
     * @param log The quiz log entry to evaluate.
     * @param quizMarkTypes The list of available mark types.
     * @return The calculated percentage (0.0 to 100.0+), or null if the log lacks sufficient data.
     */
    fun calculatePercentage(log: QuizLog, quizMarkTypes: List<QuizMarkType>): Double? {
        return calculatePercentage(log, getScoringContext(quizMarkTypes))
    }

    /**
     * Calculates the score percentage for a quiz entry using a pre-resolved context.
     *
     * ### Algorithm Logic (Parity with Python):
     * 1. **Zero-Question Check**: If `numQuestions <= 0`, it attempts a legacy fallback
     *    using `markValue / maxMarkValue`.
     * 2. **Denominator Calculation**: The max possible points is `numQuestions * defaultPointsPerMainQuestion`.
     * 3. **Empty Data Check**: If `marksData` is empty but `markValue` exists, it uses the legacy value.
     * 4. **Numerator Calculation**: Sums `count * defaultPoints` for every mark found in
     *    `marksData` that has a corresponding [QuizMarkType] definition.
     * 5. **Final Percentage**: `(Earned Points / Possible Points) * 100`.
     *
     * @param log The quiz log entry to evaluate.
     * @param context The pre-calculated scoring metadata.
     * @return The calculated percentage (0.0 to 100.0+), or null if the log lacks sufficient data.
     */
    internal fun calculatePercentage(log: QuizLog, context: QuizScoringContext): Double? {
        // BOLT: Result memoization using bit-packed Long key to avoid String allocation.
        // We use log.hashCode() instead of System.identityHashCode(log) to ensure cache persistence
        // across Room database flow emissions and ViewModel updates where content is identical but references change.
        val logHash = log.hashCode().toLong()
        val cacheKey = ((logHash and 0xFFFFFFFFL) shl 32) or (System.identityHashCode(context).toLong() and 0xFFFFFFFFL)
        val cached = scoreResultCache.get(cacheKey)
        if (cached != null) return cached

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

        val result = if (totalPossiblePointsMain > 0) {
            (totalEarnedPoints / totalPossiblePointsMain) * 100.0
        } else if (totalEarnedPoints > 0) {
            100.0 // Extra credit only scenario
        } else {
            0.0
        }

        scoreResultCache.put(cacheKey, result)
        return result
    }
}
