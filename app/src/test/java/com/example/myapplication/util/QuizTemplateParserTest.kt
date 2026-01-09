package com.example.myapplication.util

import org.junit.Assert.assertEquals
import org.junit.Test

class QuizTemplateParserTest {

    @Test
    fun `parseDefaultMarks handles valid input`() {
        val input = "Correct:1, Incorrect:0, Bonus:5"
        val expected = mapOf("Correct" to 1, "Incorrect" to 0, "Bonus" to 5)
        assertEquals(expected, QuizTemplateParser.parseDefaultMarks(input))
    }

    @Test
    fun `parseDefaultMarks handles empty input`() {
        val input = ""
        val expected = emptyMap<String, Int>()
        assertEquals(expected, QuizTemplateParser.parseDefaultMarks(input))
    }

    @Test
    fun `parseDefaultMarks handles blank input`() {
        val input = "   "
        val expected = emptyMap<String, Int>()
        assertEquals(expected, QuizTemplateParser.parseDefaultMarks(input))
    }

    @Test
    fun `parseDefaultMarks handles input with extra whitespace`() {
        val input = "  Correct : 2 ,  Incorrect : -1  "
        val expected = mapOf("Correct" to 2, "Incorrect" to -1)
        assertEquals(expected, QuizTemplateParser.parseDefaultMarks(input))
    }

    @Test
    fun `parseDefaultMarks ignores malformed pairs`() {
        val input = "Correct:1, Incorrect, Bonus:5:2, :3, Partial:abc"
        val expected = mapOf("Correct" to 1)
        assertEquals(expected, QuizTemplateParser.parseDefaultMarks(input))
    }

    @Test
    fun `parseDefaultMarks handles a single valid pair`() {
        val input = "Perfect:100"
        val expected = mapOf("Perfect" to 100)
        assertEquals(expected, QuizTemplateParser.parseDefaultMarks(input))
    }

    @Test
    fun `parseDefaultMarks handles trailing comma`() {
        val input = "First:1, Second:2,"
        val expected = mapOf("First" to 1, "Second" to 2)
        assertEquals(expected, QuizTemplateParser.parseDefaultMarks(input))
    }

    @Test
    fun `formatDefaultMarks handles valid map`() {
        val input = mapOf("Correct" to 1, "Incorrect" to 0, "Bonus" to 5)
        val result = QuizTemplateParser.formatDefaultMarks(input)
        // The order is not guaranteed, so we check the content
        val expectedParts = setOf("Correct:1", "Incorrect:0", "Bonus:5")
        val actualParts = result.split(", ").toSet()
        assertEquals(expectedParts, actualParts)
    }

    @Test
    fun `formatDefaultMarks handles empty map`() {
        val input = emptyMap<String, Int>()
        val expected = ""
        assertEquals(expected, QuizTemplateParser.formatDefaultMarks(input))
    }
}
