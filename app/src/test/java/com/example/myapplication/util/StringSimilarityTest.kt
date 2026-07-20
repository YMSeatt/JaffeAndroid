package com.example.myapplication.util

import org.junit.Assert.assertEquals
import org.junit.Test

class StringSimilarityTest {

    @Test
    fun testLevenshteinDistance() {
        assertEquals(0, StringSimilarity.levenshteinDistance("kitten", "kitten"))
        assertEquals(3, StringSimilarity.levenshteinDistance("kitten", "sitting"))
        assertEquals(2, StringSimilarity.levenshteinDistance("flaw", "lawn"))
        assertEquals(2, StringSimilarity.levenshteinDistance("gumbo", "gambol"))
        assertEquals(2, StringSimilarity.levenshteinDistance("book", "back"))
        assertEquals(4, StringSimilarity.levenshteinDistance("", "test"))
        assertEquals(4, StringSimilarity.levenshteinDistance("test", ""))

        // From NameMatchingUtilTest
        assertEquals(3, StringSimilarity.levenshteinDistance("Saturday", "Sunday"))
        assertEquals(1, StringSimilarity.levenshteinDistance("abc", "abd"))
    }

    @Test
    fun testNameSimilarityRatio() {
        // Exact match
        assertEquals(1.0f, StringSimilarity.nameSimilarityRatio("John Doe", "John Doe"), 0.001f)

        // Case insensitive match
        assertEquals(1.0f, StringSimilarity.nameSimilarityRatio("john doe", "John Doe"), 0.001f)

        // Slight difference
        val ratio1 = StringSimilarity.nameSimilarityRatio("John Doe", "Jon Doe")
        assertEquals(1.0f - (1.0f / 8.0f), ratio1, 0.001f) // "John Doe" len is 8. Distance is 1. Ratio = 1 - 1/8 = 0.875

        // Completely different
        assertEquals(0.0f, StringSimilarity.nameSimilarityRatio("abc", "xyz"), 0.001f)

        // Empty strings
        assertEquals(1.0f, StringSimilarity.nameSimilarityRatio("", ""), 0.001f)
        assertEquals(0.0f, StringSimilarity.nameSimilarityRatio("John", ""), 0.001f)

        // From NameMatchingUtilTest
        assertEquals((4.0/7.0).toFloat(), StringSimilarity.nameSimilarityRatio("kitten", "sitting"), 0.001f)
        assertEquals(1.0f, StringSimilarity.nameSimilarityRatio("John", "john"), 0.001f)
    }

    @Test
    fun testNameSimilarityRatioWithThreshold() {
        // Exact match (always 1.0f regardless of threshold)
        assertEquals(1.0f, StringSimilarity.nameSimilarityRatioWithThreshold("John Doe", "John Doe", 0.5f), 0.001f)

        // If length difference dictates potential similarity <= threshold, return 0f
        // s1: "John Doe" (len 8), s2: "John" (len 4). diff = 4. potential max ratio = 1 - 4/8 = 0.5f.
        // If threshold is 0.6f, potential is 0.5f <= 0.6f, should return 0.0f.
        assertEquals(0.0f, StringSimilarity.nameSimilarityRatioWithThreshold("John Doe", "John", 0.6f), 0.001f)

        // If potential max ratio is > threshold, but actual is <= threshold
        // s1: "John Doe" (len 8), s2: "John" (len 4). actual distance is 4 (actual ratio 0.5f).
        // threshold is 0.4f. potential is 0.5f > 0.4f, so it computes actual. Actual ratio is 0.5f > 0.4f -> returns 0.5f.
        assertEquals(0.5f, StringSimilarity.nameSimilarityRatioWithThreshold("John Doe", "John", 0.4f), 0.001f)

        // If actual ratio <= threshold
        // s1: "John Doe" (len 8), s2: "Jahn" (len 4). actual distance is 5 (actual ratio = 3 / 8 = 0.375f).
        // threshold is 0.4f. potential is 0.5f > 0.4f, so it computes actual. Actual ratio is 0.375f <= 0.4f -> returns 0.0f.
        assertEquals(0.0f, StringSimilarity.nameSimilarityRatioWithThreshold("John Doe", "Jahn", 0.4f), 0.001f)

        // Case insensitivity check
        assertEquals(1.0f, StringSimilarity.nameSimilarityRatioWithThreshold("John", "john", 0.8f), 0.001f)
    }
}
