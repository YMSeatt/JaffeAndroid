package com.example.myapplication.util

import org.junit.Assert.assertEquals
import org.junit.Test

class QuizTemplateParserTest {

    @Test
    fun `test valid marks string`() {
        val marksString = "A:10, B:5, C:0"
        val expected = mapOf("A" to 10, "B" to 5, "C" to 0)
        assertEquals(expected, QuizTemplateParser.parseDefaultMarks(marksString))
    }

    @Test
    fun `test string with extra whitespace`() {
        val marksString = "  A :  10 ,   B:5  "
        val expected = mapOf("A" to 10, "B" to 5)
        assertEquals(expected, QuizTemplateParser.parseDefaultMarks(marksString))
    }

    @Test
    fun `test string with invalid entries`() {
        val marksString = "A:10, B:foo, C:5, D:, :10"
        val expected = mapOf("A" to 10, "C" to 5)
        assertEquals(expected, QuizTemplateParser.parseDefaultMarks(marksString))
    }

    @Test
    fun `test empty string`() {
        val marksString = ""
        val expected = emptyMap<String, Int>()
        assertEquals(expected, QuizTemplateParser.parseDefaultMarks(marksString))
    }

    @Test
    fun `test string with only delimiters`() {
        val marksString = ",,:"
        val expected = emptyMap<String, Int>()
        assertEquals(expected, QuizTemplateParser.parseDefaultMarks(marksString))
    }
}
