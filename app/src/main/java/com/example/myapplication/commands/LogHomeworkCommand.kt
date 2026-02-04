package com.example.myapplication.commands

import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to record a student's homework completion status.
 * Reversing this command removes the homework log entry from the database.
 *
 * @param viewModel The ViewModel to perform the database operations.
 * @param log The [HomeworkLog] to be recorded.
 */
class LogHomeworkCommand(
    private val viewModel: SeatingChartViewModel,
    private val log: HomeworkLog
) : Command {
    private var generatedId: Long = log.id

    override suspend fun execute() {
        val logToInsert = if (generatedId != 0L) log.copy(id = generatedId) else log
        generatedId = viewModel.internalAddHomeworkLog(logToInsert)
    }

    override suspend fun undo() {
        viewModel.deleteHomeworkLog(log.copy(id = generatedId))
    }

    override fun getDescription(): String = "Log homework: ${log.assignmentName}"
}
