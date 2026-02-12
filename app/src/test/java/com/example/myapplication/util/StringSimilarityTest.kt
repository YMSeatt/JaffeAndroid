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
    }
}
