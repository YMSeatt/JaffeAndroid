package com.example.myapplication.util

import kotlin.math.max
import kotlin.math.min

object NameMatchingUtil {

    /**
     * Calculates the Levenshtein distance between two strings.
     * Ported from Python implementation for logic parity.
     */
    fun levenshteinDistance(s1: String, s2: String): Int {
        if (s1.length < s2.length) {
            return levenshteinDistance(s2, s1)
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
                val substitutions = previousRow[j] + if (s1[i] != s2[j]) 1 else 0
                currentRow[j + 1] = min(min(insertions, deletions), substitutions)
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
        if (s1.isNullOrEmpty() && s2.isNullOrEmpty()) return 1.0
        if (s1.isNullOrEmpty() || s2.isNullOrEmpty()) return 0.0

        val distance = levenshteinDistance(s1.lowercase(), s2.lowercase())
        val maxLen = max(s1.length, s2.length)
        if (maxLen == 0) return 1.0
        return 1.0 - (distance.toDouble() / maxLen)
    }
}
