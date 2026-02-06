package com.example.myapplication.util

import kotlin.math.max

object NameMatchingUtil {

    /**
     * Calculates the Levenshtein distance between two strings.
     * Ported from Python implementation for logic parity, with performance optimizations.
     *
     * @param ignoreCase Whether to ignore case during comparison.
     */
    fun levenshteinDistance(s1: String, s2: String, ignoreCase: Boolean = false): Int {
        if (s1 == s2) return 0
        if (ignoreCase && s1.equals(s2, ignoreCase = true)) return 0

        if (s1.length < s2.length) {
            return levenshteinDistance(s2, s1, ignoreCase)
        }
        if (s2.isEmpty()) {
            return s1.length
        }

        var previousRow = IntArray(s2.length + 1) { it }
        var currentRow = IntArray(s2.length + 1)
        for (i in s1.indices) {
            currentRow[0] = i + 1
            for (j in s2.indices) {
                val insertions = previousRow[j + 1] + 1
                val deletions = currentRow[j] + 1
                val match = if (ignoreCase) {
                    s1[i].equals(s2[j], ignoreCase = true)
                } else {
                    s1[i] == s2[j]
                }
                val substitutions = previousRow[j] + if (match) 0 else 1
                currentRow[j + 1] = minOf(insertions, deletions, substitutions)
            }
            val temp = previousRow
            previousRow = currentRow
            currentRow = temp
        }
        return previousRow[s2.length]
    }

    /**
     * Calculates similarity ratio between 0 and 1 based on Levenshtein distance.
     * Ported from Python implementation for logic parity.
     */
    fun nameSimilarityRatio(s1: String?, s2: String?): Double {
        if (s1 == s2) return 1.0
        if (s1.isNullOrEmpty() || s2.isNullOrEmpty()) {
            return if (s1.isNullOrEmpty() && s2.isNullOrEmpty()) 1.0 else 0.0
        }

        val distance = levenshteinDistance(s1, s2, ignoreCase = true)
        val maxLen = max(s1.length, s2.length)
        if (maxLen == 0) return 1.0
        return 1.0 - (distance.toDouble() / maxLen)
    }
}
