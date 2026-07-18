package com.example.myapplication.commands

import androidx.lifecycle.MutableLiveData
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.viewmodel.SeatingChartViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MarkLiveHomeworkCommandTest {

    private val viewModel = mockk<SeatingChartViewModel>(relaxed = true)

    @Test
    fun `execute appends homework logs and updates live scores when session is active`() = runTest {
        val isSessionActive = MutableLiveData<Boolean>(true)
        val liveScores = MutableLiveData<Map<Long, Map<String, Any>>>(emptyMap())

        every { viewModel.isSessionActive } returns isSessionActive
        every { viewModel.liveHomeworkScores } returns liveScores
        every { viewModel.getSessionHomeworkLogs() } returns emptyList()

        val log = HomeworkLog(
            id = 1L,
            studentId = 10L,
            assignmentName = "HW 1",
            status = "Done",
            loggedAt = 12345L,
            comment = "",
            marksData = "{}",
            isComplete = false
        )

        val command = MarkLiveHomeworkCommand(viewModel, listOf(log))
        command.execute()

        // Verify that the command updated session logs
        verify { viewModel.setSessionHomeworkLogs(listOf(log)) }

        // Verify that the command updated liveScores
        verify {
            viewModel.setLiveHomeworkScores(withArg { scores ->
                val studentScores = scores[10L]
                assertEquals("Done", studentScores?.get("HW 1"))
            })
        }
    }

    @Test
    fun `undo restores previous homework scores and removes logs when session is active`() = runTest {
        val isSessionActive = MutableLiveData<Boolean>(true)
        val liveScores = MutableLiveData<Map<Long, Map<String, Any>>>(emptyMap())

        every { viewModel.isSessionActive } returns isSessionActive
        every { viewModel.liveHomeworkScores } returns liveScores

        val log = HomeworkLog(
            id = 1L,
            studentId = 10L,
            assignmentName = "HW 1",
            status = "Done",
            loggedAt = 12345L,
            comment = "",
            marksData = "{}",
            isComplete = false
        )

        every { viewModel.getSessionHomeworkLogs() } returns listOf(log)

        val command = MarkLiveHomeworkCommand(viewModel, listOf(log))

        // Execute first to capture state
        command.execute()

        // Now undo
        command.undo()

        // Verify that the log is removed from session
        verify { viewModel.setSessionHomeworkLogs(emptyList()) }

        // Verify previous scores are restored
        verify { viewModel.setLiveHomeworkScores(emptyMap()) }
    }
}
