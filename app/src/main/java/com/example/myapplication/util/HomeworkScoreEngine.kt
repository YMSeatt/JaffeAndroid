package com.example.myapplication.util

import android.util.LruCache
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.HomeworkMarkMetadata
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Centralized engine for calculating homework scores from granular mark data and status labels.
 *
 * This engine implements the scoring algorithm ported from the Python reference
 * implementation. It transforms JSON-based mark counts and status strings into
 * numeric "Total Points" by applying values defined in [HomeworkMarkMetadata].
 *
 * ### BOLT (Performance-Obsessed) Architecture:
 * - **LruCache**: Utilizes [parsedMarksCache] to avoid redundant JSON deserialization of
 *   `marksData` strings.
 * - **Complexity**: Achieves $O(M + N)$ complexity (where M is marks in a log, N is metadata count).
 */
object HomeworkScoreEngine {

    private val gson = Gson()
    private val mapType = object : TypeToken<Map<String, Any>>() {}.type

    /** Cache for decoded marks data JSON strings. */
    private val parsedMarksCache = LruCache<String, Map<String, Any>>(1000)

    /**
     * Helper to safely deserialize JSON-based mark data.
     */
    private fun parseMarksData(json: String?): Map<String, Any> {
        if (json.isNullOrEmpty()) return emptyMap()
        return parsedMarksCache.get(json) ?: try {
            gson.fromJson<Map<String, Any>>(json, mapType).also {
                parsedMarksCache.put(json, it)
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Calculates the total points for a homework entry.
     *
     * ### Algorithm Logic (Parity with Python):
     * 1. **Numeric Accumulation**: Sums all numeric values found in the `marksData` JSON.
     * 2. **Status Mapping**:
     *    - If `log.status` is "Yes" (case-insensitive), it adds points from metadata named "Complete".
     *    - Otherwise, if `log.status` matches any [HomeworkMarkMetadata] name, its points are added.
     * 3. **Granular Status Mapping**: Also checks keys in `marksData` against metadata names.
     *
     * @param log The homework log entry to evaluate.
     * @param markMetadata The list of available mark metadata.
     * @return The calculated total points.
     */
    fun calculateTotalPoints(log: HomeworkLog, markMetadata: List<HomeworkMarkMetadata>): Double {
        var totalPoints = 0.0
        val marksData = parseMarksData(log.marksData)

        // 1. Numeric accumulation from marksData
        marksData.values.forEach { value ->
            val doubleVal = if (value is Number) value.toDouble() else value.toString().toDoubleOrNull()
            doubleVal?.let { totalPoints += it }
        }

        // 2. Status-based points (Parity with Python session logic)
        val statusNormalized = log.status.trim()

        // Python parity: 'yes' adds points from 'hmark_complete' (we use name "Complete")
        if (statusNormalized.equals("Yes", ignoreCase = true)) {
            markMetadata.find { it.name.equals("Complete", ignoreCase = true) }?.let {
                totalPoints += it.defaultPoints
            }
        } else {
            // Check if status matches any metadata name directly
            markMetadata.find { it.name.equals(statusNormalized, ignoreCase = true) }?.let {
                totalPoints += it.defaultPoints
            }
        }

        // 3. Mapping of status-like keys in marksData (for "Select" mode parity)
        // Python: for opt_name in selected_options: if opt_mark_type: total_points += opt_mark_type.default_points
        // In Android, selected options are often stored as keys in marksData with value 1.0 or similar.
        marksData.forEach { (key, value) ->
            // If the value is a boolean true or a 1.0, and the key matches a metadata name,
            // we might be double-counting if we already summed it as numeric.
            // But if it's NOT a number (e.g. "Done": "Yes"), we should handle it.
            if (value !is Number && value.toString().toDoubleOrNull() == null) {
                markMetadata.find { it.name.equals(key, ignoreCase = true) }?.let {
                    totalPoints += it.defaultPoints
                }
            }
        }

        return totalPoints
    }
}
