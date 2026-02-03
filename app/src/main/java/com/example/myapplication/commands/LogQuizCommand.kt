package com.example.myapplication.commands

import com.example.myapplication.data.QuizLog
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to record a student's quiz performance.
 * Reversing this command removes the quiz log entry from the database.
 *
 * @param viewModel The ViewModel to perform the database operations.
 * @param log The [QuizLog] to be recorded.
 */
class LogQuizCommand(
    private val viewModel: SeatingChartViewModel,
    private val log: QuizLog
) : Command {
    override suspend fun execute() {
        viewModel.internalSaveQuizLog(log)
    }

    override suspend fun undo() {
        viewModel.deleteQuizLog(log)
    }

    override fun getDescription(): String = "Log quiz: ${log.quizName}"
}
