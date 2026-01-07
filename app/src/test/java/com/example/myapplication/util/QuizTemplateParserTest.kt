package com.example.myapplication.util

import org.junit.Assert.assertEquals
import org.junit.Test

class QuizTemplateParserTest {

    @Test
    fun `parseDefaultMarks with empty string returns empty map`() {
        val result = QuizTemplateParser.parseDefaultMarks("")
        assertEquals(emptyMap<String, Int>(), result)
    }

    @Test
    fun `parseDefaultMarks with blank string returns empty map`() {
        val result = QuizTemplateParser.parseDefaultMarks("   ")
        assertEquals(emptyMap<String, Int>(), result)
    }

    @Test
    fun `parseDefaultMarks with single valid pair`() {
        val result = QuizTemplateParser.parseDefaultMarks("A:10")
        assertEquals(mapOf("A" to 10), result)
    }

    @Test
    fun `parseDefaultMarks with multiple valid pairs`() {
        val result = QuizTemplateParser.parseDefaultMarks("A:10, B:5, C:0")
        assertEquals(mapOf("A" to 10, "B" to 5, "C" to 0), result)
    }

    @Test
    fun `parseDefaultMarks with extra whitespace`() {
        val result = QuizTemplateParser.parseDefaultMarks("  A :  10 ,   B:5  ")
        assertEquals(mapOf("A" to 10, "B" to 5), result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `parseDefaultMarks with missing value throws exception`() {
        QuizTemplateParser.parseDefaultMarks("A:")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `parseDefaultMarks with missing key throws exception`() {
        QuizTemplateParser.parseDefaultMarks(":10")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `parseDefaultMarks with invalid separator throws exception`() {
        QuizTemplateParser.parseDefaultMarks("A-10")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `parseDefaultMarks with non-integer value throws exception`() {
        QuizTemplateParser.parseDefaultMarks("A:ten")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `parseDefaultMarks with extra colon throws exception`() {
        QuizTemplateParser.parseDefaultMarks("A:10:20")
    }
}
