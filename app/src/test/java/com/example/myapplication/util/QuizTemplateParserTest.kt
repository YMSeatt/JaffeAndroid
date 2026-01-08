package com.example.myapplication.util

import org.junit.Assert.assertEquals
import org.junit.Test

class QuizTemplateParserTest {

    @Test
    fun `parseMarks handles empty string`() {
        val result = QuizTemplateParser.parseMarks("")
        assertEquals(emptyMap<String, Int>(), result)
    }

    @Test
    fun `parseMarks handles blank string`() {
        val result = QuizTemplateParser.parseMarks("   ")
        assertEquals(emptyMap<String, Int>(), result)
    }

    @Test
    fun `parseMarks handles single valid entry`() {
        val result = QuizTemplateParser.parseMarks("A:10")
        assertEquals(mapOf("A" to 10), result)
    }

    @Test
    fun `parseMarks handles multiple valid entries`() {
        val result = QuizTemplateParser.parseMarks("A:10, B:20, C:30")
        assertEquals(mapOf("A" to 10, "B" to 20, "C" to 30), result)
    }

    @Test
    fun `parseMarks handles entries with extra whitespace`() {
        val result = QuizTemplateParser.parseMarks("  A :  10  ,   B:20 ")
        assertEquals(mapOf("A" to 10, "B" to 20), result)
    }

    @Test
    fun `parseMarks handles invalid entries`() {
        val result = QuizTemplateParser.parseMarks("A:10, B, C:30, D:E")
        assertEquals(mapOf("A" to 10, "C" to 30), result)
    }

    @Test
    fun `formatMarks handles empty map`() {
        val result = QuizTemplateParser.formatMarks(emptyMap())
        assertEquals("", result)
    }

    @Test
    fun `formatMarks handles single entry`() {
        val result = QuizTemplateParser.formatMarks(mapOf("A" to 10))
        assertEquals("A: 10", result)
    }

    @Test
    fun `formatMarks handles multiple entries`() {
        val result = QuizTemplateParser.formatMarks(mapOf("A" to 10, "B" to 20))
        val expectedParts = setOf("A: 10", "B: 20")
        val actualParts = result.split(", ").toSet()
        assertEquals(expectedParts, actualParts)
    }
}
