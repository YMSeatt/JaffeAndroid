package com.example.myapplication.util

import kotlin.math.max
import kotlin.math.min

/**
 * Utility for calculating string similarity, ported from the Python desktop application.
 * Used primarily for fuzzy matching student names when loading layout templates
 * where database IDs may have changed.
 *
 * BOLT: Optimized to use reusable IntArray buffers to eliminate per-call row allocations.
 */
object StringSimilarity {

    private const val INITIAL_BUFFER_SIZE = 64
    private val row1Buffer = ThreadLocal.withInitial { IntArray(INITIAL_BUFFER_SIZE) }
    private val row2Buffer = ThreadLocal.withInitial { IntArray(INITIAL_BUFFER_SIZE) }

    private fun getBuffer(threadLocal: ThreadLocal<IntArray>, minSize: Int): IntArray {
        var buffer = threadLocal.get()
        if (buffer.size < minSize) {
            buffer = IntArray(max(minSize, buffer.size * 2))
            threadLocal.set(buffer)
        }
        return buffer
    }

    /**
     * Calculates the Levenshtein distance between two strings.
     * This represents the minimum number of single-character edits (insertions, deletions or substitutions)
     * required to change one word into the other.
     *
     * BOLT: Uses ThreadLocal buffers to avoid O(N) allocations per call.
     */
    fun levenshteinDistance(s1: CharSequence, s2: CharSequence, ignoreCase: Boolean = false): Int {
        if (s1 == s2) return 0
        if (ignoreCase && s1.toString().equals(s2.toString(), ignoreCase = true)) return 0

        val n = s1.length
        val m = s2.length

        if (n < m) return levenshteinDistance(s2, s1, ignoreCase)
        if (m == 0) return n

        var previousRow = getBuffer(row1Buffer, m + 1)
        var currentRow = getBuffer(row2Buffer, m + 1)

        for (j in 0..m) {
            previousRow[j] = j
        }

        for (i in 0 until n) {
            currentRow[0] = i + 1
            for (j in 0 until m) {
                val insertions = previousRow[j + 1] + 1
                val deletions = currentRow[j] + 1

                val match = if (ignoreCase) {
                    s1[i].equals(s2[j], ignoreCase = true)
                } else {
                    s1[i] == s2[j]
                }
                val substitutions = previousRow[j] + if (match) 0 else 1

                currentRow[j + 1] = min(min(insertions, deletions), substitutions)
            }
            val temp = previousRow
            previousRow = currentRow
            currentRow = temp
        }
        return previousRow[m]
    }

    /**
     * Calculates a similarity ratio between 0.0 and 1.0 based on Levenshtein distance.
     * 1.0 means identical strings, 0.0 means completely different.
     */
    fun nameSimilarityRatio(s1: String?, s2: String?): Float {
        if (s1 == s2) return 1.0f
        if (s1.isNullOrEmpty() || s2.isNullOrEmpty()) {
            return if (s1.isNullOrEmpty() && s2.isNullOrEmpty()) 1.0f else 0.0f
        }

        val distance = levenshteinDistance(s1, s2, ignoreCase = true)
        val maxLen = max(s1.length, s2.length)
        if (maxLen == 0) return 1.0f
        return 1.0f - (distance.toFloat() / maxLen.toFloat())
    }
}
