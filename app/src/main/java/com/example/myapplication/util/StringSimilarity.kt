package com.example.myapplication.util

import kotlin.math.max
import kotlin.math.min

/**
 * Utility for calculating string similarity, ported from the Python desktop application.
 * Used primarily for fuzzy matching student names when loading layout templates
 * where database IDs may have changed.
 */
object StringSimilarity {

    /**
     * Calculates the Levenshtein distance between two strings.
     * This represents the minimum number of single-character edits (insertions, deletions or substitutions)
     * required to change one word into the other.
     */
    fun levenshteinDistance(s1: String, s2: String): Int {
        if (s1.length < s2.length) return levenshteinDistance(s2, s1)
        if (s2.isEmpty()) return s1.length

        var previousRow = IntArray(s2.length + 1) { it }
        var currentRow = IntArray(s2.length + 1)
        for (i in s1.indices) {
            currentRow[0] = i + 1
            for (j in s2.indices) {
                val insertions = previousRow[j + 1] + 1
                val deletions = currentRow[j] + 1
                val substitutions = previousRow[j] + (if (s1[i] == s2[j]) 0 else 1)
                currentRow[j + 1] = min(min(insertions, deletions), substitutions)
            }
            val temp = previousRow
            previousRow = currentRow
            currentRow = temp
        }
        return previousRow[s2.length]
    }

    /**
     * Calculates a similarity ratio between 0.0 and 1.0 based on Levenshtein distance.
     * 1.0 means identical strings, 0.0 means completely different.
     */
    fun nameSimilarityRatio(s1: String, s2: String): Float {
        if (s1.isEmpty() && s2.isEmpty()) return 1.0f
        if (s1.isEmpty() || s2.isEmpty()) return 0.0f

        val distance = levenshteinDistance(s1.lowercase(), s2.lowercase())
        val maxLen = max(s1.length, s2.length)
        if (maxLen == 0) return 1.0f
        return 1.0f - (distance.toFloat() / maxLen.toFloat())
    }
}
