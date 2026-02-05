package com.example.myapplication.util

import org.junit.Assert.assertEquals
import org.junit.Test

class NameMatchingUtilTest {

    @Test
    fun testLevenshteinDistance() {
        assertEquals(0, NameMatchingUtil.levenshteinDistance("kitten", "kitten"))
        assertEquals(3, NameMatchingUtil.levenshteinDistance("kitten", "sitting"))
        assertEquals(3, NameMatchingUtil.levenshteinDistance("Saturday", "Sunday"))
        assertEquals(1, NameMatchingUtil.levenshteinDistance("abc", "abd"))
        assertEquals(3, NameMatchingUtil.levenshteinDistance("", "abc"))
        assertEquals(3, NameMatchingUtil.levenshteinDistance("abc", ""))
    }

    @Test
    fun testNameSimilarityRatio() {
        assertEquals(1.0, NameMatchingUtil.nameSimilarityRatio("John", "John"), 0.001)
        assertEquals(1.0, NameMatchingUtil.nameSimilarityRatio("", ""), 0.001)
        assertEquals(0.0, NameMatchingUtil.nameSimilarityRatio("John", ""), 0.001)
        assertEquals(0.0, NameMatchingUtil.nameSimilarityRatio("", "John"), 0.001)

        // "kitten" vs "sitting" -> distance 3, max length 7 -> ratio = 1 - 3/7 = 4/7 approx 0.571
        assertEquals(4.0/7.0, NameMatchingUtil.nameSimilarityRatio("kitten", "sitting"), 0.001)

        // Case insensitive
        assertEquals(1.0, NameMatchingUtil.nameSimilarityRatio("John", "john"), 0.001)
    }
}
