package com.example.myapplication.util

import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.QuizMarkType
import org.junit.Assert.assertEquals
import org.junit.Test

class QuizScoreEngineTest {

    private val markCorrect = QuizMarkType(id = 1, name = "Correct", defaultPoints = 1.0, contributesToTotal = true, isExtraCredit = false)
    private val markIncorrect = QuizMarkType(id = 2, name = "Incorrect", defaultPoints = 0.0, contributesToTotal = true, isExtraCredit = false)
    private val markPartial = QuizMarkType(id = 3, name = "Partial Credit", defaultPoints = 0.5, contributesToTotal = true, isExtraCredit = false)
    private val markBonus = QuizMarkType(id = 4, name = "Bonus", defaultPoints = 1.0, contributesToTotal = false, isExtraCredit = true)

    private val markTypes = listOf(markCorrect, markIncorrect, markPartial, markBonus)

    @Test
    fun testStandardPercentage() {
        val log = QuizLog(
            studentId = 1,
            quizName = "Math Test",
            marksData = "{\"Correct\": 8, \"Incorrect\": 2}",
            numQuestions = 10,
            loggedAt = 0,
            comment = null,
            markValue = null,
            maxMarkValue = null
        )
        val result = QuizScoreEngine.calculatePercentage(log, markTypes)
        assertEquals(80.0, result ?: 0.0, 0.01)
    }

    @Test
    fun testPartialCredit() {
        val log = QuizLog(
            studentId = 1,
            quizName = "History Test",
            marksData = "{\"Correct\": 5, \"Partial Credit\": 4, \"Incorrect\": 1}",
            numQuestions = 10,
            loggedAt = 0,
            comment = null,
            markValue = null,
            maxMarkValue = null
        )
        val result = QuizScoreEngine.calculatePercentage(log, markTypes)
        // 5 * 1.0 + 4 * 0.5 = 7.0
        assertEquals(70.0, result ?: 0.0, 0.01)
    }

    @Test
    fun testExtraCredit() {
        val log = QuizLog(
            studentId = 1,
            quizName = "Science Test",
            marksData = "{\"Correct\": 10, \"Bonus\": 2}",
            numQuestions = 10,
            loggedAt = 0,
            comment = null,
            markValue = null,
            maxMarkValue = null
        )
        val result = QuizScoreEngine.calculatePercentage(log, markTypes)
        // (10 * 1.0 + 2 * 1.0) / (10 * 1.0) = 120%
        assertEquals(120.0, result ?: 0.0, 0.01)
    }

    @Test
    fun testExtraCreditOnly() {
        val log = QuizLog(
            studentId = 1,
            quizName = "Bonus Only",
            marksData = "{\"Bonus\": 1}",
            numQuestions = 0,
            loggedAt = 0,
            comment = null,
            markValue = null,
            maxMarkValue = null
        )
        val result = QuizScoreEngine.calculatePercentage(log, markTypes)
        // Python parity: if num_questions <= 0 it returns null unless it falls back to legacy markValue.
        // Wait, my implementation of QuizScoreEngine.kt:
        // if (log.numQuestions <= 0) { ... fallback ... return null }
        assertEquals(null, result)
    }

    @Test
    fun testEmptyMarksDataWithMarkValue() {
        val log = QuizLog(
            studentId = 1,
            quizName = "Legacy Quiz",
            marksData = "{}",
            numQuestions = 10,
            loggedAt = 0,
            comment = null,
            markValue = 7.5,
            maxMarkValue = 10.0
        )
        val result = QuizScoreEngine.calculatePercentage(log, markTypes)
        assertEquals(75.0, result ?: 0.0, 0.01)
    }

    @Test
    fun testLegacyFallback() {
        val log = QuizLog(
            studentId = 1,
            quizName = "Very Old Quiz",
            marksData = "",
            numQuestions = 0,
            loggedAt = 0,
            comment = null,
            markValue = 5.0,
            maxMarkValue = 10.0
        )
        val result = QuizScoreEngine.calculatePercentage(log, markTypes)
        assertEquals(50.0, result ?: 0.0, 0.01)
    }
}
