package com.example.myapplication.commands

import androidx.lifecycle.MutableLiveData
import com.example.myapplication.data.QuizLog
import com.example.myapplication.viewmodel.SeatingChartViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MarkLiveQuizQuestionCommandTest {

    private val viewModel = mockk<SeatingChartViewModel>(relaxed = true)

    @Test
    fun `execute appends log and updates live scores when session is active`() = runTest {
        val isSessionActive = MutableLiveData<Boolean>(true)
        val liveScores = MutableLiveData<Map<Long, Map<String, Any>>>(emptyMap())

        every { viewModel.isSessionActive } returns isSessionActive
        every { viewModel.liveQuizScores } returns liveScores
        every { viewModel.getSessionQuizLogs() } returns emptyList()

        val quizLog = QuizLog(
            id = 1L,
            studentId = 10L,
            quizName = "Quiz 1",
            markValue = 1.0,
            markType = "Correct",
            maxMarkValue = 1.0,
            loggedAt = 12345L,
            comment = "Correct",
            marksData = "{}",
            numQuestions = 1,
            isComplete = false
        )

        val command = MarkLiveQuizQuestionCommand(viewModel, quizLog)
        command.execute()

        // Verify that the command updated session logs
        verify { viewModel.setSessionQuizLogs(listOf(quizLog)) }

        // Verify that the command updated liveScores
        verify {
            viewModel.setLiveQuizScores(withArg { scores ->
                val studentScores = scores[10L]
                assertEquals("Correct", studentScores?.get("last_response"))
                assertEquals(1, studentScores?.get("total_asked"))
                assertEquals(1, studentScores?.get("correct"))
            })
        }
    }

    @Test
    fun `undo restores previous score state and removes log when session is active`() = runTest {
        val isSessionActive = MutableLiveData<Boolean>(true)
        val liveScores = MutableLiveData<Map<Long, Map<String, Any>>>(emptyMap())

        every { viewModel.isSessionActive } returns isSessionActive
        every { viewModel.liveQuizScores } returns liveScores

        val quizLog = QuizLog(
            id = 1L,
            studentId = 10L,
            quizName = "Quiz 1",
            markValue = 1.0,
            markType = "Correct",
            maxMarkValue = 1.0,
            loggedAt = 12345L,
            comment = "Correct",
            marksData = "{}",
            numQuestions = 1,
            isComplete = false
        )

        every { viewModel.getSessionQuizLogs() } returns listOf(quizLog)

        val command = MarkLiveQuizQuestionCommand(viewModel, quizLog)

        // Execute first to capture state
        command.execute()

        // Now undo
        command.undo()

        // Verify that the log is removed
        verify { viewModel.setSessionQuizLogs(emptyList()) }

        // Verify that the previous score (empty) is restored
        verify { viewModel.setLiveQuizScores(emptyMap()) }
    }
}
