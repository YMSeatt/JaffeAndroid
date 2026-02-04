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
    private var generatedId: Long = log.id

    override suspend fun execute() {
        val logToInsert = if (generatedId != 0L) log.copy(id = generatedId) else log
        generatedId = viewModel.internalSaveQuizLog(logToInsert)
    }

    override suspend fun undo() {
        viewModel.deleteQuizLog(log.copy(id = generatedId))
    }

    override fun getDescription(): String = "Log quiz: ${log.quizName}"
}
